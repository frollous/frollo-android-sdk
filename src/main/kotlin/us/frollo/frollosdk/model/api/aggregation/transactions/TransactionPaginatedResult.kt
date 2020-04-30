package us.frollo.frollosdk.model.api.aggregation.transactions

import us.frollo.frollosdk.error.FrolloSDKError

/** Result SUCCESS or ERROR with transactionPaginationInfo **/
sealed class TransactionPaginatedResult {
    class Success(
            val transactionPaginationInfo: TransactionPaginationInfo? = null
            ) : TransactionPaginatedResult()

    class Error(
            val error: FrolloSDKError? = null
    ) : TransactionPaginatedResult()
}