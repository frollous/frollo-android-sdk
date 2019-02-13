package us.frollo.frollosdk.model.coredata.aggregation.provideraccounts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

enum class AccountRefreshAdditionalStatus {
    @SerializedName("accept_splash") ACCEPT_SPLASH,
    @SerializedName("accept_terms_conditions") ACCEPT_TERMS_CONDITIONS,
    @SerializedName("account_closed") ACCOUNT_CLOSED,
    @SerializedName("account_locked") ACCOUNT_LOCKED,
    @SerializedName("account_not_found") ACCOUNT_NOT_FOUND,
    @SerializedName("account_not_supported") ACCOUNT_NOT_SUPPORTED,
    @SerializedName("additional_login") ADDITIONAL_LOGIN,
    @SerializedName("aggregation_beta") AGGREGATION_BETA,
    @SerializedName("aggregation_error") AGGREGATION_ERROR,
    @SerializedName("invalid_credentials") INVALID_CREDENTIALS,
    @SerializedName("invalid_language") INVALID_LANGUAGE,
    @SerializedName("login_cancelled") LOGIN_CANCELLED,
    @SerializedName("logout_required") LOGOUT_REQUIRED,
    @SerializedName("mfa_enrollment_needed") MFA_ENROLLMENT_NEEDED,
    @SerializedName("mfa_failed") MFA_FAILED,
    @SerializedName("mfa_invalid_token") MFA_INVALID_TOKEN,
    @SerializedName("mfa_needed") MFA_NEEDED,
    @SerializedName("mfa_timeout") MFA_TIMEOUT,
    @SerializedName("password_expired") PASSWORD_EXPIRED,
    @SerializedName("registration_duplicate") REGISTRATION_DUPLICATE,
    @SerializedName("registration_failed") REGISTRATION_FAILED,
    @SerializedName("registration_incomplete") REGISTRATION_INCOMPLETE,
    @SerializedName("registration_invalid") REGISTRATION_INVALID,
    @SerializedName("site_closed") SITE_CLOSED,
    @SerializedName("site_error") SITE_ERROR,
    @SerializedName("site_unsupported") SITE_UNSUPPORTED,
    @SerializedName("unknown_error") UNKNOWN_ERROR,
    @SerializedName("verify_credentials") VERIFY_CREDENTIALS,
    @SerializedName("verify_personal_details") VERIFY_PERSONAL_DETAILS;

    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}