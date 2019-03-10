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
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.core.testSDKConfig
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.DeviceAPI
import us.frollo.frollosdk.network.api.UserAPI
import us.frollo.frollosdk.error.*
import us.frollo.frollosdk.extensions.fromJson
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.mapping.toUser
import us.frollo.frollosdk.model.api.user.UserResponse
import us.frollo.frollosdk.model.coredata.user.Attribution
import us.frollo.frollosdk.model.testUserResponseData
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.*
import java.util.*

class AuthenticationTest {

    companion object {
        private const val TOKEN_URL = "token/"
    }

    @get:Rule val testRule = InstantTaskExecutorRule()

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    private lateinit var authentication: Authentication

    private lateinit var mockServer: MockWebServer
    private lateinit var mockTokenServer: MockWebServer
    private lateinit var preferences: Preferences
    private lateinit var keystore: Keystore
    private lateinit var database: SDKDatabase

    private fun initSetup() {
        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url("/")

        mockTokenServer = MockWebServer()
        mockTokenServer.start()
        val baseTokenUrl = mockTokenServer.url("/$TOKEN_URL")

        val config = testSDKConfig(serverUrl = baseUrl.toString(), tokenUrl = baseTokenUrl.toString())
        if (!FrolloSDK.isSetup) FrolloSDK.setup(app, config) {}

        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        database = SDKDatabase.getInstance(app)
        val oAuth = OAuth(config = config)
        val network = NetworkService(oAuth = oAuth, keystore = keystore, pref = preferences)

        authentication = Authentication(oAuth, DeviceInfo(app), network, database, preferences)

        AndroidThreeTen.init(app)
    }

    private fun tearDown() {
        mockServer.shutdown()
        mockTokenServer.shutdown()
        authentication.reset()
        preferences.resetAll()
        database.clearAllTables()
    }

