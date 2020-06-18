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

package us.frollo.frollosdk.preferences

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import us.frollo.frollosdk.authentication.FeatureType
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.model.coredata.user.FeatureFlag

class PreferencesTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
    private lateinit var preferences: Preferences
    private val keyStore = Keystore()

    @Before
    fun setup() {
        preferences = Preferences(context)
        keyStore.setup()
    }

    @After
    fun tearDown() {
        preferences.resetAll()
        keyStore.reset()
    }

    @Test
    fun testSdkVersion() {
        assertNull(preferences.sdkVersion)
        preferences.sdkVersion = "1.0.0"
        assertEquals("1.0.0", preferences.sdkVersion)
    }

    @Test
    fun testSdkVersionHistory() {
        assertTrue(preferences.sdkVersionHistory.isEmpty())
        val versionHistory = mutableListOf("1.0.0")
        preferences.sdkVersionHistory = versionHistory
        assertEquals("1.0.0", preferences.sdkVersionHistory[0])
        versionHistory.add("1.0.1")
        preferences.sdkVersionHistory = versionHistory
        assertEquals("1.0.0", preferences.sdkVersionHistory[0])
        assertEquals("1.0.1", preferences.sdkVersionHistory[1])
    }

    @Test
    fun testLoggedIn() {
        assertFalse(preferences.loggedIn)
        preferences.loggedIn = true
        assertEquals(true, preferences.loggedIn)
    }

    @Test
    fun testFeatures() {
        assertTrue(preferences.features.isEmpty())
        val features = mutableListOf(FeatureFlag(feature = "aggregation", enabled = true))
        preferences.features = features
        assertEquals(true, preferences.isFeatureEnabled(FeatureType.AGGREGATION))
    }

    @Test
    fun testEncryptedRefreshToken() {
        assertNull(preferences.encryptedRefreshToken)
        val encToken = keyStore.encrypt("DummyRefreshToken")
        assertNotNull(encToken)
        preferences.encryptedRefreshToken = encToken
        assertEquals("DummyRefreshToken", keyStore.decrypt(preferences.encryptedRefreshToken))
    }

    @Test
    fun testEncryptedAccessToken() {
        assertNull(preferences.encryptedAccessToken)
        val encToken = keyStore.encrypt("DummyAccessToken")
        assertNotNull(encToken)
        preferences.encryptedAccessToken = encToken
        assertEquals("DummyAccessToken", keyStore.decrypt(preferences.encryptedAccessToken))
    }

    @Test
    fun testAccessTokenExpiry() {
        assertEquals(-1, preferences.accessTokenExpiry)
        preferences.accessTokenExpiry = 14529375950
        assertEquals(14529375950, preferences.accessTokenExpiry)
    }

    @Test
    fun testReset() {
        preferences.sdkVersion = "1.0.0"
        preferences.sdkVersionHistory = mutableListOf("1.0.0")

        preferences.reset()

        assertNotNull(preferences.sdkVersion)
        assertTrue(preferences.sdkVersionHistory.isNotEmpty())
        assertFalse(preferences.loggedIn)
        assertTrue(preferences.features.isEmpty())
        assertNull(preferences.encryptedRefreshToken)
        assertNull(preferences.encryptedAccessToken)
        assertEquals(preferences.accessTokenExpiry, -1)
    }
}
