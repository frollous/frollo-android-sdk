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

package us.frollo.frollosdk.zauthentication

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.threetenabp.AndroidThreeTen
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.authentication.AccessToken
import us.frollo.frollosdk.authentication.AccessTokenProvider
import us.frollo.frollosdk.authentication.AuthenticationCallback
import us.frollo.frollosdk.authentication.OAuth2Helper
import us.frollo.frollosdk.core.AppInfo
import us.frollo.frollosdk.core.testSDKCustomConfig
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.testutils.randomUUID

/**
 * NOTE: Named this ZAuthentication on purpose as the tests run in alphabetical order
 * and we want this to run last because we have to reset and setup FrolloSDK again
 * with CustomAuthentication.
 */
class ZAuthenticationTest {

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    lateinit var mockServer: MockWebServer
    lateinit var network: NetworkService
    lateinit var preferences: Preferences
    lateinit var keystore: Keystore
    lateinit var database: SDKDatabase
    lateinit var authentication: TestCustomAuthentication

    private fun resetSingletonByReflection() {
        val setup = FrolloSDK::class.java.getDeclaredField("_setup")
        setup.isAccessible = true
        setup.setBoolean(null, false)
    }

    private fun initSetup() {
        resetSingletonByReflection()

        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url("/")

        authentication = TestCustomAuthentication()
        val config = testSDKCustomConfig(
                accessTokenProvider = authentication,
                authenticationCallback = authentication,
                serverUrl = baseUrl.toString())
        if (!FrolloSDK.isSetup) FrolloSDK.setup(app, config) {}

        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        database = SDKDatabase.getInstance(app)
        val oAuth = OAuth2Helper(config = config)
        network = NetworkService(oAuth2Helper = oAuth, keystore = keystore, pref = preferences, appInfo = AppInfo(app))
        network.accessTokenProvider = authentication
        network.authenticationCallback = authentication

        AndroidThreeTen.init(app)
    }

    private fun tearDown() {
        resetSingletonByReflection()
        mockServer.shutdown()
        network.reset()
        preferences.resetAll()
        database.clearAllTables()
    }

    @Test
    fun testCustomAuthentication() {
        initSetup()

        Assert.assertEquals(authentication, network.accessTokenProvider)
        Assert.assertEquals(authentication, network.authenticationCallback)

        Assert.assertNotNull(authentication.accessToken)

        val oldToken = authentication.accessToken?.token

        network.authenticationCallback?.accessTokenExpired()

        Assert.assertNotEquals(oldToken, authentication.accessToken?.token)

        Assert.assertNull(preferences.encryptedAccessToken)
        Assert.assertNull(preferences.encryptedRefreshToken)
        Assert.assertNotNull(preferences.accessTokenExpiry)

        tearDown()
    }
}

class TestCustomAuthentication : AccessTokenProvider, AuthenticationCallback {

    override var accessToken: AccessToken? = null

    init {
        refreshToken()
    }

    override fun accessTokenExpired() {
        refreshToken()
    }

    override fun tokenInvalidated() {
        accessToken = null
    }

    private fun refreshToken() {
        accessToken = AccessToken(
                token = randomUUID(),
                expiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 3600)
    }
}