package us.frollo.frollosdk.model.coredata.aggregation.transactions

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Status of the transaction's lifecycle */
enum class TransactionStatus {

    /** Pending. Transaction is authorised but not posted */
    @SerializedName("pending") PENDING,

    /** Posted. Transaction is complete */
    @SerializedName("posted") POSTED,

    /** Scheduled. Transaction is scheduled for the future */
    @SerializedName("scheduled") SCHEDULED;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}