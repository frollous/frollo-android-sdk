package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

enum class ProviderContainerName {
    @SerializedName("bank") BANK,
    @SerializedName("credit_card") CREDIT_CARD,
    @SerializedName("investment") INVESTMENT,
    @SerializedName("loan") LOAN,
    @SerializedName("bill") BILL,
    @SerializedName("insurance") INSURANCE,
    @SerializedName("reward") REWARD,
    @SerializedName("credit_score") CREDIT_SCORE,
    @SerializedName("unknown") UNKNOWN;

    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}