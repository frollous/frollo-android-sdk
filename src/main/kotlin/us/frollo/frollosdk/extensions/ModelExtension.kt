/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.frollo.frollosdk.extensions

import android.os.Bundle
import androidx.sqlite.db.SimpleSQLiteQuery
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.model.api.user.UserUpdateRequest
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountClassification
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountStatus
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountSubType
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountType
import us.frollo.frollosdk.model.coredata.aggregation.merchants.MerchantType
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshStatus
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderStatus
import us.frollo.frollosdk.model.coredata.aggregation.tags.TagsSortType
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategoryType
import us.frollo.frollosdk.model.coredata.aggregation.transactions.Transaction
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionFilter
import us.frollo.frollosdk.model.coredata.bills.BillFrequency
import us.frollo.frollosdk.model.coredata.bills.BillPaymentStatus
import us.frollo.frollosdk.model.coredata.bills.BillStatus
import us.frollo.frollosdk.model.coredata.bills.BillType
import us.frollo.frollosdk.model.coredata.budgets.BudgetFrequency
import us.frollo.frollosdk.model.coredata.budgets.BudgetStatus
import us.frollo.frollosdk.model.coredata.budgets.BudgetTrackingStatus
import us.frollo.frollosdk.model.coredata.budgets.BudgetType
import us.frollo.frollosdk.model.coredata.goals.GoalFrequency
import us.frollo.frollosdk.model.coredata.goals.GoalStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTarget
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingType
import us.frollo.frollosdk.model.coredata.messages.ContentType
import us.frollo.frollosdk.model.coredata.notifications.NotificationPayload
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.model.coredata.shared.OrderType
import us.frollo.frollosdk.model.coredata.user.User
import us.frollo.frollosdk.notifications.NotificationPayloadNames
import kotlin.math.absoluteValue

internal fun User.updateRequest(): UserUpdateRequest =
    UserUpdateRequest(
        firstName = firstName,
        email = email,
        primaryCurrency = primaryCurrency,
        attribution = attribution,
        lastName = lastName,
        mobileNumber = mobileNumber,
        gender = gender,
        currentAddress = currentAddress,
        householdSize = householdSize,
        householdType = householdType,
        occupation = occupation,
        industry = industry,
        dateOfBirth = dateOfBirth,
        driverLicense = driverLicense
    )

internal fun sqlForMessages(messageTypes: List<String>? = null, read: Boolean? = null, contentType: ContentType? = null): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("message")

    if (messageTypes != null && messageTypes.isNotEmpty()) {
        val sb = StringBuilder()
        sb.append("(")

        messageTypes.forEachIndexed { index, str ->
            sb.append("(message_types LIKE '%|$str|%')")
            if (index < messageTypes.size - 1) sb.append(" OR ")
        }

        sb.append(")")

        sqlQueryBuilder.appendSelection(selection = sb.toString())
    }

    read?.let { sqlQueryBuilder.appendSelection(selection = "read = ${ it.toInt() }") }

    contentType?.let { sqlQueryBuilder.appendSelection(selection = "content_type = '${ it.name }'") }

    return sqlQueryBuilder.create()
}

internal fun sqlForMessagesCount(messageTypes: List<String>? = null, read: Boolean? = null, contentType: ContentType? = null): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("message")

    if (messageTypes != null && messageTypes.isNotEmpty()) {
        val sb = StringBuilder()
        sb.append("(")

        messageTypes.forEachIndexed { index, str ->
            sb.append("(message_types LIKE '%|$str|%')")
            if (index < messageTypes.size - 1) sb.append(" OR ")
        }

        sb.append(")")

        sqlQueryBuilder.appendSelection(selection = sb.toString())
    }

    read?.let { sqlQueryBuilder.appendSelection(selection = "read = ${ it.toInt() }") }

    contentType?.let { sqlQueryBuilder.appendSelection(selection = "content_type = '${ it.name }'") }

    sqlQueryBuilder.columns(columns = arrayOf("COUNT(msg_id)"))

    return sqlQueryBuilder.create()
}

