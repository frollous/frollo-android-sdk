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
import us.frollo.frollosdk.model.coredata.budgets.BudgetPeriod
import us.frollo.frollosdk.model.coredata.budgets.BudgetStatus
import us.frollo.frollosdk.model.coredata.budgets.BudgetTrackingStatus
import us.frollo.frollosdk.model.coredata.budgets.BudgetType
import us.frollo.frollosdk.model.coredata.cards.CardStatus
import us.frollo.frollosdk.model.coredata.cdr.ConsentStatus
import us.frollo.frollosdk.model.coredata.contacts.PaymentMethod
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

internal fun User.updateRequest(): UserUpdateRequest =
    UserUpdateRequest(
        firstName = firstName,
        email = email,
        primaryCurrency = primaryCurrency,
        attribution = attribution,
        lastName = lastName,
        mobileNumber = mobileNumber,
        gender = gender,
        address = if (address?.postcode?.isNotBlank() == true) address else null, // This is to avoid the "Invalid postcode length" error from the host
        mailingAddress = if (mailingAddress?.postcode?.isNotBlank() == true) address else null, // This is to avoid the "Invalid postcode length" error from the host
        householdSize = householdSize,
        householdType = householdType,
        occupation = occupation,
        industry = industry,
        dateOfBirth = dateOfBirth,
        driverLicense = driverLicense,
        foreignTax = foreignTax,
        taxResidency = taxResidency,
        tfn = tfn,
        tin = tin
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
    transactionFilter: TransactionFilter? = null
): SimpleSQLiteQuery {

    val sqlQueryBuilder = SimpleSQLiteQueryBuilder(tableName = "transaction_model", aliasName = "t")

    sqlQueryBuilder.columns(arrayOf("t.transaction_id"))
    sqlQueryBuilder.appendJoin("LEFT JOIN account a ON t.account_id = a.account_id")

    val beforeDate = beforeDateString?.toLocalDate(Transaction.DATE_FORMAT_PATTERN)
    val dayBeforeFirstDate = beforeDate?.minusDays(1)?.toString(Transaction.DATE_FORMAT_PATTERN)
    val afterDate = afterDateString?.toLocalDate(Transaction.DATE_FORMAT_PATTERN)
    val dayAfterLastDate = afterDate?.plusDays(1)?.toString(Transaction.DATE_FORMAT_PATTERN)

    /**
     * Following code creates a filter predicate that will be applied to cached transactions to update
     *
     * Predicate 1: Considers All transactions before the date of first transaction (date of first transaction - 1) (as the list is descending)
     * Predicate 2: Considers All transactions of the date of first transaction but <= ID of the first transaction.
     * Predicate 3: Considers All transactions after the date of last transaction (date of last transaction + 1) (as the list is descending)
     * Predicate 4: All transactions of the date of last transaction but >= ID of the last transaction.
     * Predicate 5: Predicate 1 OR Predicate 2 (Upper limit Predicate)
     * Predicate 6: Predicate 2 OR Predicate 4 (Lower limit Predicate)
     * Predicate 7: Predicate 5 AND Predicate 6 (Satisfy both upper and lower limit) (Final filter predicate to apply in query)
     *
     * More explanation: https://frollo.atlassian.net/wiki/spaces/DEV/pages/2301493342/Transaction+Response+Handling
     */

    // TODO: Need to consider one more edge case - https://frollo.atlassian.net/browse/SDK-590

    // Filter by before cursor in paginated response
    if (beforeDateString != null && beforeId != null && dayBeforeFirstDate != null) {
        val selection = "(t.transaction_date <= '$dayBeforeFirstDate' " +
            "OR (t.transaction_date = '$beforeDateString' AND t.transaction_id <= $beforeId))"
        sqlQueryBuilder.appendSelection(selection)
    }

    // Filter by after cursor in paginated response
    if (afterDateString != null && afterId != null && dayAfterLastDate != null) {
        val selection = "(t.transaction_date >= '$dayAfterLastDate' " +
            "OR (t.transaction_date = '$afterDateString' AND t.transaction_id >= $afterId))"
        sqlQueryBuilder.appendSelection(selection)
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
    filter.transactionIds?.let { if (it.isNotEmpty()) sqlQueryBuilder.appendSelection(selection = "t.transaction_id IN (${ it.joinToString(",") })") }
    filter.accountIds?.let { if (it.isNotEmpty()) sqlQueryBuilder.appendSelection(selection = "t.account_id IN (${ it.joinToString(",") })") }
    filter.merchantIds?.let { if (it.isNotEmpty()) sqlQueryBuilder.appendSelection(selection = "t.merchant_id IN (${ it.joinToString(",") })") }
    filter.transactionCategoryIds?.let { if (it.isNotEmpty()) sqlQueryBuilder.appendSelection(selection = "t.category_id IN (${ it.joinToString(",") })") }
    filter.billId?.let { sqlQueryBuilder.appendSelection(selection = "t.bill_id = $it") }
    filter.goalId?.let { sqlQueryBuilder.appendSelection(selection = "t.goal_id = $it") }
    filter.budgetCategory?.let { sqlQueryBuilder.appendSelection(selection = "t.budget_category = '${ it.name }'") }
    filter.baseType?.let { sqlQueryBuilder.appendSelection(selection = "t.base_type = '${ it.name }'") }
    filter.status?.let { sqlQueryBuilder.appendSelection(selection = "t.status = '${ it.name}'") }
    filter.minimumAmount?.let { if (it.isNotBlank()) sqlQueryBuilder.appendSelection(selection = "ABS(CAST(t.amount_amount AS DECIMAL)) >= $it") }
    filter.maximumAmount?.let { if (it.isNotBlank()) sqlQueryBuilder.appendSelection(selection = "ABS(CAST(t.amount_amount AS DECIMAL)) <= $it") }
    filter.transactionIncluded?.let { sqlQueryBuilder.appendSelection(selection = "t.included = ${ it.toInt() }") }
    filter.accountIncluded?.let { sqlQueryBuilder.appendSelection(selection = "a.included = ${ it.toInt() }") }
    filter.searchTerm?.let { if (it.isNotBlank()) sqlQueryBuilder.appendSelection(selection = " ( t.description_original LIKE '%$it%' OR t.description_user LIKE '%$it%' OR t.description_simple LIKE '%$it%' ) ") }
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
        filter.fromDate?.isNotBlank() == true && (filter.toDate == null || filter.toDate?.isBlank() == true) -> {
            sqlQueryBuilder.appendSelection(selection = "t.transaction_date >= '${ filter.fromDate }'")
        }
        (filter.fromDate == null || filter.fromDate?.isBlank() == true) && filter.toDate?.isNotBlank() == true -> {
            sqlQueryBuilder.appendSelection(selection = "t.transaction_date <= '${ filter.toDate }'")
        }
        filter.fromDate?.isNotBlank() == true && filter.toDate?.isNotBlank() == true -> {
            sqlQueryBuilder.appendSelection(selection = "(t.transaction_date BETWEEN Date('${ filter.fromDate }') AND Date('${ filter.toDate }'))")
        }
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
        getString(NotificationPayloadNames.USER_MESSAGE_ID.toString()),
        getString(NotificationPayloadNames.ONBOARDING_STEP.toString())
    )

internal fun Map<String, String>.toNotificationPayload(): NotificationPayload =
    createNotificationPayload(
        get(NotificationPayloadNames.EVENT.toString()),
        get(NotificationPayloadNames.LINK.toString()),
        get(NotificationPayloadNames.TRANSACTION_IDS.toString()),
        get(NotificationPayloadNames.USER_EVENT_ID.toString()),
        get(NotificationPayloadNames.USER_MESSAGE_ID.toString()),
        get(NotificationPayloadNames.ONBOARDING_STEP.toString())
    )

internal fun createNotificationPayload(
    event: String? = null,
    link: String? = null,
    transactionIDs: String? = null,
    userEventID: String? = null,
    userMessageID: String? = null,
    onboardingStep: String? = null,
): NotificationPayload {
    return NotificationPayload(
        event = event,
        link = link,
        transactionIDs = transactionIDs
            ?.replace("[", "")
            ?.replace("]", "")
            ?.split(",")
            ?.map { it.toLong() }
            ?.toList(),
        userEventID = userEventID?.trim()?.toLong(),
        userMessageID = userMessageID?.trim()?.toLong(),
        onboardingStep = onboardingStep
    )
}

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

internal fun sqlForBudgetPeriodIdsToGetStaleIds(
    beforeDateString: String? = null,
    afterDateString: String? = null,
    beforeId: Long? = null,
    afterId: Long? = null,
    budgetId: Long? = null,
    budgetStatus: BudgetStatus? = null,
    fromDate: String? = null,
    toDate: String? = null
): SimpleSQLiteQuery {

    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("budget_period", aliasName = "bp")
    sqlQueryBuilder.columns(arrayOf("bp.budget_period_id"))
    sqlQueryBuilder.appendJoin("LEFT JOIN budget b ON bp.budget_id = b.budget_id")

    val beforeDate = beforeDateString?.toLocalDate(BudgetPeriod.DATE_FORMAT_PATTERN)
    val dayAfterFirstDate = beforeDate?.plusDays(1)?.toString(BudgetPeriod.DATE_FORMAT_PATTERN)
    val afterDate = afterDateString?.toLocalDate(BudgetPeriod.DATE_FORMAT_PATTERN)
    val dayBeforeLastDate = afterDate?.minusDays(1)?.toString(BudgetPeriod.DATE_FORMAT_PATTERN)

    /**
     * Following code creates a filter predicate that will be applied to cached budget periods to update
     *
     * Predicate 1: Considers All budget periods after the date of first budget period (date of first budget period + 1) (as the list is ascending)
     * Predicate 2: Considers All budget periods of the date of first budget period but >= ID of the first budget period.
     * Predicate 3: Considers All budget periods before the date of last budget period (date of last budget period - 1) (as the list is ascending)
     * Predicate 4: Considers All budget periods of the date of last budget period but <= ID of the last budget period.
     * Predicate 5: Predicate 1 OR Predicate 2 (Upper limit Predicate)
     * Predicate 6: Predicate 2 OR Predicate 4 (Lower limit Predicate)
     * Predicate 7: Predicate 5 AND Predicate 6 (Satisfy both upper and lower limit) (Final filter predicate to apply in query)
     *
     * More explanation: https://frollo.atlassian.net/wiki/spaces/DEV/pages/2301526130/Budget+Period+Response+Handling
     */

    // TODO: Need to consider one more edge case - https://frollo.atlassian.net/browse/SDK-590

    // Filter by before cursor in paginated response
    if (beforeDateString != null && beforeId != null && dayAfterFirstDate != null) {
        val selection = "(bp.start_date >= '$dayAfterFirstDate' " +
            "OR (bp.start_date = '$beforeDateString' AND bp.budget_period_id >= $beforeId))"
        sqlQueryBuilder.appendSelection(selection)
    }

    // Filter by after cursor in paginated response
    if (afterDateString != null && afterId != null && dayBeforeLastDate != null) {
        val selection = "(bp.start_date <= '$dayBeforeLastDate' " +
            "OR (bp.start_date = '$afterDateString' AND bp.budget_period_id <= $afterId))"
        sqlQueryBuilder.appendSelection(selection)
    }

    // Append other filters to the query
    appendBudgetPeriodFiltersToSqlQuery(
        sqlQueryBuilder = sqlQueryBuilder,
        budgetId = budgetId,
        budgetStatus = budgetStatus,
        fromDate = fromDate,
        toDate = toDate
    )

    return sqlQueryBuilder.create()
}

internal fun sqlForBudgetPeriods(
    budgetId: Long? = null,
    budgetStatus: BudgetStatus? = null,
    trackingStatus: BudgetTrackingStatus? = null,
    fromDate: String? = null,
    toDate: String? = null
): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("budget_period", aliasName = "bp")
    sqlQueryBuilder.columns(arrayOf("bp.*"))
    sqlQueryBuilder.appendJoin("LEFT JOIN budget b ON bp.budget_id = b.budget_id")

    appendBudgetPeriodFiltersToSqlQuery(
        sqlQueryBuilder = sqlQueryBuilder,
        budgetId = budgetId,
        budgetStatus = budgetStatus,
        trackingStatus = trackingStatus,
        fromDate = fromDate,
        toDate = toDate
    )

    return sqlQueryBuilder.create()
}

