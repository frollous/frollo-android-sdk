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

import retrofit2.Call
import us.frollo.frollosdk.model.api.aggregation.merchants.MerchantsResponse
import us.frollo.frollosdk.model.api.aggregation.tags.TransactionTagResponse
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionResponseWrapper
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionsSummaryResponse
import us.frollo.frollosdk.model.api.bills.BillPaymentResponse
import us.frollo.frollosdk.model.api.budgets.BudgetPeriodResponse
import us.frollo.frollosdk.model.api.budgets.BudgetResponse
import us.frollo.frollosdk.model.api.goals.GoalResponse
import us.frollo.frollosdk.model.api.reports.AccountBalanceReportResponse
import us.frollo.frollosdk.model.api.reports.ReportsResponse
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountType
import us.frollo.frollosdk.model.coredata.aggregation.transactions.Transaction
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionFilter
import us.frollo.frollosdk.model.coredata.budgets.BudgetType
import us.frollo.frollosdk.model.coredata.goals.GoalStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingStatus
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.coredata.reports.TransactionReportPeriod
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.model.coredata.surveys.Survey
import us.frollo.frollosdk.network.api.AggregationAPI
import us.frollo.frollosdk.network.api.BillsAPI
import us.frollo.frollosdk.network.api.BudgetsAPI
import us.frollo.frollosdk.network.api.GoalsAPI
import us.frollo.frollosdk.network.api.ReportsAPI
import us.frollo.frollosdk.network.api.SurveysAPI

// Aggregation

internal fun AggregationAPI.fetchTransactions(transactionFilter: TransactionFilter): Call<TransactionResponseWrapper> {
    val queryMap = mutableMapOf<String, String>()
    transactionFilter.after?.let { queryMap.put("after", transactionFilter.after) }
    transactionFilter.searchTerm?.let { queryMap.put("search_term", it) }
    transactionFilter.merchantIds?.let { queryMap.put("merchant_ids", it.joinToString(",")) }
    transactionFilter.accountIds?.let { queryMap.put("account_ids", it.joinToString(",")) }
    transactionFilter.transactionCategoryIds?.let { queryMap.put("transaction_category_ids", it.joinToString(",")) }
    transactionFilter.transactionIds?.let { queryMap.put("transaction_ids", it.joinToString(",")) }
    transactionFilter.budgetCategory?.let { queryMap.put("budget_category", it.toString()) }
    transactionFilter.minAmount?.let { queryMap.put("min_amount", it.toString()) }
    transactionFilter.maxAmount?.let { queryMap.put("max_amount", it.toString()) }
    transactionFilter.baseType?.let { queryMap.put("base_type", it.toString()) }
    transactionFilter.status?.let { queryMap.put("status", it.toString()) }
    transactionFilter.tags?.let { queryMap.put("tags", it.joinToString(",")) }
    transactionFilter.accountIncluded?.let { queryMap.put("account_included", it.toString()) }
    transactionFilter.transactionIncluded?.let { queryMap.put("transaction_included", it.toString()) }
    transactionFilter.fromDate?.let { queryMap.put("from_date", it) }?.toLocalDate(Transaction.DATE_FORMAT_PATTERN)
    transactionFilter.toDate?.let { queryMap.put("to_date", it) }?.toLocalDate(Transaction.DATE_FORMAT_PATTERN)
    transactionFilter.size?.let { queryMap.put("size", it.toString()) }
    return fetchTransactions(queryMap)
}

internal fun AggregationAPI.fetchTransactionsSummaryByQuery(
    fromDate: String, // yyyy-MM-dd
    toDate: String, // yyyy-MM-dd
    accountIds: LongArray? = null,
    accountIncluded: Boolean? = null,
    transactionIncluded: Boolean? = null
): Call<TransactionsSummaryResponse> {

    val queryMap = mutableMapOf("from_date" to fromDate, "to_date" to toDate)
    accountIncluded?.let { queryMap.put("account_included", it.toString()) }
    transactionIncluded?.let { queryMap.put("transaction_included", it.toString()) }
    accountIds?.let { queryMap.put("account_ids", it.joinToString(",")) }

    return fetchTransactionsSummary(queryMap)
}

internal fun AggregationAPI.fetchTransactionsSummaryByIDs(transactionIds: LongArray): Call<TransactionsSummaryResponse> =
    fetchTransactionsSummary(mapOf("transaction_ids" to transactionIds.joinToString(",")))

internal fun AggregationAPI.fetchMerchants(before: Long? = null, after: Long? = null, size: Long? = null, merchantIds: LongArray? = null): Call<MerchantsResponse> {
    val queryMap = mutableMapOf<String, String>()
    before?.let { queryMap.put("before", it.toString()) }
    after?.let { queryMap.put("after", it.toString()) }
    size?.let { queryMap.put("size", it.toString()) }
    merchantIds?.let { queryMap.put("merchant_ids", it.joinToString(",")) }
    return fetchMerchants(queryMap)
}

internal fun AggregationAPI.fetchUserTags(
    searchTerm: String? = null,
    sort: String? = null,
    order: String? = null
): Call<List<TransactionTagResponse>> {

    val queryMap = mutableMapOf<String, String>()
    searchTerm?.let { queryMap.put("search_term", it) }
    sort?.let { queryMap.put("sort", it) }
    order?.let { queryMap.put("order", it) }
    return fetchUserTags(queryMap)
}

