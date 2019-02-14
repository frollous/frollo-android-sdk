package us.frollo.frollosdk.model.coredata.aggregation.accounts

import com.google.gson.annotations.SerializedName

data class BalanceTier(
        @SerializedName("description") val description: String,
        @SerializedName("min") val min: Int,
        @SerializedName("max") val max: Int
)