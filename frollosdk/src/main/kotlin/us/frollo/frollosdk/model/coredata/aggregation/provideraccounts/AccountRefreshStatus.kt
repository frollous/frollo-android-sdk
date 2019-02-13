package us.frollo.frollosdk.model.coredata.aggregation.provideraccounts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

enum class AccountRefreshStatus {
    @SerializedName("success") SUCCESS,
    @SerializedName("adding") ADDING,
    @SerializedName("updating") UPDATING,
    @SerializedName("needs_action") NEEDS_ACTION,
    @SerializedName("failed") FAILED;

    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}