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
import us.frollo.frollosdk.model.api.aggregation.merchants.MerchantResponse
import us.frollo.frollosdk.model.api.aggregation.tags.TransactionTagResponse
import us.frollo.frollosdk.network.api.AggregationAPI
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionResponse
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionsSummaryResponse
import us.frollo.frollosdk.model.api.bills.BillPaymentResponse
import us.frollo.frollosdk.model.api.goals.GoalResponse
import us.frollo.frollosdk.model.api.reports.AccountBalanceReportResponse
import us.frollo.frollosdk.model.api.reports.ReportsResponse
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountType
import us.frollo.frollosdk.model.coredata.goals.GoalStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingStatus
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.coredata.reports.TransactionReportPeriod
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.model.coredata.surveys.Survey
import us.frollo.frollosdk.network.api.BillsAPI
import us.frollo.frollosdk.network.api.GoalsAPI
import us.frollo.frollosdk.network.api.ReportsAPI
import us.frollo.frollosdk.network.api.SurveysAPI

// Aggregation

internal fun AggregationAPI.fetchTransactionsByQuery(
    fromDate: String, // yyyy-MM-dd
    toDate: String, // yyyy-MM-dd
    accountIds: LongArray? = null,
    accountIncluded: Boolean? = null, // TODO: not using this currently as this requires refactoring handleTransactionsResponse
    transactionIncluded: Boolean? = null,
    skip: Int? = null,
    count: Int? = null
): Call<List<TransactionResponse>> {

    val queryMap = mutableMapOf("from_date" to fromDate, "to_date" to toDate)
    skip?.let { queryMap.put("skip", it.toString()) }
    count?.let { queryMap.put("count", it.toString()) }
    accountIncluded?.let { queryMap.put("account_included", it.toString()) }
    transactionIncluded?.let { queryMap.put("transaction_included", it.toString()) }
    accountIds?.let { queryMap.put("account_ids", it.joinToString(",")) }

    return fetchTransactions(queryMap)
}

internal fun AggregationAPI.fetchTransactionsByIDs(transactionIds: LongArray): Call<List<TransactionResponse>> =
        fetchTransactions(mapOf("transaction_ids" to transactionIds.joinToString(",")))

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

internal fun AggregationAPI.fetchMerchantsByIDs(merchantIds: LongArray): Call<List<MerchantResponse>> =
        fetchMerchantsByIds(mapOf("merchant_ids" to merchantIds.joinToString(",")))

internal fun AggregationAPI.transactionSearch(
    searchTerm: String,
    fromDate: String? = null, // yyyy-MM-dd
    toDate: String? = null, // yyyy-MM-dd
    accountIds: LongArray? = null,
    accountIncluded: Boolean? = null,
    transactionIncluded: Boolean? = null,
    skip: Int? = null,
    count: Int? = null
): Call<List<TransactionResponse>> {

    val queryMap = mutableMapOf("search_term" to searchTerm)
    fromDate?.let { queryMap.put("from_date", it) }
    toDate?.let { queryMap.put("to_date", it) }
    skip?.let { queryMap.put("skip", it.toString()) }
    count?.let { queryMap.put("count", it.toString()) }
    accountIncluded?.let { queryMap.put("account_included", it.toString()) }
    transactionIncluded?.let { queryMap.put("transaction_included", it.toString()) }
    accountIds?.let { queryMap.put("account_ids", it.joinToString(",")) }

    return transactionSearch(queryMap)
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

internal fun ReportsAPI.fetchReports(
    grouping: ReportGrouping,
    period: TransactionReportPeriod,
    fromDate: String,
    toDate: String,
    budgetCategory: BudgetCategory? = null,
    transactionTag: String? = null,
    categoryId: Long? = null,
    merchantId: Long? = null
): Call<ReportsResponse> {
    val queryMap = mutableMapOf("grouping" to grouping.toString(), "period" to period.toString(), "from_date" to fromDate, "to_date" to toDate)

    return when (grouping) {
        ReportGrouping.CATEGORY -> {
            categoryId?.let { id ->
                fetchReportsByCategory(id, queryMap)
            } ?: fetchReportsByCategory(queryMap)
        }
        ReportGrouping.MERCHANT -> {
            merchantId?.let { id ->
                fetchReportsByMerchant(id, queryMap)
            } ?: fetchReportsByMerchant(queryMap)
        }
        ReportGrouping.BUDGET_CATEGORY -> {
            budgetCategory?.let { bCategory ->
                fetchReportsByBudgetCategory(bCategory.budgetCategoryId, queryMap)
            } ?: fetchReportsByBudgetCategory(queryMap)
        }
        ReportGrouping.TAG -> {
            transactionTag?.let { tag ->
                fetchReportsByTag(tag, queryMap)
            } ?: fetchReportsByTag(queryMap)
        }
    }
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