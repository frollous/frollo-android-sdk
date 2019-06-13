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

import android.app.Application
import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.jakewharton.threetenabp.AndroidThreeTen
import com.jraska.livedata.test
import net.openid.appauth.AuthorizationException
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Test

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.core.testSDKConfig
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.error.APIError
import us.frollo.frollosdk.error.APIErrorType
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.error.OAuth2Error
import us.frollo.frollosdk.error.OAuth2ErrorType
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.UserAPI
import us.frollo.frollosdk.extensions.fromJson
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.mapping.toUser
import us.frollo.frollosdk.model.api.user.UserResponse
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.randomString
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import us.frollo.frollosdk.testutils.wait
import us.frollo.frollosdk.user.UserManagement

class AuthenticationTest {

    companion object {
        private const val TOKEN_URL = "token/"
        private const val REVOKE_TOKEN_URL = "revoke/"
    }

    @get:Rule val testRule = InstantTaskExecutorRule()

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    private lateinit var authentication: Authentication
    private lateinit var userManagement: UserManagement

    private lateinit var mockServer: MockWebServer
    private lateinit var mockTokenServer: MockWebServer
    private lateinit var mockRevokeTokenServer: MockWebServer
    private lateinit var preferences: Preferences
    private lateinit var keystore: Keystore
    private lateinit var database: SDKDatabase
    private val scopes = listOf("offline_access", "openid", "email")

    private fun initSetup() {
        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url("/")

        mockTokenServer = MockWebServer()
        mockTokenServer.start()
        val baseTokenUrl = mockTokenServer.url("/$TOKEN_URL")

        mockRevokeTokenServer = MockWebServer()
        mockRevokeTokenServer.start()
        val baseRevokeTokenUrl = mockRevokeTokenServer.url("/$REVOKE_TOKEN_URL")

        val config = testSDKConfig(serverUrl = baseUrl.toString(), tokenUrl = baseTokenUrl.toString(), revokeTokenURL = baseRevokeTokenUrl.toString())
        if (!FrolloSDK.isSetup) FrolloSDK.setup(app, config) {}

        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        database = SDKDatabase.getInstance(app)
        val oAuth = OAuth2Helper(config = config)
        val network = NetworkService(oAuth2Helper = oAuth, keystore = keystore, pref = preferences)

        authentication = Authentication(oAuth, network, preferences, FrolloSDK)
        userManagement = UserManagement(DeviceInfo(app), network, database, preferences, authentication)

        AndroidThreeTen.init(app)
    }

    private fun tearDown() {
        mockServer.shutdown()
        mockTokenServer.shutdown()
        userManagement.reset()
        authentication.reset()
        preferences.resetAll()
        database.clearAllTables()
    }

    @Test
    fun testGetLoggedIn() {
        initSetup()

        assertFalse(authentication.loggedIn)

        preferences.loggedIn = true

        assertTrue(authentication.loggedIn)

        tearDown()
    }

