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

package us.frollo.frollosdk.user

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.jakewharton.threetenabp.AndroidThreeTen
import com.jraska.livedata.test
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
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
import us.frollo.frollosdk.authentication.OAuth2Helper
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.core.testSDKConfig
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.error.APIError
import us.frollo.frollosdk.error.APIErrorType
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.extensions.fromJson
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.mapping.toUser
import us.frollo.frollosdk.model.api.shared.APIErrorCode
import us.frollo.frollosdk.model.api.user.UserResponse
import us.frollo.frollosdk.model.coredata.user.Attribution
import us.frollo.frollosdk.model.testUserResponseData
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.DeviceAPI
import us.frollo.frollosdk.network.api.UserAPI
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.randomString
import us.frollo.frollosdk.testutils.randomUUID
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import us.frollo.frollosdk.testutils.wait
import java.util.Date

class UserManagementTest {
    companion object {
        private const val TOKEN_URL = "token/"
        private const val REVOKE_TOKEN_URL = "revoke/"
    }

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    private lateinit var authentication: Authentication
    private lateinit var userManagement: UserManagement

    private lateinit var mockServer: MockWebServer
    private lateinit var mockTokenServer: MockWebServer
    private lateinit var mockRevokeTokenServer: MockWebServer
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
    fun testFetchUser() {
        initSetup()

        database.users().insert(testUserResponseData(userId = 12345).toUser())

        val testObserver2 = userManagement.fetchUser().test()
        testObserver2.awaitValue()
        assertNotNull(testObserver2.value().data)
        assertEquals(12345L, testObserver2.value().data?.userId)

        wait(3)

        tearDown()
    }

    @Test
    fun testRegisterUser() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_REGISTER) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        userManagement.registerUser(
                firstName = "Frollo",
                lastName = "User",
                mobileNumber = "0412345678",
                postcode = "2060",
                dateOfBirth = Date(),
                email = "user@frollo.us",
                password = "password") { result ->

            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = userManagement.fetchUser().test()
            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)

            val expectedResponse = Gson().fromJson<UserResponse>(body)
            assertEquals(expectedResponse.toUser(), testObserver.value().data)
        }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_REGISTER, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRegisterUserInvalid() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_REGISTER) {
                    return MockResponse()
                            .setResponseCode(409)
                            .setBody(readStringFromJson(app, R.raw.error_duplicate))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        userManagement.registerUser(
                firstName = "Frollo",
                lastName = "User",
                mobileNumber = "0412345678",
                postcode = "2060",
                dateOfBirth = Date(),
                email = "user@frollo.us",
                password = "password") { result ->

            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            val testObserver = userManagement.fetchUser().test()
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
    fun testRegisterUserFailsIfLoggedIn() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_REGISTER) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.loggedIn = true

        userManagement.registerUser(
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

        userManagement.refreshUser { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = userManagement.fetchUser().test()
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

        userManagement.updateUser(testUserResponseData().toUser()) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = userManagement.fetchUser().test()
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

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.user_details_complete))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        userManagement.updateUser(testUserResponseData().toUser()) { result ->
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

        userManagement.updateAttribution(Attribution(campaign = randomString(8))) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = userManagement.fetchUser().test()
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
    fun testChangePassword() {
        initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_CHANGE_PASSWORD) {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        userManagement.changePassword(currentPassword = randomUUID(), newPassword = randomUUID()) { result ->
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

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_CHANGE_PASSWORD) {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        userManagement.changePassword(currentPassword = randomUUID(), newPassword = "1234") { result ->
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

        mockServer.setDispatcher(object : Dispatcher() {
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

        userManagement.deleteUser { result ->
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

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_DELETE_USER) {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        userManagement.deleteUser { result ->
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

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_PASSWORD_RESET) {
                    return MockResponse()
                            .setResponseCode(202)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        userManagement.resetPassword(email = "user@frollo.us") { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_PASSWORD_RESET, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateDevice() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
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

        userManagement.updateDevice(notificationToken = "SomeToken12345") { result ->
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

        mockServer.setDispatcher(object : Dispatcher() {
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

        userManagement.updateDeviceCompliance(true) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        val request = mockServer.takeRequest()
        assertEquals(DeviceAPI.URL_DEVICE, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testAuthenticatingRequestManually() {
        initSetup()

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val request = userManagement.authenticateRequest(Request.Builder()
                .url("http://api.example.com/")
                .build())
        assertNotNull(request)
        assertEquals("http://api.example.com/", request.url().toString())
        assertEquals("Bearer ExistingAccessToken", request.header("Authorization"))

        tearDown()
    }

    @Test
    fun testMigrateUser() {
        initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_MIGRATE_USER) {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        userManagement.migrateUser(password = randomUUID()) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            assertFalse(preferences.loggedIn)
            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1, preferences.accessTokenExpiry)
        }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_MIGRATE_USER, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testMigrateUserFailsMigrationError() {
        initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        val expiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
        preferences.accessTokenExpiry = expiry

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_MIGRATE_USER) {
                    return MockResponse()
                            .setResponseCode(400)
                            .setBody(readStringFromJson(app, R.raw.error_migration))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        userManagement.migrateUser(password = randomUUID()) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertEquals(APIErrorType.MIGRATION_FAILED, (result.error as APIError).type)
            assertEquals(APIErrorCode.MIGRATION_FAILED, (result.error as APIError).errorCode)

            assertTrue(preferences.loggedIn)
            assertEquals("ExistingAccessToken", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("ExistingRefreshToken", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(expiry, preferences.accessTokenExpiry)
        }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_MIGRATE_USER, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testMigrateUserFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        userManagement.migrateUser(password = randomUUID()) { result ->
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
    fun testMigrateUserFailsMissingRefreshToken() {
        initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        val expiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
        preferences.accessTokenExpiry = expiry

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_MIGRATE_USER) {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        userManagement.migrateUser(password = randomUUID()) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_REFRESH_TOKEN, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testMigrateUserFailsIfTooShort() {
        initSetup()

        preferences.loggedIn = true

        userManagement.migrateUser(password = "1234") { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertEquals(DataErrorType.API, (result.error as DataError).type)
            assertEquals(DataErrorSubType.PASSWORD_TOO_SHORT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }
}