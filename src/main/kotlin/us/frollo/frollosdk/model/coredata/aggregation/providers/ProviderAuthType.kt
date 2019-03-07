package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** How the provider performs authentication. */
enum class ProviderAuthType {

    /** OAuth. Using OAuth2 authentication */
    @SerializedName("oauth") OAUTH,

    /** Credentials. Using the user's login credentials */
    @SerializedName("credentials") CREDENTIALS,

    /** MFA Credentials. Using the user's MFA and login credentials */
    @SerializedName("mfa_credentials") MFA_CREDENTIALS,

    /** Unknown */
    @SerializedName("unknown") UNKNOWN;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}