internal fun sqlForTransactionIdsToGetStaleIds(
    beforeDateString: String? = null,
    afterDateString: String? = null,
    beforeId: Long? = null,
    afterId: Long? = null,
    transactionFilter: TransactionFilter? = null,
    today: String = LocalDate.now().toString(Transaction.DATE_FORMAT_PATTERN)
): SimpleSQLiteQuery {

    val sqlQueryBuilder = SimpleSQLiteQueryBuilder(tableName = "transaction_model", aliasName = "t")

    sqlQueryBuilder.columns(arrayOf("t.transaction_id"))
    sqlQueryBuilder.appendJoin("LEFT JOIN account a ON t.account_id = a.account_id")

    val beforeDate = beforeDateString?.toLocalDate(Transaction.DATE_FORMAT_PATTERN)
    val afterDate = afterDateString?.toLocalDate(Transaction.DATE_FORMAT_PATTERN)

    when {
        // TODO: Understand all the edge case scenarios and revisit these queries to check they can be optimized
        beforeDate != null && afterDate != null -> {
            val dayBeforeFirstDate = beforeDate.minusDays(1).toString(Transaction.DATE_FORMAT_PATTERN)
            val dayAfterLastDate = afterDate.plusDays(1).toString(Transaction.DATE_FORMAT_PATTERN)

            val daysInBetween = ChronoUnit.DAYS.between(beforeDate, afterDate)
            val selection = when (daysInBetween.absoluteValue) {
                0L -> {
                    // This is same day so we need an AND
                    "((t.transaction_date = '$beforeDateString' AND t.transaction_id <= $beforeId) " +
                        "AND (t.transaction_date = '$afterDateString' AND t.transaction_id >= $afterId))"
                }
                1L -> {
                    // This is 2 different dates, so we need an OR
                    "((t.transaction_date = '$beforeDateString' AND t.transaction_id <= $beforeId) " +
                        "OR (t.transaction_date = '$afterDateString' AND t.transaction_id >= $afterId))"
                }
                2L -> {
                    "((t.transaction_date = '$beforeDateString' AND t.transaction_id <= $beforeId) " +
                        "AND ((t.transaction_date = '$beforeDateString' AND t.transaction_id >= $afterId) " +
                        "OR t.transaction_date = '$dayAfterLastDate'))"
                }
                else -> {
                    "((t.transaction_date >= '$dayAfterLastDate' AND t.transaction_date <= '$dayBeforeFirstDate') " +
                        "OR ((t.transaction_date = '$beforeDateString' AND t.transaction_id <= $beforeId) " +
                        "OR (t.transaction_date = '$afterDateString' AND t.transaction_id >= $afterId)))"
                }
            }
            sqlQueryBuilder.appendSelection(selection)
        }

        beforeDate != null && afterDate == null -> {
            val dayBeforeFirstDate = beforeDate.minusDays(1).toString(Transaction.DATE_FORMAT_PATTERN)
            // Last page, so we take the first transaction in before and get everything after that
            val selection = "(t.transaction_date <= '$dayBeforeFirstDate' " +
                "OR (t.transaction_date = '$beforeDateString' AND t.transaction_id <= $beforeId))"
            sqlQueryBuilder.appendSelection(selection)
        }

        beforeDate == null && afterDate != null -> {
            val dayAfterLastDate = afterDate.plusDays(1).toString(Transaction.DATE_FORMAT_PATTERN)
            // First page - Before date is today as we are fetching from top
            val selection = "(t.transaction_date <= '$today' " +
                "OR t.transaction_date >= '$dayAfterLastDate' " +
                "OR (t.transaction_date = '$afterDateString' AND t.transaction_id >= $afterId))"
            sqlQueryBuilder.appendSelection(selection)
        }

        else -> {
            // No pagination or Single page
        }
    }

    transactionFilter?.let { appendTransactionFilterToSqlQuery(sqlQueryBuilder, it) }

    return sqlQueryBuilder.create()
}

internal fun sqlForTransactions(transactionFilter: TransactionFilter? = null): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder(tableName = "transaction_model", aliasName = "t")

    sqlQueryBuilder.columns(arrayOf("t.*"))
    sqlQueryBuilder.appendJoin("LEFT JOIN account a ON t.account_id = a.account_id")

    transactionFilter?.let { appendTransactionFilterToSqlQuery(sqlQueryBuilder, it) }

    return sqlQueryBuilder.create()
}

