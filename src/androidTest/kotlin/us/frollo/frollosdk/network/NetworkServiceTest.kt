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

package us.frollo.frollosdk.network

import okhttp3.Request
import org.junit.Test

import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.BaseAndroidTest

class NetworkServiceTest : BaseAndroidTest() {

    @Test
    fun testSaveAccessTokens() {
        initSetup()

        assertNull(preferences.encryptedAccessToken)
        assertEquals(-1, preferences.accessTokenExpiry)

        network.saveAccessTokens(accessToken = "MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", expiry = 2550794799)

        assertNotNull(preferences.encryptedAccessToken)
        assertEquals(2550794799, preferences.accessTokenExpiry)

        tearDown()
    }

    @Test
    fun testAuthenticateRequest() {
        initSetup()

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val request = network.authenticateRequest(Request.Builder()
                .url("http://api.example.com/")
                .build())
        assertNotNull(request)
        assertEquals("http://api.example.com/", request.url().toString())

        tearDown()
    }

    @Test
    fun testReset() {
        initSetup()

        network.invalidTokenRetries = 6
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = 14529375950
        assertNotNull(preferences.encryptedAccessToken)
        assertNotNull(preferences.encryptedRefreshToken)
        assertNotEquals(-1, preferences.accessTokenExpiry)

        network.reset()

        assertEquals(0, network.invalidTokenRetries)
        assertNull(preferences.encryptedAccessToken)
        assertNull(preferences.encryptedRefreshToken)
        assertEquals(-1, preferences.accessTokenExpiry)

        tearDown()
    }

    // TODO: SSL Pinning Tests
}