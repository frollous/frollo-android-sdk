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

/**
 * Authentication Callback
 *
 * Called by the authentication class to notify other parts of the SDK of token or authentication changes.
 * This must be implemented by all custom authentication implementations.
 */
interface AuthenticationCallback {

    /**
     * Notifies the SDK that authentication of the user is no longer valid and to reset itself
     *
     * This should be called when the user's authentication is no longer valid and no possible automated
     * re-authentication can be performed. For example when a refresh token has been revoked so no more
     * access tokens can be obtained.
     */
    fun authenticationReset()

    /**
     * Update the SDK with the latest access token
     * Allows the SDK to cache and use the latest access token available for API requests. This should
     * be called whenever the access token is refreshed or the user has authenticated and obtained a new
     * access token.
     *
     * @param accessToken: Current valid access token
     * @param expiry: Indicates the number of seconds since the Unix Epoch (UTC) when the access token expires so SDK can pre-emptively request a new one
     */
    fun saveAccessTokens(accessToken: String, expiry: Long)
}