private fun appendTransactionFilterToSqlQuery(sqlQueryBuilder: SimpleSQLiteQueryBuilder, filter: TransactionFilter) {
    filter.transactionIds?.let { sqlQueryBuilder.appendSelection(selection = "t.transaction_id IN (${ it.joinToString(",") })") }
    filter.accountIds?.let { sqlQueryBuilder.appendSelection(selection = "t.account_id IN (${ it.joinToString(",") })") }
    filter.merchantIds?.let { sqlQueryBuilder.appendSelection(selection = "t.merchant_id IN (${ it.joinToString(",") })") }
    filter.transactionCategoryIds?.let { sqlQueryBuilder.appendSelection(selection = "t.category_id IN (${ it.joinToString(",") })") }
    filter.budgetCategory?.let { sqlQueryBuilder.appendSelection(selection = "t.budget_category = '${ it.name }'") }
    filter.baseType?.let { sqlQueryBuilder.appendSelection(selection = "t.base_type = '${ it.name }'") }
    filter.status?.let { sqlQueryBuilder.appendSelection(selection = "t.status = '${ it.name}'") }
    filter.minimumAmount?.let { sqlQueryBuilder.appendSelection(selection = "ABS(CAST(t.amount_amount AS DECIMAL)) >= $it") }
    filter.maximumAmount?.let { sqlQueryBuilder.appendSelection(selection = "ABS(CAST(t.amount_amount AS DECIMAL)) <= $it") }
    filter.transactionIncluded?.let { sqlQueryBuilder.appendSelection(selection = "t.included = ${ it.toInt() }") }
    filter.accountIncluded?.let { sqlQueryBuilder.appendSelection(selection = "a.included = ${ it.toInt() }") }
    filter.searchTerm?.let { sqlQueryBuilder.appendSelection(selection = " ( t.description_original LIKE '%$it%' OR t.description_user LIKE '%$it%' OR t.description_simple LIKE '%$it%' ) ") }
    val filterTags = filter.tags
    if (filterTags != null && filterTags.isNotEmpty()) {
        val sb = StringBuilder()
        sb.append("(")
        filterTags.forEachIndexed { index, str ->
            sb.append("(t.user_tags LIKE '%|$str|%')")
            if (index < filterTags.size - 1) sb.append(" OR ")
        }
        sb.append(")")
        sqlQueryBuilder.appendSelection(selection = sb.toString())
    }
    when {
        filter.fromDate != null && filter.toDate == null -> sqlQueryBuilder.appendSelection(selection = "t.transaction_date >= '${ filter.fromDate }'")
        filter.fromDate == null && filter.toDate != null -> sqlQueryBuilder.appendSelection(selection = "t.transaction_date <= '${ filter.toDate }'")
        filter.fromDate != null && filter.toDate != null -> sqlQueryBuilder.appendSelection(selection = "(t.transaction_date BETWEEN Date('${ filter.fromDate }') AND Date('${ filter.toDate }'))")
    }
}

internal fun sqlForExistingAccountBalanceReports(date: String, period: ReportPeriod, reportAccountIds: LongArray, accountId: Long? = null, accountType: AccountType? = null): SimpleSQLiteQuery {
    val sb = StringBuilder()

    sb.append("SELECT rab.* FROM report_account_balance AS rab ")

    accountType?.let { sb.append(" LEFT JOIN account AS a ON rab.account_id = a.account_id ") }

    sb.append(" WHERE rab.period = '${period.name}' AND rab.date = '$date' AND rab.account_id IN (${reportAccountIds.joinToString(",")}) ")

    accountId?.let { sb.append(" AND rab.account_id = $it ") }

    accountType?.let { sb.append(" AND a.attr_account_type = '${accountType.name}' ") }

    return SimpleSQLiteQuery(sb.toString())
}

internal fun sqlForStaleIdsAccountBalanceReports(date: String, period: ReportPeriod, reportAccountIds: LongArray, accountId: Long? = null, accountType: AccountType? = null): SimpleSQLiteQuery {
    val sb = StringBuilder()

    sb.append("SELECT rab.* FROM report_account_balance AS rab ")

    accountType?.let { sb.append(" LEFT JOIN account AS a ON rab.account_id = a.account_id ") }

    sb.append(" WHERE rab.period = '${period.name}' AND rab.date = '$date' AND rab.account_id NOT IN (${reportAccountIds.joinToString(",")}) ")

    accountId?.let { sb.append(" AND rab.account_id = $it ") }

    accountType?.let { sb.append(" AND a.attr_account_type = '${accountType.name}' ") }

    return SimpleSQLiteQuery(sb.toString())
}