    @Test
    fun testFetchUser() {
        initSetup()

        database.users().insert(testUserResponseData(userId = 12345).toUser())

        val testObserver2 = authentication.fetchUser().test()
        testObserver2.awaitValue()
        assertNotNull(testObserver2.value().data)
        assertEquals(12345L, testObserver2.value().data?.userId)

        wait(3)

        tearDown()
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
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.loginUser("user@frollo.us", "password") { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = authentication.fetchUser().test()
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

        mockTokenServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(401)
                            .setBody(readStringFromJson(app, R.raw.error_invalid_username_password))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.loginUser("user@frollo.us", "wrong_password") { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            val testObserver = authentication.fetchUser().test()
            testObserver.awaitValue()
            assertNull(testObserver.value().data)

            assertEquals(APIErrorType.INVALID_USERNAME_PASSWORD, (result.error as APIError).type)
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
    fun testInvalidLoginUserSecondaryFailure() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.error_invalid_access_token))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.loginUser("user@frollo.us", "password") { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            val testObserver = authentication.fetchUser().test()
            testObserver.awaitValue()
            assertNull(testObserver.value().data)

            assertEquals(APIErrorType.INVALID_ACCESS_TOKEN, (result.error as APIError).type)
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
    fun testLoginUserFailsIfLoggedIn() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object: Dispatcher() {
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

        authentication.loginUser("user@frollo.us", "password") { result ->
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
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object: Dispatcher() {
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

        authentication.handleWebLoginResponse(intent) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = authentication.fetchUser().test()
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

        authentication.handleWebLoginResponse(intent) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            val testObserver = authentication.fetchUser().test()
            testObserver.awaitValue()
            assertNull(testObserver.value().data)

            assertEquals(OAuthErrorType.ACCESS_DENIED, (result.error as OAuthError).type)
            assertFalse(authentication.loggedIn)

            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1L, preferences.accessTokenExpiry)
        }

        tearDown()
    }

    @Test
    fun testRegisterUser() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_REGISTER) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.registerUser(
                firstName = "Frollo",
                lastName = "User",
                mobileNumber = "0412345678",
                postcode = "2060",
                dateOfBirth = Date(),
                email = "user@frollo.us",
                password = "password") { result ->

            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = authentication.fetchUser().test()
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
        assertEquals(UserAPI.URL_REGISTER, request1.trimmedPath)

        val request2 = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request2.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRegisterUserInvalid() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_REGISTER) {
                    return MockResponse()
                            .setResponseCode(409)
                            .setBody(readStringFromJson(app, R.raw.error_duplicate))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.registerUser(
                firstName = "Frollo",
                lastName = "User",
                mobileNumber = "0412345678",
                postcode = "2060",
                dateOfBirth = Date(),
                email = "user@frollo.us",
                password = "password") { result ->

            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            val testObserver = authentication.fetchUser().test()
            testObserver.awaitValue()
            assertNull(testObserver.value().data)

            assertEquals(APIErrorType.ALREADY_EXISTS, (result.error as APIError).type)
            assertFalse(authentication.loggedIn)

            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1L, preferences.accessTokenExpiry)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRegisterUserInvalidSecondaryFailure() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_REGISTER) {
                    return MockResponse()
                            .setResponseCode(201)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(401)
                            .setBody(readStringFromJson(app, R.raw.error_invalid_username_password))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.registerUser(
                firstName = "Frollo",
                lastName = "User",
                mobileNumber = "0412345678",
                postcode = "2060",
                dateOfBirth = Date(),
                email = "user@frollo.us",
                password = "password") { result ->

            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            val testObserver = authentication.fetchUser().test()
            testObserver.awaitValue()
            assertNull(testObserver.value().data)

            assertEquals(APIErrorType.INVALID_USERNAME_PASSWORD, (result.error as APIError).type)
            assertFalse(authentication.loggedIn)

            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1L, preferences.accessTokenExpiry)
        }

        val request1 = mockServer.takeRequest()
        assertEquals(UserAPI.URL_REGISTER, request1.trimmedPath)

        val request2 = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request2.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRegisterUserFailsIfLoggedIn() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_REGISTER) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object: Dispatcher() {
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

        authentication.registerUser(
                firstName = "Frollo",
                lastName = "User",
                mobileNumber = "0412345678",
                postcode = "2060",
                dateOfBirth = Date(),
                email = "user@frollo.us",
                password = "password") { result ->

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
    fun testRefreshUser() {
        initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.refreshUser { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = authentication.fetchUser().test()
            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)

            val expectedResponse = Gson().fromJson<UserResponse>(body)
            assertEquals(expectedResponse.toUser(), testObserver.value().data)
        }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_USER_DETAILS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateUser() {
        initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.updateUser(testUserResponseData().toUser()) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = authentication.fetchUser().test()
            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)

            val expectedResponse = Gson().fromJson<UserResponse>(body)
            assertEquals(expectedResponse.toUser(), testObserver.value().data)
        }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_USER_DETAILS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateUserFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.user_details_complete))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.updateUser(testUserResponseData().toUser()) { result ->
            assertFalse(authentication.loggedIn)

            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        assertEquals(0, mockServer.requestCount)

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateAttribution() {
        initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.updateAttribution(Attribution(campaign = randomString(8))) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = authentication.fetchUser().test()
            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)

            val expectedResponse = Gson().fromJson<UserResponse>(body)
            assertEquals(expectedResponse.toUser(), testObserver.value().data)
        }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_USER_DETAILS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testLogoutUser() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_LOGOUT) {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        authentication.logoutUser { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            assertFalse(authentication.loggedIn)
            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1, preferences.accessTokenExpiry)
        }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_LOGOUT, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testUserLoggedOutOn401() {
        initSetup()

        val body = readStringFromJson(app, R.raw.error_suspended_device)
        mockServer.setDispatcher(object: Dispatcher() {
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

        authentication.refreshUser { result ->
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
    fun testChangePassword() {
        initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_CHANGE_PASSWORD) {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.changePassword(currentPassword = randomUUID(), newPassword = randomUUID()) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_CHANGE_PASSWORD, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testChangePasswordFailsIfTooShort() {
        initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_CHANGE_PASSWORD) {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.changePassword(currentPassword = randomUUID(), newPassword = "1234") { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertEquals(DataErrorType.API, (result.error as DataError).type)
            assertEquals(DataErrorSubType.PASSWORD_TOO_SHORT, (result.error as DataError).subType)
        }

        assertEquals(0, mockServer.requestCount)

        wait(3)

        tearDown()
    }

    @Test
    fun testDeleteUser() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_DELETE_USER) {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        authentication.deleteUser { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            assertFalse(authentication.loggedIn)
            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1, preferences.accessTokenExpiry)
        }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_DELETE_USER, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testDeleteUserFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_DELETE_USER) {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.deleteUser { result ->
            assertFalse(authentication.loggedIn)

            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        assertEquals(0, mockServer.requestCount)

        wait(3)

        tearDown()
    }


    @Test
    fun testResetPassword() {
        initSetup()

        preferences.loggedIn = true

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_PASSWORD_RESET) {
                    return MockResponse()
                            .setResponseCode(202)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.resetPassword(email = "user@frollo.us") { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_PASSWORD_RESET, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testForcedLogoutIfMissingRefreshToken() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object: Dispatcher() {
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

        authentication.refreshUser { result ->
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
    fun testUpdateDevice() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == DeviceAPI.URL_DEVICE) {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        authentication.updateDevice(notificationToken = "SomeToken12345") { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        val request = mockServer.takeRequest()
        assertEquals(DeviceAPI.URL_DEVICE, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateDeviceCompliance() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == DeviceAPI.URL_DEVICE) {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        authentication.updateDeviceCompliance(true) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        val request = mockServer.takeRequest()
        assertEquals(DeviceAPI.URL_DEVICE, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testExchangeAuthorizationCode() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.exchangeAuthorizationCode(code = randomString(32), codeVerifier = randomString(32)) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = authentication.fetchUser().test()
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

        mockTokenServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(401)
                            .setBody(readStringFromJson(app, R.raw.error_invalid_username_password))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.exchangeAuthorizationCode(code = randomString(32), codeVerifier = randomString(32)) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            val testObserver = authentication.fetchUser().test()
            testObserver.awaitValue()
            assertNull(testObserver.value().data)

            assertEquals(APIErrorType.INVALID_USERNAME_PASSWORD, (result.error as APIError).type)
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

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.error_invalid_auth_head))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.exchangeAuthorizationCode(code = randomString(32), codeVerifier = randomString(32)) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            val testObserver = authentication.fetchUser().test()
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
    fun testExchangeLegacyAccessToken() {
        initSetup()

        mockTokenServer.setDispatcher(object: Dispatcher() {
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

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt(legacyToken)
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        authentication.exchangeLegacyToken(legacyToken = legacyToken) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(2550794799, preferences.accessTokenExpiry)
        }

        val request = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testAuthenticatingRequestManually() {
        initSetup()

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val request = authentication.authenticateRequest(Request.Builder()
                .url("http://api.example.com/")
                .build())
        assertNotNull(request)
        assertEquals("http://api.example.com/", request.url().toString())
        assertEquals("Bearer ExistingAccessToken", request.header("Authorization"))

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