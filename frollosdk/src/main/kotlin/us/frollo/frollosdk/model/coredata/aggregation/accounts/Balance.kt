package us.frollo.frollosdk.model.coredata.aggregation.accounts

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class Balance(
        @SerializedName("amount") val amount: BigDecimal,
        @SerializedName("currency") val currency: String
)