internal fun sqlForFetchingAccountBalanceReports(fromDate: String, toDate: String, period: ReportPeriod, accountId: Long? = null, accountType: AccountType? = null): SimpleSQLiteQuery {
    val sb = StringBuilder()

    sb.append("SELECT rab.* FROM report_account_balance AS rab ")

    accountType?.let { sb.append(" LEFT JOIN account AS a ON rab.account_id = a.account_id ") }

    sb.append(" WHERE rab.period = '${period.name}' AND (rab.date BETWEEN '$fromDate' AND '$toDate') ")

    accountId?.let { sb.append(" AND rab.account_id = $it ") }

    accountType?.let { sb.append(" AND a.attr_account_type = '${accountType.name}' ") }

    return SimpleSQLiteQuery(sb.toString())
}

internal fun sqlForUserTags(searchTerm: String? = null, sortBy: TagsSortType? = null, orderBy: OrderType? = null): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("transaction_user_tags")

    val sort = sortBy?.toString() ?: TagsSortType.NAME.toString()
    val order = orderBy?.toString() ?: OrderType.ASC.toString()
    sqlQueryBuilder.orderBy(orderBy = "$sort $order")
    searchTerm?.let { sqlQueryBuilder.appendSelection(selection = "name LIKE '%$searchTerm%'") }

    return sqlQueryBuilder.create()
}

internal fun sqlForBills(frequency: BillFrequency? = null, paymentStatus: BillPaymentStatus? = null, status: BillStatus? = null, type: BillType? = null): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("bill")

    frequency?.let { sqlQueryBuilder.appendSelection(selection = "frequency = '${ it.name }'") }
    paymentStatus?.let { sqlQueryBuilder.appendSelection(selection = "payment_status = '${ it.name }'") }
    status?.let { sqlQueryBuilder.appendSelection(selection = "status = '${ it.name }'") }
    type?.let { sqlQueryBuilder.appendSelection(selection = "bill_type = '${ it.name }'") }

    return sqlQueryBuilder.create()
}

internal fun sqlForBillPayments(billId: Long? = null, fromDate: String? = null, toDate: String? = null, frequency: BillFrequency? = null, paymentStatus: BillPaymentStatus? = null): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("bill_payment")

    billId?.let { sqlQueryBuilder.appendSelection(selection = "bill_id = $it") }
    ifNotNull(fromDate, toDate) { from, to -> sqlQueryBuilder.appendSelection(selection = "(date BETWEEN Date('$from') AND Date('$to'))") }
    frequency?.let { sqlQueryBuilder.appendSelection(selection = "frequency = '${ it.name }'") }
    paymentStatus?.let { sqlQueryBuilder.appendSelection(selection = "payment_status = '${ it.name }'") }

    return sqlQueryBuilder.create()
}

internal fun sqlForProviders(status: ProviderStatus? = null): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("provider")

    status?.let { sqlQueryBuilder.appendSelection(selection = "provider_status = '${ it.name }'") }

    return sqlQueryBuilder.create()
}

internal fun sqlForProviderAccounts(providerId: Long? = null, refreshStatus: AccountRefreshStatus? = null, externalId: String? = null): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("provider_account")

    providerId?.let { sqlQueryBuilder.appendSelection(selection = "provider_id = $it") }
    refreshStatus?.let { sqlQueryBuilder.appendSelection(selection = "r_status_status = '${ it.name }'") }
    externalId?.let { sqlQueryBuilder.appendSelection(selection = "external_id = '$it'") }

    return sqlQueryBuilder.create()
}

internal fun sqlForAccounts(
    providerAccountId: Long? = null,
    accountStatus: AccountStatus? = null,
    accountSubType: AccountSubType? = null,
    accountType: AccountType? = null,
    accountClassification: AccountClassification? = null,
    favourite: Boolean? = null,
    hidden: Boolean? = null,
    included: Boolean? = null,
    refreshStatus: AccountRefreshStatus? = null,
    externalId: String? = null
): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("account")

    providerAccountId?.let { sqlQueryBuilder.appendSelection(selection = "provider_account_id = $it") }
    accountStatus?.let { sqlQueryBuilder.appendSelection(selection = "account_status = '${ it.name }'") }
    accountSubType?.let { sqlQueryBuilder.appendSelection(selection = "attr_account_sub_type = '${ it.name }'") }
    accountType?.let { sqlQueryBuilder.appendSelection(selection = "attr_account_type = '${ it.name }'") }
    accountClassification?.let { sqlQueryBuilder.appendSelection(selection = "attr_account_classification = '${ it.name }'") }
    favourite?.let { sqlQueryBuilder.appendSelection(selection = "favourite = ${ it.toInt() }") }
    hidden?.let { sqlQueryBuilder.appendSelection(selection = "hidden = ${ it.toInt() }") }
    included?.let { sqlQueryBuilder.appendSelection(selection = "included = ${ it.toInt() }") }
    refreshStatus?.let { sqlQueryBuilder.appendSelection(selection = "r_status_status = '${ it.name }'") }
    externalId?.let { sqlQueryBuilder.appendSelection(selection = "external_id = '$it'") }

    return sqlQueryBuilder.create()
}