    @Test
    fun testLoginUser() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.loginUser("user@frollo.us", "password", scopes) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = userManagement.fetchUser().test()
            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)

            val expectedResponse = Gson().fromJson<UserResponse>(body)
            assertEquals(expectedResponse.toUser(), testObserver.value().data)
            assertTrue(authentication.loggedIn)

            assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(2550794799, preferences.accessTokenExpiry)
        }

        val request1 = mockServer.takeRequest()
        assertEquals(UserAPI.URL_USER_DETAILS, request1.trimmedPath)

        val request2 = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request2.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testInvalidLoginUser() {
        initSetup()

        mockTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(401)
                            .setBody(readStringFromJson(app, R.raw.error_oauth2_invalid_client))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.loginUser("user@frollo.us", "wrong_password", scopes) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            val testObserver = userManagement.fetchUser().test()
            testObserver.awaitValue()
            assertNull(testObserver.value().data)

            assertEquals(OAuth2ErrorType.INVALID_CLIENT, (result.error as OAuth2Error).type)
            assertFalse(authentication.loggedIn)

            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1L, preferences.accessTokenExpiry)
        }

        val request = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testLoginUserFailsIfLoggedIn() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.loggedIn = true

        authentication.loginUser("user@frollo.us", "password", scopes) { result ->
            assertTrue(authentication.loggedIn)

            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.ALREADY_LOGGED_IN, (result.error as DataError).subType)
        }

        assertEquals(0, mockServer.requestCount)
        assertEquals(0, mockTokenServer.requestCount)

        wait(3)

        tearDown()
    }

    @Test
    fun testLoginUserViaWebPendingIntent() {
        // TODO: to be implemented
    }

    @Test
    fun testLoginUserViaWeb() {
        // TODO: to be implemented
    }

    @Test
    fun testHandleWebLoginResponse() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val json = readStringFromJson(app, R.raw.authorization_code_valid)
        val intent = Intent().putExtra("net.openid.appauth.AuthorizationResponse", json)

        authentication.handleWebLoginResponse(intent, scopes) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = userManagement.fetchUser().test()
            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)

            val expectedResponse = Gson().fromJson<UserResponse>(body)
            assertEquals(expectedResponse.toUser(), testObserver.value().data)
            assertTrue(authentication.loggedIn)

            assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(2550794799, preferences.accessTokenExpiry)
        }

        val request1 = mockServer.takeRequest()
        assertEquals(UserAPI.URL_USER_DETAILS, request1.trimmedPath)

        val request2 = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request2.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testHandleWebLoginResponseFail() {
        initSetup()

        val intent = AuthorizationException.AuthorizationRequestErrors.ACCESS_DENIED.toIntent()

        authentication.handleWebLoginResponse(intent, scopes) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            val testObserver = userManagement.fetchUser().test()
            testObserver.awaitValue()
            assertNull(testObserver.value().data)

            assertEquals(OAuth2ErrorType.ACCESS_DENIED, (result.error as OAuth2Error).type)
            assertFalse(authentication.loggedIn)

            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1L, preferences.accessTokenExpiry)
        }

        tearDown()
    }

    @Test
    fun testLogoutUser() {
        initSetup()

        var tokenRevoked = false
        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        mockRevokeTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == REVOKE_TOKEN_URL) {
                    tokenRevoked = true
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.logoutUser()

        wait(2)

        assertFalse(authentication.loggedIn)
        assertNull(preferences.encryptedAccessToken)
        assertNull(preferences.encryptedRefreshToken)
        assertEquals(-1, preferences.accessTokenExpiry)
        assertTrue(tokenRevoked)

        tearDown()
    }

    @Test
    fun testUserLoggedOutOn401() {
        initSetup()

        val body = readStringFromJson(app, R.raw.error_suspended_device)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(401)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        userManagement.refreshUser { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertTrue(result.error is APIError)
            assertEquals(APIErrorType.SUSPENDED_DEVICE, (result.error as APIError).type)

            assertFalse(authentication.loggedIn)
            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1, preferences.accessTokenExpiry)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testForcedLogoutIfMissingRefreshToken() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        userManagement.refreshUser { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertTrue(result.error is DataError)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_REFRESH_TOKEN, (result.error as DataError).subType)

            assertFalse(authentication.loggedIn)
            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1, preferences.accessTokenExpiry)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testExchangeAuthorizationCode() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.exchangeAuthorizationCode(code = randomString(32), codeVerifier = randomString(32), scopes = scopes) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = userManagement.fetchUser().test()
            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)

            val expectedResponse = Gson().fromJson<UserResponse>(body)
            assertEquals(expectedResponse.toUser(), testObserver.value().data)
            assertTrue(authentication.loggedIn)

            assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(2550794799, preferences.accessTokenExpiry)
        }

        val request1 = mockServer.takeRequest()
        assertEquals(UserAPI.URL_USER_DETAILS, request1.trimmedPath)

        val request2 = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request2.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testExchangeAuthorizationCodeInvalid() {
        initSetup()

        mockTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(401)
                            .setBody(readStringFromJson(app, R.raw.error_oauth2_invalid_client))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.exchangeAuthorizationCode(code = randomString(32), codeVerifier = randomString(32), scopes = scopes) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            val testObserver = userManagement.fetchUser().test()
            testObserver.awaitValue()
            assertNull(testObserver.value().data)

            assertEquals(OAuth2ErrorType.INVALID_CLIENT, (result.error as OAuth2Error).type)
            assertFalse(authentication.loggedIn)

            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1L, preferences.accessTokenExpiry)
        }

        val request = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testExchangeAuthorizationCodeInvalidSecondaryFailure() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.error_invalid_auth_head))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.exchangeAuthorizationCode(code = randomString(32), codeVerifier = randomString(32), scopes = scopes) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            val testObserver = userManagement.fetchUser().test()
            testObserver.awaitValue()
            assertNull(testObserver.value().data)

            assertEquals(APIErrorType.OTHER_AUTHORISATION, (result.error as APIError).type)
            assertFalse(authentication.loggedIn)

            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1L, preferences.accessTokenExpiry)
        }

        val request1 = mockServer.takeRequest()
        assertEquals(UserAPI.URL_USER_DETAILS, request1.trimmedPath)

        val request2 = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request2.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testExchangeLegacyRefreshToken() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val legacyToken = randomString(32)

        authentication.exchangeLegacyToken(legacyToken = legacyToken) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(2550794799, preferences.accessTokenExpiry)

            val testObserver = userManagement.fetchUser().test()
            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)

            val expectedResponse = Gson().fromJson<UserResponse>(body)
            assertEquals(expectedResponse.toUser(), testObserver.value().data)
            assertTrue(authentication.loggedIn)
        }

        val request1 = mockServer.takeRequest()
        assertEquals(UserAPI.URL_USER_DETAILS, request1.trimmedPath)

        val request2 = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request2.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testExchangeLegacyRefreshTokenSecondaryFailure() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.error_invalid_refresh_token))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val legacyToken = randomString(32)

        authentication.exchangeLegacyToken(legacyToken = legacyToken) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            val testObserver = userManagement.fetchUser().test()
            testObserver.awaitValue()
            assertNull(testObserver.value().data)

            assertEquals(APIErrorType.INVALID_REFRESH_TOKEN, (result.error as APIError).type)
            assertFalse(authentication.loggedIn)

            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1L, preferences.accessTokenExpiry)
        }

        val request1 = mockServer.takeRequest()
        assertEquals(UserAPI.URL_USER_DETAILS, request1.trimmedPath)

        val request2 = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request2.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testReset() {
        initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        assertTrue(authentication.loggedIn)

        authentication.reset()

        assertFalse(authentication.loggedIn)
        assertNull(preferences.encryptedAccessToken)
        assertNull(preferences.encryptedRefreshToken)
        assertEquals(-1, preferences.accessTokenExpiry)

        tearDown()
    }
}