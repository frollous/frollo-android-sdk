package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Container names supported by the provider */
enum class ProviderContainerName {

    /** Bank */
    @SerializedName("bank") BANK,

    /** Credit card */
    @SerializedName("credit_card") CREDIT_CARD,

    /** Investment */
    @SerializedName("investment") INVESTMENT,

    /** Loan */
    @SerializedName("loan") LOAN,

    /** Bill */
    @SerializedName("bill") BILL,

    /** Insurance */
    @SerializedName("insurance") INSURANCE,

    /** Reward */
    @SerializedName("reward") REWARD,

    /** Credit score */
    @SerializedName("credit_score") CREDIT_SCORE,

    /** Unknown */
    @SerializedName("unknown") UNKNOWN;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}