internal fun sqlForUpdateAccount(
    accountId: Long,
    hidden: Boolean,
    included: Boolean,
    favourite: Boolean? = null,
    accountSubType: AccountSubType? = null,
    nickName: String? = null
): SimpleSQLiteQuery {
    val sb = StringBuffer()
    sb.append("UPDATE account SET ")

    sb.append("hidden = ${ hidden.toInt() } , ")
    sb.append("included = ${ included.toInt() } ")
    favourite?.let { sb.append(", favourite = ${ it.toInt() } ") }
    accountSubType?.let { sb.append(", attr_account_sub_type = '${ it.name }' ") }
    nickName?.let { sb.append(", nick_name = '$it' ") }
    sb.append("WHERE account_id = $accountId ")

    return SimpleSQLiteQuery(sb.toString())
}

internal fun sqlForTransactionCategories(defaultBudgetCategory: BudgetCategory? = null, type: TransactionCategoryType? = null): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("transaction_category")

    defaultBudgetCategory?.let { sqlQueryBuilder.appendSelection(selection = "default_budget_category = '${ it.name }'") }
    type?.let { sqlQueryBuilder.appendSelection(selection = "category_type = '${ it.name }'") }

    return sqlQueryBuilder.create()
}

internal fun sqlForMerchants(type: MerchantType? = null): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("merchant")

    type?.let { sqlQueryBuilder.appendSelection(selection = "merchant_type = '${ it.name }'") }

    return sqlQueryBuilder.create()
}

internal fun sqlForMerchantsIds(before: Long? = null, after: Long? = null): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("merchant")

    sqlQueryBuilder.columns(arrayOf("merchant_id"))
    before?.let { sqlQueryBuilder.appendSelection(selection = "merchant_id > $it") }
    after?.let { sqlQueryBuilder.appendSelection(selection = "merchant_id <= $it") }

    return sqlQueryBuilder.create()
}

internal fun sqlForGoals(
    frequency: GoalFrequency? = null,
    status: GoalStatus? = null,
    target: GoalTarget? = null,
    trackingStatus: GoalTrackingStatus? = null,
    trackingType: GoalTrackingType? = null,
    accountId: Long? = null
): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("goal")

    frequency?.let { sqlQueryBuilder.appendSelection(selection = "frequency = '${ it.name }'") }
    status?.let { sqlQueryBuilder.appendSelection(selection = "status = '${ it.name }'") }
    target?.let { sqlQueryBuilder.appendSelection(selection = "target = '${ it.name }'") }
    trackingStatus?.let { sqlQueryBuilder.appendSelection(selection = "tracking_status = '${ it.name }'") }
    trackingType?.let { sqlQueryBuilder.appendSelection(selection = "tracking_type = '${ it.name }'") }
    accountId?.let { sqlQueryBuilder.appendSelection(selection = "account_id = $it") }

    return sqlQueryBuilder.create()
}

internal fun sqlForGoalIds(
    status: GoalStatus? = null,
    trackingStatus: GoalTrackingStatus? = null
): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("goal")

    sqlQueryBuilder.columns(arrayOf("goal_id"))
    status?.let { sqlQueryBuilder.appendSelection(selection = "status = '${ it.name }'") }
    trackingStatus?.let { sqlQueryBuilder.appendSelection(selection = "tracking_status = '${ it.name }'") }

    return sqlQueryBuilder.create()
}

internal fun sqlForGoalPeriods(
    goalId: Long? = null,
    trackingStatus: GoalTrackingStatus? = null
): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("goal_period")

    goalId?.let { sqlQueryBuilder.appendSelection(selection = "goal_id = $it") }
    trackingStatus?.let { sqlQueryBuilder.appendSelection(selection = "tracking_status = '${ it.name }'") }

    return sqlQueryBuilder.create()
}

internal fun Bundle.toNotificationPayload(): NotificationPayload =
    createNotificationPayload(
        getString(NotificationPayloadNames.EVENT.toString()),
        getString(NotificationPayloadNames.LINK.toString()),
        getString(NotificationPayloadNames.TRANSACTION_IDS.toString()),
        getString(NotificationPayloadNames.USER_EVENT_ID.toString()),
        getString(NotificationPayloadNames.USER_MESSAGE_ID.toString())
    )

