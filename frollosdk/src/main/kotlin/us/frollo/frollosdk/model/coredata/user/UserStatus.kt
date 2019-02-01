package us.frollo.frollosdk.model.coredata.user

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Status indicating the current state of the user in the onboarding and setting up process. */
enum class UserStatus {
    /** The user has registered but not yet completed any setup activities such as adding an aggregation account. */
    @SerializedName("registered") REGISTERED,
    /** An aggregation account has been added */
    @SerializedName("account_added") ACCOUNT_ADDED,
    /** The user has connected an aggregation account and there is now enough data for the user to setup their budget. */
    @SerializedName("budget_ready") BUDGET_READY,
    /** The user has completed all setup activities and is now fully active. This includes adding an account and setting a budget */
    @SerializedName("active") ACTIVE,
    /** The user is inactive as they have previously added an account but now have no aggregation accounts linked. Similar to [REGISTERED] */
    @SerializedName("inactive") INACTIVE;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}