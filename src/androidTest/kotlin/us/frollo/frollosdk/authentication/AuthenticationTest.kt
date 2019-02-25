package us.frollo.frollosdk.authentication

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
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.core.SetupParams
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
import us.frollo.frollosdk.testutils.randomString
import us.frollo.frollosdk.testutils.randomUUID
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.wait
import java.util.*

class AuthenticationTest {

    @get:Rule val testRule = InstantTaskExecutorRule()

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    private lateinit var authentication: Authentication

    private lateinit var mockServer: MockWebServer
    private lateinit var preferences: Preferences
    private lateinit var keystore: Keystore
    private lateinit var database: SDKDatabase

    private fun initSetup() {
        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url("/")

        if (!FrolloSDK.isSetup) FrolloSDK.setup(app, SetupParams.Builder().serverUrl(baseUrl.toString()).build()) {}
        FrolloSDK.app = app

        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        database = SDKDatabase.getInstance(app)
        val network = NetworkService(baseUrl.toString(), keystore, preferences)

        authentication = Authentication(DeviceInfo(app), network, database, preferences)

        AndroidThreeTen.init(app)
    }

    private fun tearDown() {
        mockServer.shutdown()
        authentication.reset()
        preferences.resetAll()
        database.clearAllTables()
    }

    @Test
    fun testFetchUser() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == UserAPI.URL_LOGIN) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val testObserver = authentication.fetchUser().test()
        testObserver.awaitValue()
        assertNull(testObserver.value().data)

        authentication.loginUser(AuthType.EMAIL, "user@frollo.us", "password") { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver2 = authentication.fetchUser().test()
            testObserver2.awaitValue()
            assertNotNull(testObserver2.value().data)

            val expectedResponse = Gson().fromJson<UserResponse>(body)
            assertEquals(expectedResponse.toUser(), testObserver2.value().data)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testGetLoggedIn() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == UserAPI.URL_LOGIN) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        assertFalse(authentication.loggedIn)

        authentication.loginUser(AuthType.EMAIL, "user@frollo.us", "password") { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            assertTrue(authentication.loggedIn)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testLoginUserEmail() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == UserAPI.URL_LOGIN) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.loginUser(AuthType.EMAIL, "user@frollo.us", "password") { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = authentication.fetchUser().test()
            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)

            val expectedResponse = Gson().fromJson<UserResponse>(body)
            assertEquals(expectedResponse.toUser(), testObserver.value().data)
            assertTrue(authentication.loggedIn)
        }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_LOGIN, request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testInvalidLoginUser() {
        initSetup()

        val body = readStringFromJson(app, R.raw.error_invalid_username_password)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == UserAPI.URL_LOGIN) {
                    return MockResponse()
                            .setResponseCode(401)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.loginUser(AuthType.EMAIL, "user@frollo.us", "wrong_password") { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            val testObserver = authentication.fetchUser().test()
            testObserver.awaitValue()
            assertNull(testObserver.value().data)

            assertEquals(401, (result.error as APIError).statusCode)
            assertFalse(authentication.loggedIn)
        }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_LOGIN, request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testInvalidLoginData() {
        initSetup()

        authentication.loginUser(AuthType.FACEBOOK) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            val testObserver = authentication.fetchUser().test()
            testObserver.awaitValue()
            assertNull(testObserver.value().data)

            assertEquals((result.error as DataError).type, DataErrorType.API)
            assertEquals((result.error as DataError).subType, DataErrorSubType.INVALID_DATA)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRegisterUser() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == UserAPI.URL_REGISTER) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
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
        }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_REGISTER, request.path)

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
                if (request?.path == UserAPI.URL_USER_DETAILS) {
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
        assertEquals(UserAPI.URL_USER_DETAILS, request.path)

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
                if (request?.path == UserAPI.URL_USER_DETAILS) {
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
        assertEquals(UserAPI.URL_USER_DETAILS, request.path)

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
                if (request?.path == UserAPI.URL_USER_DETAILS) {
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
        assertEquals(UserAPI.URL_USER_DETAILS, request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testLogoutUser() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == UserAPI.URL_LOGOUT) {
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
        assertEquals(UserAPI.URL_LOGOUT, request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testUserLoggedOutOn401() {
        initSetup()

        val body = readStringFromJson(app, R.raw.error_suspended_device)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == UserAPI.URL_USER_DETAILS) {
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
                if (request?.path == UserAPI.URL_CHANGE_PASSWORD) {
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
        assertEquals(UserAPI.URL_CHANGE_PASSWORD, request.path)

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
                if (request?.path == UserAPI.URL_CHANGE_PASSWORD) {
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
                if (request?.path == UserAPI.URL_DELETE_USER) {
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
        assertEquals(UserAPI.URL_DELETE_USER, request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testResetPassword() {
        initSetup()

        preferences.loggedIn = true

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == UserAPI.URL_PASSWORD_RESET) {
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
        assertEquals(UserAPI.URL_PASSWORD_RESET, request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testForcedLogoutIfMissingRefreshToken() {
        initSetup()

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == UserAPI.URL_USER_DETAILS
                        || request?.path == UserAPI.URL_LOGIN) {
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
                if (request?.path == DeviceAPI.URL_DEVICE) {
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
        assertEquals(DeviceAPI.URL_DEVICE, request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateDeviceCompliance() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == DeviceAPI.URL_DEVICE) {
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
        assertEquals(DeviceAPI.URL_DEVICE, request.path)

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

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == UserAPI.URL_LOGIN) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.loginUser(AuthType.EMAIL, "deepak@frollo.us", "pass1234") { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            assertTrue(authentication.loggedIn)

            authentication.reset()

            assertFalse(authentication.loggedIn)
            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1, preferences.accessTokenExpiry)
        }

        wait(3)

        tearDown()
    }

    /*
     * Example test for livedata observer
     *
     * @get:Rule val testRule = InstantTaskExecutorRule()
     *
     * @Test
     * fun testLivedata() {
     *     val testObserver = yourFunctionToGetLiveData().test() //com.jraska.livedata.test
     *     testObserver.awaitNextValue()
     *
     *     testObserver.assertHasValue()
     *     assertEquals(Resource.Status.SUCCESS, testObserver.value().status)
     *     assertEquals(expectedValue, testObserver.value().data)
     * }
     *
     */
}