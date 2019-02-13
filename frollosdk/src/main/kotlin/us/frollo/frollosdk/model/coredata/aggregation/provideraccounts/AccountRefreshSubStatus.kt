package us.frollo.frollosdk.model.coredata.aggregation.provideraccounts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

enum class AccountRefreshSubStatus {
    @SerializedName("success") SUCCESS,
    @SerializedName("partial_success") PARTIAL_SUCCESS,
    @SerializedName("input_required") INPUT_REQUIRED,
    @SerializedName("provider_site_action") PROVIDER_SITE_ACTION,
    @SerializedName("relogin_required") RELOGIN_REQUIRED,
    @SerializedName("temporary_failure") TEMPORARY_FAILURE,
    @SerializedName("permanent_failure") PERMANENT_FAILURE,
    @SerializedName("email_required") EMAIL_REQUIRED,
    @SerializedName("last_name_required") LAST_NAME_REQUIRED;

    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}