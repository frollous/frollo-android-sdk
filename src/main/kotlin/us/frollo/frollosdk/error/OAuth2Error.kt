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
import us.frollo.frollosdk.mapping.toOAuth2ErrorResponse
import us.frollo.frollosdk.mapping.toOAuth2ErrorType
import us.frollo.frollosdk.model.oauth.OAuth2ErrorResponse

/**
 * Represents OAuth2 error that can be returned from the authentication server
 */
class OAuth2Error(private val exception: AuthorizationException? = null, response: String? = null) : FrolloSDKError(exception?.message ?: response) {

    private var oAuth2ErrorResponse: OAuth2ErrorResponse? = null

    init {
        oAuth2ErrorResponse = response?.toOAuth2ErrorResponse()
    }

    /** Type of OAuth2 Error */
    val type: OAuth2ErrorType
        get() = oAuth2ErrorResponse?.errorType ?: exception?.toOAuth2ErrorType() ?: OAuth2ErrorType.UNKNOWN

    /** Optional error uri returned from authentication server */
    val errorUri: String?
        get() = oAuth2ErrorResponse?.errorUri

    /** Localized description */
    override val localizedDescription: String?
        get() = oAuth2ErrorResponse?.errorDescription ?: type.toLocalizedString(context)

    /** Debug description */
    override val debugDescription: String?
        get() {
            var debug = "OAuth2Error: Type [${ type.name }] "
            debug = debug.plus(localizedDescription)
            return debug
        }
}
