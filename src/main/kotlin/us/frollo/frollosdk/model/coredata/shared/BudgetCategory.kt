package us.frollo.frollosdk.model.coredata.shared

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Indicates a budget type */
enum class BudgetCategory {

    /** Income budget */
    @SerializedName("income") INCOME,

    /** Living budget */
    @SerializedName("living") LIVING,

    /** Lifestyle budget */
    @SerializedName("lifestyle") LIFESTYLE,

    /** Savings budget */
    @SerializedName("goals") SAVINGS,

    /** One offs budget */
    @SerializedName("one_off") ONE_OFF;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}