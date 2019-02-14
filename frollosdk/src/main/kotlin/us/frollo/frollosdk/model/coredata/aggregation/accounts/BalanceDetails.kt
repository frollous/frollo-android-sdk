package us.frollo.frollosdk.model.coredata.aggregation.accounts

import com.google.gson.annotations.SerializedName

data class BalanceDetails(
        @SerializedName("current_description") val currentDescription: String,
        @SerializedName("tiers") val tiers: List<BalanceTier>
)