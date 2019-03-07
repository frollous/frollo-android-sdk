package us.frollo.frollosdk.model.coredata.aggregation.accounts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Main type of the account as determined by container */
enum class AccountType {

    /** Bank account */
    @SerializedName("bank") BANK,

    /** Biller account */
    @SerializedName("credit_card") CREDIT_CARD,

    /** Credit card */
    @SerializedName("investment") INVESTMENT,

    /** Credit Score */
    @SerializedName("loan") LOAN,

    /** Insurance */
    @SerializedName("bill") BILL,

    /** Investment */
    @SerializedName("insurance") INSURANCE,

    /** Loan */
    @SerializedName("reward") REWARD,

    /** Reward/Loyalty Account */
    @SerializedName("unknown") UNKNOWN,

    /** Unknown */
    @SerializedName("credit_score") CREDIT_SCORE;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
            //Try to get the annotation value if available instead of using plain .toString()
            //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}
