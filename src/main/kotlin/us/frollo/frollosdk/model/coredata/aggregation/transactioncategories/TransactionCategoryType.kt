package us.frollo.frollosdk.model.coredata.aggregation.transactioncategories

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** High level type of the category */
enum class TransactionCategoryType {

    /** Credit Score Event Type */
    @SerializedName("credit_score") CREDIT_SCORE,

    /** Deferred compensation */
    @SerializedName("deferred_compensation") DEFERRED_COMPENSATION,

    /** Expense */
    @SerializedName("expense") EXPENSE,

    /** Income */
    @SerializedName("income") INCOME,

    /** Transfer. Internal or external financial transfer */
    @SerializedName("transfer") TRANSFER,

    /** Uncategorized */
    @SerializedName("uncategorize") UNCATEGORIZED,

    /** Unknown. Transaction category is not recognised */
    @SerializedName("unknown") UNKNOWN;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}