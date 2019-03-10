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

import net.openid.appauth.AuthorizationException
import us.frollo.frollosdk.mapping.toOAuthErrorType

/**
 * Represents errors that can be returned from the authorization flow
 */
class OAuthError(private val exception: AuthorizationException?) : FrolloSDKError(exception?.message) {

    /** Type of OAuth Error */
    val type : OAuthErrorType
        get() = exception?.toOAuthErrorType() ?: OAuthErrorType.UNKNOWN

    /** Localized description */
    override val localizedDescription: String?
        get() = type.toLocalizedString(context)

    /** Debug description */
    override val debugDescription: String?
        get() {
            var debug = "OAuthError: Type [${ type.name }] "
            debug = debug.plus(localizedDescription)
            return debug
        }
}