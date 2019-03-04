package us.frollo.frollosdk.extensions

import retrofit2.Call
import us.frollo.frollosdk.model.api.aggregation.merchants.MerchantResponse
import us.frollo.frollosdk.network.api.AggregationAPI
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionResponse
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionsSummaryResponse

internal fun AggregationAPI.fetchTransactionsByQuery(
        fromDate: String, // yyyy-MM-dd
        toDate: String, // yyyy-MM-dd
        accountIds: LongArray? = null,
        accountIncluded: Boolean? = null, // TODO: not using this currently as this requires refactoring handleTransactionsResponse
        transactionIncluded: Boolean? = null,
        skip: Int? = null,
        count: Int? = null
) : Call<List<TransactionResponse>> {

    var queryMap = mapOf("from_date" to fromDate, "to_date" to toDate)
    skip?.let { queryMap = queryMap.plus(Pair("skip", it.toString())) }
    count?.let { queryMap = queryMap.plus(Pair("count", it.toString())) }
    accountIncluded?.let { queryMap = queryMap.plus(Pair("account_included", it.toString())) }
    transactionIncluded?.let { queryMap = queryMap.plus(Pair("transaction_included", it.toString())) }
    accountIds?.let { queryMap = queryMap.plus(Pair("account_ids", it.joinToString(","))) }

    return fetchTransactions(queryMap)
}

internal fun AggregationAPI.fetchTransactionsByIDs(transactionIds: LongArray) : Call<List<TransactionResponse>> =
        fetchTransactions(mapOf("transaction_ids" to transactionIds.joinToString(",")))

internal fun AggregationAPI.fetchTransactionsSummaryByQuery(
        fromDate: String, // yyyy-MM-dd
        toDate: String, // yyyy-MM-dd
        accountIds: LongArray? = null,
        accountIncluded: Boolean? = null,
        transactionIncluded: Boolean? = null
) : Call<TransactionsSummaryResponse> {

    var queryMap = mapOf("from_date" to fromDate, "to_date" to toDate)
    accountIncluded?.let { queryMap = queryMap.plus(Pair("account_included", it.toString())) }
    transactionIncluded?.let { queryMap = queryMap.plus(Pair("transaction_included", it.toString())) }
    accountIds?.let { queryMap = queryMap.plus(Pair("account_ids", it.joinToString(","))) }

    return fetchTransactionsSummary(queryMap)
}

internal fun AggregationAPI.fetchTransactionsSummaryByIDs(transactionIds: LongArray) : Call<TransactionsSummaryResponse> =
        fetchTransactionsSummary(mapOf("transaction_ids" to transactionIds.joinToString(",")))

internal fun AggregationAPI.fetchMerchantsByIDs(merchantIds: LongArray) : Call<List<MerchantResponse>> =
        fetchMerchantsByIds(mapOf("merchant_ids" to merchantIds.joinToString(",")))