internal fun AggregationAPI.fetchSuggestedTags(
    searchTerm: String? = null,
    sort: String? = null,
    order: String? = null
): Call<List<TransactionTagResponse>> {

    val queryMap = mutableMapOf<String, String>()
    searchTerm?.let { queryMap.put("search_term", it) }
    sort?.let { queryMap.put("sort", it) }
    order?.let { queryMap.put("order", it) }
    return fetchSuggestedTags(queryMap)
}

// Reports
internal fun ReportsAPI.fetchAccountBalanceReports(
    period: ReportPeriod,
    fromDate: String,
    toDate: String,
    accountId: Long? = null,
    accountType: AccountType? = null
): Call<AccountBalanceReportResponse> {

    val queryMap = mutableMapOf("period" to period.toString(), "from_date" to fromDate, "to_date" to toDate)
    accountType?.let { queryMap.put("container", it.toString()) }
    accountId?.let { queryMap.put("account_id", it.toString()) }
    return fetchAccountBalanceReports(queryMap)
}

internal fun ReportsAPI.fetchTransactionCategoryReports(
    period: TransactionReportPeriod,
    fromDate: String,
    toDate: String,
    categoryId: Long? = null,
    grouping: ReportGrouping? = null
): Call<ReportsResponse> {

    val queryMap = mutableMapOf("period" to period.toString(), "from_date" to fromDate, "to_date" to toDate)
    grouping?.let { queryMap.put("grouping", it.toString()) }
    return categoryId?.let { id ->
        fetchReportsByCategory(id, queryMap)
    } ?: fetchReportsByCategory(queryMap)
}

internal fun ReportsAPI.fetchMerchantReports(
    period: TransactionReportPeriod,
    fromDate: String,
    toDate: String,
    merchantId: Long? = null,
    grouping: ReportGrouping? = null
): Call<ReportsResponse> {

    val queryMap = mutableMapOf("period" to period.toString(), "from_date" to fromDate, "to_date" to toDate)
    grouping?.let { queryMap.put("grouping", it.toString()) }
    return merchantId?.let { id ->
        fetchReportsByMerchant(id, queryMap)
    } ?: fetchReportsByMerchant(queryMap)
}

internal fun ReportsAPI.fetchBudgetCategoryReports(
    period: TransactionReportPeriod,
    fromDate: String,
    toDate: String,
    budgetCategory: BudgetCategory? = null,
    grouping: ReportGrouping? = null
): Call<ReportsResponse> {

    val queryMap = mutableMapOf("period" to period.toString(), "from_date" to fromDate, "to_date" to toDate)
    grouping?.let { queryMap.put("grouping", it.toString()) }
    return budgetCategory?.let { bCategory ->
        fetchReportsByBudgetCategory(bCategory.budgetCategoryId, queryMap)
    } ?: fetchReportsByBudgetCategory(queryMap)
}

internal fun ReportsAPI.fetchTagReports(
    period: TransactionReportPeriod,
    fromDate: String,
    toDate: String,
    transactionTag: String? = null,
    grouping: ReportGrouping? = null
): Call<ReportsResponse> {

    val queryMap = mutableMapOf("period" to period.toString(), "from_date" to fromDate, "to_date" to toDate)
    grouping?.let { queryMap.put("grouping", it.toString()) }
    return transactionTag?.let { tag ->
        fetchReportsByTag(tag, queryMap)
    } ?: fetchReportsByTag(queryMap)
}

// Bills

internal fun BillsAPI.fetchBillPayments(
    fromDate: String, // yyyy-MM-dd
    toDate: String, // yyyy-MM-dd
    skip: Int? = null,
    count: Int? = null
): Call<List<BillPaymentResponse>> {

    val queryMap = mutableMapOf("from_date" to fromDate, "to_date" to toDate)
    skip?.let { queryMap.put("skip", it.toString()) }
    count?.let { queryMap.put("count", it.toString()) }

    return fetchBillPayments(queryMap)
}

// Surveys

internal fun SurveysAPI.fetchSurvey(surveyKey: String, latest: Boolean? = null): Call<Survey> {
    val queryMap = mutableMapOf<String, String>()
    latest?.let { queryMap.put("latest", latest.toString()) }
    return fetchSurvey(surveyKey, queryMap)
}

// Goals

internal fun GoalsAPI.fetchGoals(status: GoalStatus? = null, trackingStatus: GoalTrackingStatus? = null): Call<List<GoalResponse>> {
    val queryMap = mutableMapOf<String, String>()
    status?.let { queryMap.put("status", status.toString()) }
    trackingStatus?.let { queryMap.put("tracking_status", trackingStatus.toString()) }
    return fetchGoals(queryMap)
}

// Budgets

internal fun BudgetsAPI.fetchBudgets(
    current: Boolean? = null,
    budgetType: BudgetType? = null
): Call<List<BudgetResponse>> {
    val queryMap = mutableMapOf<String, String>()
    current?.let { queryMap["current"] = it.toString() }
    budgetType?.let { queryMap["category_type"] = it.toString() }
    return fetchBudgets(queryMap)
}

internal fun BudgetsAPI.fetchBudgetPeriods(
    budgetId: Long,
    fromDate: String? = null,
    toDate: String? = null
): Call<List<BudgetPeriodResponse>> {
    val queryMap = mutableMapOf<String, String>()
    fromDate?.let { queryMap["from_date"] = it }
    toDate?.let { queryMap["to_date"] = it }
    return fetchBudgetPeriods(budgetId, queryMap)
}
