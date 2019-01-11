package us.frollo.frollosdk.auth

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
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.data.local.SDKDatabase
import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.data.remote.api.UserAPI
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
import us.frollo.frollosdk.testutils.readStringFromJson

class AuthenticationTest {

    @get:Rule val testRule = InstantTaskExecutorRule()

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    private lateinit var authentication: Authentication

    private lateinit var mockServer: MockWebServer
    private lateinit var preferences: Preferences
    private lateinit var keystore: Keystore

    private fun initSetup() {
        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url("/")

        if (!FrolloSDK.isSetup) FrolloSDK.setup(app, SetupParams.Builder().serverUrl(baseUrl.toString()).build()) {}

        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        val database = SDKDatabase.getInstance(app)
        val network = NetworkService(baseUrl.toString(), keystore, preferences)

        authentication = Authentication(DeviceInfo(app), network, database, preferences)

        AndroidThreeTen.init(app)
    }

    private fun tearDown() {
        mockServer.shutdown()
        authentication.reset()
        preferences.reset()
    }

    @Test
    fun testGetUser() {
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

        assertNull(authentication.user)

        val testObserver = authentication.loginUser(AuthType.EMAIL, "deepak@frollo.us", "pass1234").test()
        testObserver.awaitNextValue()

        assertNotNull(authentication.user)
        val expectedResponse = Gson().fromJson<UserResponse>(body)
        assertEquals(expectedResponse.toUser(), authentication.user)

        tearDown()
    }

    @Test
    fun testGetUserLiveData() {
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

        val testObserver = authentication.loginUser(AuthType.EMAIL, "deepak@frollo.us", "pass1234").test()
        testObserver.awaitNextValue()

        val testObserver2 = authentication.userLiveData?.test()
        testObserver2?.assertHasValue()
        val expectedResponse = Gson().fromJson<UserResponse>(body)
        assertEquals(Resource.Status.SUCCESS, testObserver2?.value()?.status)
        assertNotNull(testObserver2?.value()?.data)
        assertEquals(expectedResponse.toUser(), testObserver2?.value()?.data)

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

        val testObserver = authentication.loginUser(AuthType.EMAIL, "deepak@frollo.us", "pass1234").test()
        testObserver.awaitNextValue()

        assertTrue(authentication.loggedIn)

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

        val testObserver = authentication.loginUser(AuthType.EMAIL, "user@frollo.us", "password").test()

        testObserver.assertHasValue()
        assertEquals(Resource.Status.LOADING, testObserver.value().status)
        assertNull(testObserver.value().data)

        testObserver.awaitNextValue()

        testObserver.assertHasValue()
        val expectedResponse = Gson().fromJson<UserResponse>(body)
        assertEquals(Resource.Status.SUCCESS, testObserver.value().status)
        assertNotNull(testObserver.value().data)
        assertEquals(expectedResponse.toUser(), testObserver.value().data)

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_LOGIN, request.path)

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

        val testObserver = authentication.loginUser(AuthType.EMAIL, "user@frollo.us", "wrong_password").test()

        testObserver.assertHasValue()
        assertEquals(Resource.Status.LOADING, testObserver.value().status)
        assertNull(testObserver.value().data)

        testObserver.awaitNextValue()

        testObserver.assertHasValue()
        assertEquals(Resource.Status.ERROR, testObserver.value().status)
        assertNotNull(testObserver.value().error)
        assertNull(authentication.user)

        val error = testObserver.value().error as APIError
        assertEquals(error.statusCode, 401)

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_LOGIN, request.path)

        tearDown()
    }

    @Test
    fun testInvalidLoginData() {
        initSetup()

        val testObserver = authentication.loginUser(AuthType.FACEBOOK).test()
        testObserver.assertHasValue()
        assertEquals(Resource.Status.ERROR, testObserver.value().status)
        assertNotNull(testObserver.value().error)
        assertNull(authentication.user)

        val error = testObserver.value().error as DataError
        assertEquals(error.type, DataErrorType.API)
        assertEquals(error.subType, DataErrorSubType.INVALID_DATA)

        tearDown()
    }

    @Test
    fun testRefreshUser() {
        initSetup()

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

        val testObserver = authentication.refreshUser().test()

        testObserver.assertHasValue()
        assertEquals(Resource.Status.LOADING, testObserver.value().status)
        assertNull(testObserver.value().data)

        testObserver.awaitNextValue()

        testObserver.assertHasValue()
        val expectedResponse = Gson().fromJson<UserResponse>(body)
        assertEquals(Resource.Status.SUCCESS, testObserver.value().status)
        assertNotNull(testObserver.value().data)
        assertEquals(expectedResponse.toUser(), testObserver.value().data)

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_USER_DETAILS, request.path)

        tearDown()
    }

    @Test
    fun testUpdateUser() {
        initSetup()

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

        val testObserver = authentication.updateUser(testUserResponseData().toUser()).test()

        testObserver.assertHasValue()
        assertEquals(Resource.Status.LOADING, testObserver.value().status)
        assertNull(testObserver.value().data)

        testObserver.awaitNextValue()

        testObserver.assertHasValue()
        val expectedResponse = Gson().fromJson<UserResponse>(body)
        assertEquals(Resource.Status.SUCCESS, testObserver.value().status)
        assertNotNull(testObserver.value().data)
        assertEquals(expectedResponse.toUser(), testObserver.value().data)

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_USER_DETAILS, request.path)

        tearDown()
    }

    @Test
    fun testUpdateAttribution() {
        initSetup()

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

        val testObserver = authentication.updateAttribution(Attribution(campaign = randomString(8))).test()

        testObserver.assertHasValue()
        assertEquals(Resource.Status.LOADING, testObserver.value().status)
        assertNull(testObserver.value().data)

        testObserver.awaitNextValue()

        testObserver.assertHasValue()
        val expectedResponse = Gson().fromJson<UserResponse>(body)
        assertEquals(Resource.Status.SUCCESS, testObserver.value().status)
        assertNotNull(testObserver.value().data)
        assertEquals(expectedResponse.toUser(), testObserver.value().data)

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_USER_DETAILS, request.path)

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

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val testObserver = authentication.refreshUser().test()
        testObserver.awaitNextValue()

        assertEquals(Resource.Status.ERROR, testObserver.value().status)
        assertNotNull(testObserver.value().error)
        assertTrue(testObserver.value().error is APIError)
        assertEquals(APIErrorType.SUSPENDED_DEVICE, (testObserver.value().error as APIError).type)

        assertNull(preferences.encryptedAccessToken)
        assertNull(preferences.encryptedRefreshToken)
        assertEquals(-1, preferences.accessTokenExpiry)

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

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val testObserver = authentication.refreshUser().test()
        testObserver.awaitNextValue()

        assertEquals(Resource.Status.ERROR, testObserver.value().status)
        assertNotNull(testObserver.value().error)
        assertTrue(testObserver.value().error is DataError)
        assertEquals(DataErrorType.AUTHENTICATION, (testObserver.value().error as DataError).type)
        assertEquals(DataErrorSubType.MISSING_REFRESH_TOKEN, (testObserver.value().error as DataError).subType)

        assertNull(preferences.encryptedAccessToken)
        assertNull(preferences.encryptedRefreshToken)
        assertEquals(-1, preferences.accessTokenExpiry)

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

        val testObserver = authentication.loginUser(AuthType.EMAIL, "deepak@frollo.us", "pass1234").test()
        testObserver.awaitNextValue()

        assertTrue(authentication.loggedIn)

        authentication.reset()

        assertFalse(authentication.loggedIn)
        assertNull(authentication.user)

        tearDown()
    }
}