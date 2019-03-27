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

package us.frollo.frollosdk.error

import android.content.Context
import androidx.annotation.StringRes
import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.R
import us.frollo.frollosdk.extensions.serializedName

/**
 * Type of error that has occurred during authorization
 */
enum class OAuth2ErrorType(
        /** Localized string resource id */
        @StringRes val textResource: Int) {

    /** Access denied */
    @SerializedName("access_denied") ACCESS_DENIED(R.string.FrolloSDK_Error_OAuth_AccessDenied),

    /** Client error */
    @SerializedName("client_error") CLIENT_ERROR(R.string.FrolloSDK_Error_OAuth_BrowserError),

    /** Invalid client */
    @SerializedName("invalid_client") INVALID_CLIENT(R.string.FrolloSDK_Error_OAuth_ClientError),

    /** Invalid client metadata */
    @SerializedName("invalid_client_metadata") INVALID_CLIENT_METADATA(R.string.FrolloSDK_Error_OAuth_InvalidClient),

    /** Invalid grant */
    @SerializedName("invalid_grant") INVALID_GRANT(R.string.FrolloSDK_Error_OAuth_InvalidGrant),

    /** Invalid redirect URL */
    @SerializedName("invalid_redirect_uri") INVALID_REDIRECT_URI(R.string.FrolloSDK_Error_OAuth_InvalidRedirectURI),

    /** Invalid request */
    @SerializedName("invalid_request") INVALID_REQUEST(R.string.FrolloSDK_Error_OAuth_InvalidRequest),

    /** Invalid scope */
    @SerializedName("invalid_scope") INVALID_SCOPE(R.string.FrolloSDK_Error_OAuth_InvalidScope),

    /** Unauthorized client */
    @SerializedName("unauthorized_client") UNAUTHORIZED_CLIENT(R.string.FrolloSDK_Error_OAuth_UnauthorizedClient),

    /** Unsupported grant type */
    @SerializedName("unsupported_grant_type") UNSUPPORTED_GRANT_TYPE(R.string.FrolloSDK_Error_OAuth_UnsupportedGrantType),

    /** Unsupported response type */
    @SerializedName("unsupported_response_type") UNSUPPORTED_RESPONSE_TYPE(R.string.FrolloSDK_Error_OAuth_UnsupportedResponseType),

    /** The browser could not be opened */
    @SerializedName("browser_error") BROWSER_ERROR(R.string.FrolloSDK_Error_OAuth_BrowserError),

    /** A network error occurred during authentication */
    @SerializedName("network_error") NETWORK_ERROR(R.string.FrolloSDK_Error_OAuth_NetworkError),

    /** A server error occurred during authentication */
    @SerializedName("server_error") SERVER_ERROR(R.string.FrolloSDK_Error_OAuth_ServerError),

    /** User cancelled the authentication request */
    @SerializedName("user_cancelled") USER_CANCELLED(R.string.FrolloSDK_Error_OAuth_UserCancelled),

    /** An unknown issue with authorisation has occurred */
    @SerializedName("other_authorisation") OTHER_AUTHORISATION(R.string.FrolloSDK_Error_OAuth_OtherAuthorisation),

    /** Unknown error */
    @SerializedName("unknown") UNKNOWN(R.string.FrolloSDK_Error_OAuth_Unknown);

    /** Enum to localized message */
    fun toLocalizedString(context: Context?, arg1: String? = null): String? =
            context?.resources?.getString(textResource, arg1)

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}