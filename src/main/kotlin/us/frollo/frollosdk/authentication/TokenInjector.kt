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

import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.preferences.Preferences

/**
 * Manages token injection to SDK.
 */
class TokenInjector(private val keystore: Keystore, private val pref: Preferences) {

    /**
     * Inject refresh token, access token and token expiry to SDK.
     */
    fun injectTokens(refreshToken: String, accessToken: String, accessTokenExpiry: Long? = null) {
        pref.encryptedRefreshToken = keystore.encrypt(refreshToken)
        pref.encryptedAccessToken = keystore.encrypt(accessToken)
        pref.accessTokenExpiry = accessTokenExpiry ?: LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
        pref.loggedIn = true
    }
}