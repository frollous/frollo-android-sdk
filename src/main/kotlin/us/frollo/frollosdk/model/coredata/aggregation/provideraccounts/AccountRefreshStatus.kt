package us.frollo.frollosdk.model.coredata.aggregation.provideraccounts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** High level indication on the aggregator refresh of an account. Use this to determine if there's an issue. */
enum class AccountRefreshStatus {

    /** Success. The account was refreshed without issue */
    @SerializedName("success") SUCCESS,

    /** Adding. The user has just added the account and the aggregator is fetching data */
    @SerializedName("adding") ADDING,

    /** Updating. The account data is currently being updated. */
    @SerializedName("updating") UPDATING,

    /** Needs Action. The user needs to take an additional step to complete the latest update attempt. See [AccountRefreshSubStatus] */
    @SerializedName("needs_action") NEEDS_ACTION,

    /** Failed. The last update failed, the user may need to take an action or wait for the problem to be solved. See [AccountRefreshAdditionalStatus] */
    @SerializedName("failed") FAILED;


    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}