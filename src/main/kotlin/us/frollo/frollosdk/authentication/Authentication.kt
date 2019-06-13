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

import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener

/**
 * Manages authentication, login, registration, logout and the user profile.
 */
abstract class Authentication {
    /**
     * Indicates if the user is currently authorised with Frollo
     */
    abstract val loggedIn: Boolean

    /**
     * SDK callback to be called to update SDK about authentication events. SDK sets this as part of setup
     */
    abstract var authenticationCallback: AuthenticationCallback?

    /**
     * Refresh Access Token
     *
     * Forces a refresh of the access tokens if a 401 was encountered. For advanced usage only in combination with web request authentication.
     *
     * @param completion Completion handler with any error that occurred (Optional)
     */
    abstract fun refreshTokens(completion: OnFrolloSDKCompletionListener<Result>? = null)

    /**
     * Logout the user if possible and then reset and clear local caches
     */
    abstract fun logout()

    /**
     * Resets any token cache etc and logout the user locally
     */
    abstract fun reset()
}