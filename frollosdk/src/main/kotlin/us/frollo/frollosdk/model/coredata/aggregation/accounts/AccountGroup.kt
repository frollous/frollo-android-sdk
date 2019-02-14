package us.frollo.frollosdk.model.coredata.aggregation.accounts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

enum class AccountGroup {
    @SerializedName("bank") BANK,
    @SerializedName("savings") SAVINGS,
    @SerializedName("credit_card") CREDIT_CARD,
    @SerializedName("super_annuation") SUPER_ANNUATION,
    @SerializedName("investment") INVESTMENT,
    @SerializedName("loan") LOAN,
    @SerializedName("insurance") INSURANCE,
    @SerializedName("reward") REWARD,
    @SerializedName("score") SCORE,
    @SerializedName("custom") CUSTOM,
    @SerializedName("other") OTHER;

    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}


