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

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.preferences.Preferences

class AuthTokenTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
    private lateinit var preferences: Preferences
    private val keyStore = Keystore()
    private lateinit var authToken: AuthToken

    @Before
    fun setup() {
        preferences = Preferences(context)
        keyStore.setup()
        authToken = AuthToken(keyStore, preferences)
        authToken.clearTokens()
    }

    @After
    fun tearDown() {
        preferences.resetAll()
        keyStore.reset()
        authToken.clearTokens()
    }

    @Test
    fun testAccessToken() {
        assertNull(authToken.getAccessToken())
        val encToken = keyStore.encrypt("DummyAccessToken")
        assertNotNull(encToken)
        preferences.encryptedAccessToken = encToken
        assertEquals("DummyAccessToken", authToken.getAccessToken())
    }

    @Test
    fun testRefreshToken() {
        assertNull(authToken.getRefreshToken())
        val encToken = keyStore.encrypt("DummyRefreshToken")
        assertNotNull(encToken)
        preferences.encryptedRefreshToken = encToken
        assertEquals("DummyRefreshToken", authToken.getRefreshToken())
    }

    @Test
    fun testAccessTokenExpiry() {
        assertEquals(-1, authToken.getAccessTokenExpiry())
        preferences.accessTokenExpiry = 14529375950
        assertEquals(14529375950, authToken.getAccessTokenExpiry())
    }

    @Test
    fun testSaveRefreshToken() {
        assertNull(authToken.getRefreshToken())
        authToken.saveRefreshToken(token = "IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk")
        assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", authToken.getRefreshToken())
        assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keyStore.decrypt(preferences.encryptedRefreshToken))
    }

    @Test
    fun testSaveAccessToken() {
        assertNull(authToken.getAccessToken())
        authToken.saveAccessToken(token = "MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3")
        assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", authToken.getAccessToken())
        assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keyStore.decrypt(preferences.encryptedAccessToken))
    }

    @Test
    fun testSaveTokenExpiry() {
        assertEquals(-1, authToken.getAccessTokenExpiry())
        authToken.saveTokenExpiry(expiry = 2550794799)
        assertEquals(2550794799, authToken.getAccessTokenExpiry())
        assertEquals(2550794799, preferences.accessTokenExpiry)
    }

    @Test
    fun testClearTokens() {
        authToken.clearTokens()
        assertNull(authToken.getAccessToken())
        assertNull(authToken.getRefreshToken())
        assertNull(preferences.encryptedAccessToken)
        assertNull(preferences.encryptedRefreshToken)
        assertEquals(-1, authToken.getAccessTokenExpiry())
        assertEquals(-1, preferences.accessTokenExpiry)
    }
}