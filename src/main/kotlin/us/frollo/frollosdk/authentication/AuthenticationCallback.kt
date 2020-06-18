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
 * Called by the authentication class to notify the implementing app issues with authentication.
 * This must be implemented by custom authentication implementations.
 */
interface AuthenticationCallback {
    /**
     * Access Token Expired
     *
     * Alerts the authentication handler to an expired access token that needs refreshing
     */
    fun accessTokenExpired()

    /**
     * Access Token Invalid
     *
     * The host has rejected the access token and the user should be re-authenticated
     */
    fun tokenInvalidated()
}
