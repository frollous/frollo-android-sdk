package us.frollo.frollosdk.model.coredata.aggregation.transactions

import java.math.BigDecimal

/** Transaction summary */
data class TransactionsSummary(

        /** Number of transactions */
        val count: Long,

        /** Sum of the transactions */
        val sum: BigDecimal
)