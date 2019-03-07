package us.frollo.frollosdk.model.coredata.aggregation.accounts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Status of the account according to the Provider */
enum class AccountStatus {

    /** Account is fully active */
    @SerializedName("active") ACTIVE,

    /** Account is inactive (usually no transactions or other information) */
    @SerializedName("inactive") INACTIVE,

    /** Account is about to be closed */
    @SerializedName("to_be_closed") TO_BE_CLOSED,

    /** Account has been closed */
    @SerializedName("closed") CLOSED;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}