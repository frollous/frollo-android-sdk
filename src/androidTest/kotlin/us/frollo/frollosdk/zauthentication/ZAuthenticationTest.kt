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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.authentication.Authentication
import us.frollo.frollosdk.authentication.AuthenticationCallback
import us.frollo.frollosdk.authentication.OAuth2Helper
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.core.testSDKCustomConfig
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.testutils.wait

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
        val config = testSDKCustomConfig(authentication = authentication, serverUrl = baseUrl.toString())
        if (!FrolloSDK.isSetup) FrolloSDK.setup(app, config) {}

        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        database = SDKDatabase.getInstance(app)
        val oAuth = OAuth2Helper(config = config)
        network = NetworkService(oAuth2Helper = oAuth, keystore = keystore, pref = preferences)
        network.authentication = authentication

        AndroidThreeTen.init(app)
    }

    private fun tearDown() {
        resetSingletonByReflection()
        mockServer.shutdown()
        network.reset()
        authentication.reset()
        preferences.resetAll()
        database.clearAllTables()
    }

    @Test
    fun testCustomAuthentication() {
        initSetup()

        assertEquals(authentication, FrolloSDK.authentication)
        assertFalse(FrolloSDK.authentication.loggedIn)

        authentication.login()

        assertTrue(FrolloSDK.authentication.loggedIn)
        assertEquals("AccessToken001", keystore.decrypt(preferences.encryptedAccessToken))
        assertNull(preferences.encryptedRefreshToken)
        assertNotNull(preferences.accessTokenExpiry)

        FrolloSDK.authentication.refreshTokens { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            assertEquals("AccessToken002", keystore.decrypt(preferences.encryptedAccessToken))
            assertNull(preferences.encryptedRefreshToken)
            assertNotNull(preferences.accessTokenExpiry)
        }

        wait(3)

        tearDown()
    }
}

class TestCustomAuthentication : Authentication() {

    private var tokenIndex = 0
    private val validTokens = arrayOf("AccessToken001", "AccessToken002", "AccessToken003")

    override var loggedIn: Boolean = false

    override var authenticationCallback: AuthenticationCallback? = null

    fun login() {
        loggedIn = true

        authenticationCallback?.saveAccessTokens(
                accessToken = validTokens[tokenIndex],
                expiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 3600)
    }

    override fun refreshTokens(completion: OnFrolloSDKCompletionListener<Result>?) {
        if (tokenIndex < 2) {
            tokenIndex ++
        } else {
            tokenIndex = 0
        }

        authenticationCallback?.saveAccessTokens(
                accessToken = validTokens[tokenIndex],
                expiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 3600)

        completion?.invoke(Result.success())
    }

    override fun logout() {
        reset()
    }

    override fun reset() {
        loggedIn = false

        tokenIndex = 0
    }
}