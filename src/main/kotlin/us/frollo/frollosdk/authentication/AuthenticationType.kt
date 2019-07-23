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

package us.frollo.frollosdk.authentication

import android.net.Uri

/**
 * Authentication method to be used by the SDK. Includes authentication specific parameters
 */
sealed class AuthenticationType {
    /**
     * Custom - provide a custom authentication class managed externally from the SDK
     *
     * @param authentication: Custom authentication method. See [OAuth2Authentication] for a default implementation.
     */
    class Custom(val authentication: Authentication) : AuthenticationType()

    /**
     * OAuth2 - generic OAuth2 based authentication
     *
     * @param redirectUrl OAuth2 Redirection URL. URL to redirect to after the authorization flow is complete. This should be a deep or universal link to the host app
     * @param authorizationUrl URL of the OAuth2 authorization endpoint for web based login
     * @param tokenUrl URL of the OAuth2 token endpoint for getting tokens and native login
     * @param revokeTokenURL OAuth2 token endpoint to revoke refresh token on logout (if supported)
     */
    class OAuth2(
        val redirectUrl: String,
        val authorizationUrl: String,
        val tokenUrl: String,
        val revokeTokenURL: String? = null
    ) : AuthenticationType() {

        /**
         * OAuth2 Redirection URL. URL to redirect to after the authorization flow is complete. This should be a deep or universal link to the host app
         */
        val redirectUri: Uri = Uri.parse(redirectUrl)
        /**
         * URL of the OAuth2 authorization endpoint for web based login
         */
        val authorizationUri: Uri = Uri.parse(authorizationUrl)
        /**
         * URL of the OAuth2 token endpoint for getting tokens and native login
         */
        val tokenUri: Uri = Uri.parse(tokenUrl)
    }
}