private fun appendBudgetPeriodFiltersToSqlQuery(
    sqlQueryBuilder: SimpleSQLiteQueryBuilder,
    budgetId: Long? = null,
    budgetStatus: BudgetStatus? = null,
    trackingStatus: BudgetTrackingStatus? = null,
    fromDate: String? = null,
    toDate: String? = null
) {
    budgetId?.let {
        sqlQueryBuilder.appendSelection(selection = "bp.budget_id = $it")
    } ?: run {
        // BudgetStatus filter is applicable only when fetching ALL budgets not for a specific budget.
        budgetStatus?.let { sqlQueryBuilder.appendSelection(selection = "b.status = '${ it.name }'") }
    }
    trackingStatus?.let { sqlQueryBuilder.appendSelection(selection = "bp.tracking_status = '${ it.name }'") }

    when {
        fromDate?.isNotBlank() == true && (toDate == null || toDate.isBlank()) -> {
            sqlQueryBuilder.appendSelection(selection = "bp.start_date >= '$fromDate'")
        }
        (fromDate == null || fromDate.isBlank()) && toDate?.isNotBlank() == true -> {
            sqlQueryBuilder.appendSelection(selection = "bp.start_date <= '$toDate'")
        }
        fromDate?.isNotBlank() == true && toDate?.isNotBlank() == true -> {
            sqlQueryBuilder.appendSelection(selection = "(bp.start_date BETWEEN Date('$fromDate') AND Date('$toDate'))")
        }
    }
}

