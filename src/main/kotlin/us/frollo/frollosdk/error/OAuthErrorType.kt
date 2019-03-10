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
import us.frollo.frollosdk.R

/**
 * Type of error that has occurred during authorization
 */
enum class OAuthErrorType(
        /** Localized string resource id */
        @StringRes val textResource: Int) {

    /** Access denied */
    ACCESS_DENIED(R.string.FrolloSDK_Error_OAuth_AccessDenied),

    /** Client error */
    CLIENT_ERROR(R.string.FrolloSDK_Error_OAuth_BrowserError),

    /** Invalid client */
    INVALID_CLIENT(R.string.FrolloSDK_Error_OAuth_ClientError),

    /** Invalid client metadata */
    INVALID_CLIENT_METADATA(R.string.FrolloSDK_Error_OAuth_InvalidClient),

    /** Invalid grant */
    INVALID_GRANT(R.string.FrolloSDK_Error_OAuth_InvalidGrant),

    /** Invalid redirect URL */
    INVALID_REDIRECT_URI(R.string.FrolloSDK_Error_OAuth_InvalidRedirectURI),

    /** Invalid request */
    INVALID_REQUEST(R.string.FrolloSDK_Error_OAuth_InvalidRequest),

    /** Invalid scope */
    INVALID_SCOPE(R.string.FrolloSDK_Error_OAuth_InvalidScope),

    /** Unauthorized client */
    UNAUTHORIZED_CLIENT(R.string.FrolloSDK_Error_OAuth_UnauthorizedClient),

    /** Unsupported grant type */
    UNSUPPORTED_GRANT_TYPE(R.string.FrolloSDK_Error_OAuth_UnsupportedGrantType),

    /** Unsupported response type */
    UNSUPPORTED_RESPONSE_TYPE(R.string.FrolloSDK_Error_OAuth_UnsupportedResponseType),

    /** The browser could not be opened */
    BROWSER_ERROR(R.string.FrolloSDK_Error_OAuth_BrowserError),

    /** A network error occurred during authentication */
    NETWORK_ERROR(R.string.FrolloSDK_Error_OAuth_NetworkError),

    /** A server error occurred during authentication */
    SERVER_ERROR(R.string.FrolloSDK_Error_OAuth_ServerError),

    /** User cancelled the authentication request */
    USER_CANCELLED(R.string.FrolloSDK_Error_OAuth_UserCancelled),

    /** An unknown issue with authorisation has occurred */
    OTHER_AUTHORISATION(R.string.FrolloSDK_Error_OAuth_OtherAuthorisation),

    /** Unknown error */
    UNKNOWN(R.string.FrolloSDK_Error_OAuth_Unknown);

    /** Enum to localized message */
    fun toLocalizedString(context: Context?, arg1: String? = null): String? =
            context?.resources?.getString(textResource, arg1)
}