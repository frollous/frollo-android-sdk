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

import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.preferences.Preferences

internal class AuthToken(private val keystore: Keystore, private val pref: Preferences) {

    companion object {
        private var accessToken: String? = null
        private var refreshToken: String? = null
        private var accessTokenExpiry: Long = -1
    }

    fun getAccessToken(): String? {
        if (accessToken == null)
            accessToken = keystore.decrypt(pref.encryptedAccessToken)
        return accessToken
    }

    fun getRefreshToken(): String? {
        if (refreshToken == null)
            refreshToken = keystore.decrypt(pref.encryptedRefreshToken)
        return refreshToken
    }

    fun getAccessTokenExpiry(): Long {
        if (accessTokenExpiry == -1L)
            accessTokenExpiry = pref.accessTokenExpiry
        return accessTokenExpiry
    }

    fun saveRefreshToken(token: String) {
        refreshToken = token
        pref.encryptedRefreshToken = keystore.encrypt(token)
    }

    fun saveAccessToken(token: String) {
        accessToken = token
        pref.encryptedAccessToken = keystore.encrypt(accessToken)
    }

    fun saveTokenExpiry(expiry: Long) {
        accessTokenExpiry = expiry
        pref.accessTokenExpiry = expiry
    }

    fun clearTokens() {
        accessToken = null
        pref.resetEncryptedAccessToken()
        refreshToken = null
        pref.resetEncryptedRefreshToken()
        accessTokenExpiry = -1
        pref.resetAccessTokenExpiry()
    }
}