internal fun Map<String, String>.toNotificationPayload(): NotificationPayload =
    createNotificationPayload(
        get(NotificationPayloadNames.EVENT.toString()),
        get(NotificationPayloadNames.LINK.toString()),
        get(NotificationPayloadNames.TRANSACTION_IDS.toString()),
        get(NotificationPayloadNames.USER_EVENT_ID.toString()),
        get(NotificationPayloadNames.USER_MESSAGE_ID.toString())
    )

internal fun createNotificationPayload(event: String? = null, link: String? = null, transactionIDs: String? = null, userEventID: String? = null, userMessageID: String? = null) =
    NotificationPayload(
        event = event,
        link = link,
        transactionIDs = transactionIDs
            ?.replace("[", "")
            ?.replace("]", "")
            ?.split(",")
            ?.map { it.toLong() }
            ?.toList(),
        userEventID = userEventID?.trim()?.toLong(),
        userMessageID = userMessageID?.trim()?.toLong()
    )

internal fun String.toBudgetCategory(): BudgetCategory? {
    return when (this) {
        BudgetCategory.INCOME.toString() -> BudgetCategory.INCOME
        BudgetCategory.LIVING.toString() -> BudgetCategory.LIVING
        BudgetCategory.LIFESTYLE.toString() -> BudgetCategory.LIFESTYLE
        BudgetCategory.SAVINGS.toString() -> BudgetCategory.SAVINGS
        BudgetCategory.ONE_OFF.toString() -> BudgetCategory.ONE_OFF
        else -> null
    }
}

internal fun sqlForBudgets(
    current: Boolean? = null,
    budgetFrequency: BudgetFrequency? = null,
    budgetStatus: BudgetStatus? = null,
    budgetTrackingStatus: BudgetTrackingStatus? = null,
    budgetType: BudgetType? = null,
    budgetTypeValue: String? = null
): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("budget")

    budgetFrequency?.let { sqlQueryBuilder.appendSelection(selection = "frequency = '${ it.name }'") }
    budgetStatus?.let { sqlQueryBuilder.appendSelection(selection = "status = '${ it.name }'") }
    budgetTrackingStatus?.let { sqlQueryBuilder.appendSelection(selection = "tracking_status = '${ it.name }'") }
    budgetType?.let { sqlQueryBuilder.appendSelection(selection = "type = '${ it.name }'") }
    budgetTypeValue?.let { sqlQueryBuilder.appendSelection(selection = "type_value = '$it'") }
    current?.let { sqlQueryBuilder.appendSelection(selection = "is_current = ${it.toInt()}") }

    return sqlQueryBuilder.create()
}

internal fun sqlForBudgetIds(
    current: Boolean? = null,
    budgetType: BudgetType? = null
): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("budget")

    sqlQueryBuilder.columns(arrayOf("budget_id"))
    current?.let { sqlQueryBuilder.appendSelection(selection = "is_current = ${ it.toInt() }") }
    budgetType?.let { sqlQueryBuilder.appendSelection(selection = "type = '${ it.name }'") }

    return sqlQueryBuilder.create()
}

internal fun sqlForBudgetPeriodIds(
    budgetId: Long,
    fromDate: String? = null,
    toDate: String? = null
): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("budget_period")
    sqlQueryBuilder.columns(arrayOf("budget_period_id"))
    sqlQueryBuilder.appendSelection(selection = "budget_id = $budgetId ")
    ifNotNull(fromDate, toDate) { startDate, endDate ->
        sqlQueryBuilder.appendSelection(selection = "(start_date BETWEEN Date('$startDate') AND Date('$endDate'))")
    }
    return sqlQueryBuilder.create()
}

internal fun sqlForBudgetPeriods(
    budgetId: Long? = null,
    trackingStatus: BudgetTrackingStatus? = null,
    fromDate: String? = null,
    toDate: String? = null
): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("budget_period")

    budgetId?.let { sqlQueryBuilder.appendSelection(selection = "budget_id = $it") }
    trackingStatus?.let { sqlQueryBuilder.appendSelection(selection = "tracking_status = '${ it.name }'") }
    ifNotNull(fromDate, toDate) {
        from, to ->
        sqlQueryBuilder.appendSelection(selection = "(start_date BETWEEN Date('$from') AND Date('$to'))")
    }

    return sqlQueryBuilder.create()
}
