/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.frollo.frollosdk.model.coredata.aggregation.provideraccounts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Additional details on what issue occurred with the account. Use this to provide instructions and context to the user. */
enum class AccountRefreshAdditionalStatus {

    /** Accept Splash. The user needs to login to the Provider website and dismiss a modal popup */
    @SerializedName("accept_splash") ACCEPT_SPLASH,

    /** Accept Terms & Conditions. The user needs to login to the Provider website and accept the update terms & conditions */
    @SerializedName("accept_terms_conditions") ACCEPT_TERMS_CONDITIONS,

    /** Account Closed. The account has been closed so no data is available. */
    @SerializedName("account_closed") ACCOUNT_CLOSED,

    /** Account Locked. The account has been locked and the user will need to resolve this with the Provider */
    @SerializedName("account_locked") ACCOUNT_LOCKED,

    /** Account Not Found. No accounts were found when logging into the Provider */
    @SerializedName("account_not_found") ACCOUNT_NOT_FOUND,

    /** Account Not Supported. The account found on the Provider is not supported currently. */
    @SerializedName("account_not_supported") ACCOUNT_NOT_SUPPORTED,

    /** Additional Login. Additional information needed from the user. Usually MFA or captcha. */
    @SerializedName("additional_login") ADDITIONAL_LOGIN,

    /** Aggregation Beta. Refresh may be incomplete or failed as work to support the Provider is still in progress. Ensure user leaves the account in this state to help the process of adding an account */
    @SerializedName("aggregation_beta") AGGREGATION_BETA,

    /** Aggregation Error. The aggregator encountered an issue refreshing the site. This should get automatically resolved. */
    @SerializedName("aggregation_error") AGGREGATION_ERROR,

    /** Invalid Credentials. The user needs to provide login details again. */
    @SerializedName("invalid_credentials") INVALID_CREDENTIALS,

    /** Invalid Language. The Provider account is set to an unsupported language by the aggregator. */
    @SerializedName("invalid_language") INVALID_LANGUAGE,

    /** Login Cancelled. Login process was cancelled by the user. Try again. */
    @SerializedName("login_cancelled") LOGIN_CANCELLED,

    /** Logout Required. The user is logged in at another location and will need to logout for the refresh to succeed. */
    @SerializedName("logout_required") LOGOUT_REQUIRED,

    /** MFA Enrollment Needed. MFA has not been setup by the user. This must be done through the Provider website */
    @SerializedName("mfa_enrollment_needed") MFA_ENROLLMENT_NEEDED,

    /** MFA Failed. The MFA login failed. Try again */
    @SerializedName("mfa_failed") MFA_FAILED,

    /** MFA Invalid Token. The user provided an invalid MFA token and will need to login again */
    @SerializedName("mfa_invalid_token") MFA_INVALID_TOKEN,

    /** MFA Needed. User needs to provide details for the MFA login form on [ProviderAccount] */
    @SerializedName("mfa_needed") MFA_NEEDED,

    /** MFA Timeout. User did not respond to MFA request quick enough. Consider displaying ProviderLoginForm.mfaTimeout to the user */
    @SerializedName("mfa_timeout") MFA_TIMEOUT,

    /** Password Expired. The user needs to visit the Provider website and update their password, then link to the app again. */
    @SerializedName("password_expired") PASSWORD_EXPIRED,

    /** Registration Duplicate */
    @SerializedName("registration_duplicate") REGISTRATION_DUPLICATE,

    /** Registration Failed. The user failed to register with the Provider properly */
    @SerializedName("registration_failed") REGISTRATION_FAILED,

    /** Registration Incomplete. The user hasn't completed their account setup with the Provider properly */
    @SerializedName("registration_incomplete") REGISTRATION_INCOMPLETE,

    /** Registration Invalid. The user's account with the Provider has an issue that needs to be rectified on their website */
    @SerializedName("registration_invalid") REGISTRATION_INVALID,

    /** Site Closed. The Provider website has shutdown and is no longer supported. */
    @SerializedName("site_closed") SITE_CLOSED,

    /** Site Error. An error occurred at the Provider website and will need to be resolved by the provider themselves */
    @SerializedName("site_error") SITE_ERROR,

    /** Site Unsupported. The Provider site is no longer supported */
    @SerializedName("site_unsupported") SITE_UNSUPPORTED,

    /** Unknown Error */
    @SerializedName("unknown_error") UNKNOWN_ERROR,

    /** Verify Credentials. The user need to visit the Provider website to confirm their credentials. */
    @SerializedName("verify_credentials") VERIFY_CREDENTIALS,

    /** Verify Personal Details. The user need to visit the Provider website to confirm their personal details. */
    @SerializedName("verify_personal_details") VERIFY_PERSONAL_DETAILS;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}