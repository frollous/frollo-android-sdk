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

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.assertNotNull
import us.frollo.frollosdk.authentication.AuthToken
import us.frollo.frollosdk.core.AppInfo
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.preferences.Preferences

class NetworkHelperTest {

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    private lateinit var keystore: Keystore
    private lateinit var preferences: Preferences
    private lateinit var authToken: AuthToken
    private lateinit var appInfo: AppInfo

    @Before
    fun setUp() {
        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        authToken = AuthToken(keystore, preferences)
        appInfo = AppInfo(app)
    }

    @After
    fun tearDown() {
        keystore.reset()
        preferences.resetAll()
        authToken.clearTokens()
    }

    @Test
    fun testBundleId() {
        val networkHelper = NetworkHelper(appInfo)
        assertNotNull(networkHelper.bundleId)
    }

    @Test
    fun testSoftwareVersion() {
        val networkHelper = NetworkHelper(appInfo)
        assertNotNull(networkHelper.softwareVersion)
    }

    @Test
    fun testDeviceVersion() {
        val networkHelper = NetworkHelper(appInfo)
        assertNotNull(networkHelper.deviceVersion)
    }

    @Test
    fun testUserAgent() {
        val networkHelper = NetworkHelper(appInfo)
        assertNotNull(networkHelper.userAgent)
    }
}