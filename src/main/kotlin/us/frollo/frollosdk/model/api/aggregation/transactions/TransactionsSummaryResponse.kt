package us.frollo.frollosdk.model.api.aggregation.transactions

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

internal data class TransactionsSummaryResponse(
        @SerializedName("count") val count: Long,
        @SerializedName("sum") val sum: BigDecimal
)