internal fun sqlForImages(imageType: String? = null): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("image")
    imageType?.let { sqlQueryBuilder.appendSelection(selection = "image_types LIKE '%|$it|%'") }
    return sqlQueryBuilder.create()
}

internal fun sqlForImageIds(imageType: String? = null): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("image")
    sqlQueryBuilder.columns(arrayOf("image_id"))
    imageType?.let { sqlQueryBuilder.appendSelection(selection = "image_types LIKE '%|$it|%'") }
    return sqlQueryBuilder.create()
}

internal fun sqlForConsents(providerId: Long? = null, providerAccountId: Long? = null, status: ConsentStatus? = null): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("consent")

    providerId?.let { sqlQueryBuilder.appendSelection(selection = "provider_id = $it") }
    providerAccountId?.let { sqlQueryBuilder.appendSelection(selection = "provider_account_id = $it") }
    status?.let { sqlQueryBuilder.appendSelection(selection = "status = '${ it.name }'") }

    return sqlQueryBuilder.create()
}

internal fun sqlForContacts(paymentMethod: PaymentMethod? = null): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("contact")

    paymentMethod?.let { sqlQueryBuilder.appendSelection(selection = "payment_method = '${ it.name }'") }

    return sqlQueryBuilder.create()
}

internal fun sqlForContactIdsToGetStaleIds(
    before: Long? = null,
    after: Long? = null,
    paymentMethod: PaymentMethod? = null
): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("contact")
    sqlQueryBuilder.columns(arrayOf("contact_id"))

    before?.let { sqlQueryBuilder.appendSelection(selection = "contact_id > $it") }
    after?.let { sqlQueryBuilder.appendSelection(selection = "contact_id <= $it") }
    paymentMethod?.let { sqlQueryBuilder.appendSelection(selection = "payment_method = '${ it.name }'") }

    return sqlQueryBuilder.create()
}

internal fun sqlForCards(status: CardStatus? = null, accountId: Long? = null): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("card")

    status?.let { sqlQueryBuilder.appendSelection(selection = "status = '${ it.name }'") }
    accountId?.let { sqlQueryBuilder.appendSelection(selection = "account_id = $it") }

    return sqlQueryBuilder.create()
}
