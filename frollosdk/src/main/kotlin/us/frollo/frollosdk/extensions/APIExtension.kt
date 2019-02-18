package us.frollo.frollosdk.extensions

import retrofit2.Call
import us.frollo.frollosdk.data.remote.api.AggregationAPI
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionResponse

internal fun AggregationAPI.fetchTransactionsByQuery(
        fromDate: String, // yyyy-MM-dd
        toDate: String, // yyyy-MM-dd
        accountIds: LongArray? = null,
        accountIncluded: Boolean? = null,
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