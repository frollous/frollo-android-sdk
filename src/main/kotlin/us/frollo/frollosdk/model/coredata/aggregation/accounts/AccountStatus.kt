package us.frollo.frollosdk.model.coredata.aggregation.accounts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

enum class AccountStatus {
    @SerializedName("active") ACTIVE,
    @SerializedName("inactive") INACTIVE,
    @SerializedName("to_be_closed") TO_BE_CLOSED,
    @SerializedName("closed") CLOSED;

    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}