package us.frollo.frollosdk.model.coredata.aggregation.transactions

import java.math.BigDecimal

data class TransactionsSummary(
        val count: Long,
        val sum: BigDecimal
)