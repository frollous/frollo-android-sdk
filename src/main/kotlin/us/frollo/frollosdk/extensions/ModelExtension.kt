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
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionBaseType
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionStatus
import us.frollo.frollosdk.model.coredata.bills.BillFrequency
import us.frollo.frollosdk.model.coredata.bills.BillPaymentStatus
import us.frollo.frollosdk.model.coredata.bills.BillStatus
import us.frollo.frollosdk.model.coredata.bills.BillType
import us.frollo.frollosdk.model.coredata.goals.GoalFrequency
import us.frollo.frollosdk.model.coredata.goals.GoalStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTarget
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingType
import us.frollo.frollosdk.model.coredata.messages.ContentType
import us.frollo.frollosdk.model.coredata.notifications.NotificationPayload
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.model.coredata.shared.OrderType
import us.frollo.frollosdk.model.coredata.user.User
import us.frollo.frollosdk.notifications.NotificationPayloadNames
import java.lang.StringBuilder

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
                driverLicense = driverLicense)

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

internal fun sqlForHistoryReports(
    fromDate: String,
    toDate: String,
    grouping: ReportGrouping,
    period: ReportPeriod,
    budgetCategory: BudgetCategory? = null,
    dates: Array<String>? = null,
    transactionTag: String? = null
): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("report_transaction_history")

    sqlQueryBuilder.appendSelection(selection = "(date BETWEEN '$fromDate' AND '$toDate')")
    sqlQueryBuilder.appendSelection(selection = "report_grouping = '${ grouping.name }'")
    sqlQueryBuilder.appendSelection(selection = "period = '${ period.name }'")
    val budgetCategorySelection = budgetCategory?.let { "filtered_budget_category = '${ it.name }'" } ?: run { "filtered_budget_category IS NULL" }
    sqlQueryBuilder.appendSelection(selection = budgetCategorySelection)
    val tagsSelection = transactionTag?.let { "transaction_tags = '$transactionTag'" } ?: run { "transaction_tags IS NULL" }
    sqlQueryBuilder.appendSelection(selection = tagsSelection)
    dates?.let { sqlQueryBuilder.appendSelection(selection = "date IN (${ it.joinToString(",") { "'$it'" } })") }

    return sqlQueryBuilder.create()
}

internal fun sqlForStaleHistoryReportIds(
    fromDate: String,
    toDate: String,
    grouping: ReportGrouping,
    period: ReportPeriod,
    budgetCategory: BudgetCategory? = null,
    dates: Array<String>? = null,
    transactionTag: String? = null
): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("report_transaction_history")

    sqlQueryBuilder.columns(arrayOf("report_id"))

    sqlQueryBuilder.appendSelection(selection = "(date BETWEEN '$fromDate' AND '$toDate')")
    sqlQueryBuilder.appendSelection(selection = "report_grouping = '${ grouping.name }'")
    sqlQueryBuilder.appendSelection(selection = "period = '${ period.name }'")
    val budgetCategorySelection = budgetCategory?.let { "filtered_budget_category = '${ it.name }'" } ?: run { "filtered_budget_category IS NULL" }
    sqlQueryBuilder.appendSelection(selection = budgetCategorySelection)
    val tagsSelection = transactionTag?.let { "transaction_tags = '$transactionTag'" } ?: run { "transaction_tags IS NULL" }
    sqlQueryBuilder.appendSelection(selection = tagsSelection)
    dates?.let { sqlQueryBuilder.appendSelection(selection = "date NOT IN (${ it.joinToString(",") { "'$it'" } })") }

    return sqlQueryBuilder.create()
}

internal fun sqlForTransactionStaleIds(fromDate: String, toDate: String, accountIds: LongArray? = null, transactionIncluded: Boolean? = null): SimpleSQLiteQuery {
    val sb = StringBuilder()

    accountIds?.let { sb.append(" AND account_id IN (${accountIds.joinToString(",")}) ") }

    transactionIncluded?.let { sb.append(" AND included = ${ it.toInt() } ") }

    val query = "SELECT transaction_id FROM transaction_model " +
                "WHERE ((transaction_date BETWEEN Date('$fromDate') AND Date('$toDate')) $sb)"

    return SimpleSQLiteQuery(query)
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

internal fun sqlForTransactions(
    accountId: Long? = null,
    userTags: List<String>? = null,
    baseType: TransactionBaseType? = null,
    budgetCategory: BudgetCategory? = null,
    status: TransactionStatus? = null,
    included: Boolean? = null,
    fromDate: String? = null,
    toDate: String? = null,
    externalId: String? = null
): SimpleSQLiteQuery {
    val sqlQueryBuilder = SimpleSQLiteQueryBuilder("transaction_model")

    accountId?.let { sqlQueryBuilder.appendSelection(selection = "account_id = $it") }
    if (userTags != null && userTags.isNotEmpty()) {
        val sb = StringBuilder()
        sb.append("(")
        userTags.forEachIndexed { index, str ->
            sb.append("(user_tags LIKE '%|$str|%')")
            if (index < userTags.size - 1) sb.append(" AND ")
        }
        sb.append(")")
        sqlQueryBuilder.appendSelection(selection = sb.toString())
    }
    baseType?.let { sqlQueryBuilder.appendSelection(selection = "base_type = '${ it.name }'") }
    budgetCategory?.let { sqlQueryBuilder.appendSelection(selection = "budget_category = '${ it.name }'") }
    status?.let { sqlQueryBuilder.appendSelection(selection = "status = '${ it.name }'") }
    included?.let { sqlQueryBuilder.appendSelection(selection = "included = ${ it.toInt() }") }
    externalId?.let { sqlQueryBuilder.appendSelection(selection = "external_id = '$it'") }
    ifNotNull(fromDate, toDate) { from, to -> sqlQueryBuilder.appendSelection(selection = "(transaction_date BETWEEN Date('$from') AND Date('$to'))") }

    return sqlQueryBuilder.create()
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
                getString(NotificationPayloadNames.USER_MESSAGE_ID.toString()))

internal fun Map<String, String>.toNotificationPayload(): NotificationPayload =
        createNotificationPayload(
                get(NotificationPayloadNames.EVENT.toString()),
                get(NotificationPayloadNames.LINK.toString()),
                get(NotificationPayloadNames.TRANSACTION_IDS.toString()),
                get(NotificationPayloadNames.USER_EVENT_ID.toString()),
                get(NotificationPayloadNames.USER_MESSAGE_ID.toString()))

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
                userMessageID = userMessageID?.trim()?.toLong())

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