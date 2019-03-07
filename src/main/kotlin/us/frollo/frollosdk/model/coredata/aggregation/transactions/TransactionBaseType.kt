package us.frollo.frollosdk.model.coredata.aggregation.transactions

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** The basic type of transaction */
enum class TransactionBaseType {

    /** Credit */
    @SerializedName("credit") CREDIT,

    /** Debit */
    @SerializedName("debit") DEBIT,

    /** Other */
    @SerializedName("other") OTHER,

    /** Unknown */
    @SerializedName("unknown") UNKNOWN;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}