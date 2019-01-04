package us.frollo.frollosdk.auth

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.jraska.livedata.test
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.data.local.SDKDatabase
import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.data.remote.api.UserAPI
import us.frollo.frollosdk.error.APIError
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.extensions.fromJson
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.mapping.toUser
import us.frollo.frollosdk.model.api.user.UserResponse
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson

class AuthenticationTest {

    @get:Rule val testRule = InstantTaskExecutorRule()

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    private lateinit var authentication: Authentication

    private lateinit var mockServer: MockWebServer
    private lateinit var preferences: Preferences

    @Before
    fun setUp() {
        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url(UserAPI.URL_LOGIN)

        FrolloSDK.app = app

        val keyStore = Keystore()
        keyStore.setup()
        preferences = Preferences(app)
        val database = SDKDatabase.getInstance(app)
        val network = NetworkService(baseUrl.toString(), keyStore, preferences)

        authentication = Authentication(DeviceInfo(app), network, database, preferences)
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
        preferences.reset()
    }

    @Test
    fun testGetUser() {
        val body = readStringFromJson(app, R.raw.user_details_complete)
        val mockedResponse = MockResponse()
                .setResponseCode(200)
                .setBody(body)
        mockServer.enqueue(mockedResponse)

        assertNull(authentication.user)

        val testObserver = authentication.loginUser(AuthType.EMAIL, "deepak@frollo.us", "pass1234").test()
        testObserver.awaitNextValue()

        assertNotNull(authentication.user)
        val expectedResponse = Gson().fromJson<UserResponse>(body)
        assertEquals(expectedResponse.toUser(), authentication.user)
    }

    @Test
    fun testGetUserLiveData() {
        val body = readStringFromJson(app, R.raw.user_details_complete)
        val mockedResponse = MockResponse()
                .setResponseCode(200)
                .setBody(body)
        mockServer.enqueue(mockedResponse)

        val testObserver = authentication.loginUser(AuthType.EMAIL, "deepak@frollo.us", "pass1234").test()
        testObserver.awaitNextValue()

        val testObserver2 = authentication.userLiveData?.test()
        testObserver2?.assertHasValue()
        val expectedResponse = Gson().fromJson<UserResponse>(body)
        assertEquals(Resource.Status.SUCCESS, testObserver2?.value()?.status)
        assertNotNull(testObserver2?.value()?.data)
        assertEquals(expectedResponse.toUser(), testObserver2?.value()?.data)
    }

    @Test
    fun testGetLoggedIn() {
        val body = readStringFromJson(app, R.raw.user_details_complete)
        val mockedResponse = MockResponse()
                .setResponseCode(200)
                .setBody(body)
        mockServer.enqueue(mockedResponse)

        assertFalse(authentication.loggedIn)

        val testObserver = authentication.loginUser(AuthType.EMAIL, "deepak@frollo.us", "pass1234").test()
        testObserver.awaitNextValue()

        assertTrue(authentication.loggedIn)
    }

    @Test
    fun testLoginUserEmail() {
        val body = readStringFromJson(app, R.raw.user_details_complete)
        val mockedResponse = MockResponse()
                .setResponseCode(200)
                .setBody(body)
        mockServer.enqueue(mockedResponse)

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
    }

    @Test
    fun testInvalidLoginUser() {
        val body = readStringFromJson(app, R.raw.error_invalid_username_password)
        val mockedResponse = MockResponse()
                .setResponseCode(401)
                .setBody(body)
        mockServer.enqueue(mockedResponse)

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
    }

    @Test
    fun testInvalidLoginData() {
        val testObserver = authentication.loginUser(AuthType.FACEBOOK).test()
        testObserver.assertHasValue()
        assertEquals(Resource.Status.ERROR, testObserver.value().status)
        assertNotNull(testObserver.value().error)
        assertNull(authentication.user)

        val error = testObserver.value().error as DataError
        assertEquals(error.type, DataErrorType.API)
        assertEquals(error.subType, DataErrorSubType.INVALID_DATA)
    }

    @Test
    fun testUserLoggedOutOn401() {
        //TODO: to be implemented
    }

    @Test
    fun testForcedLogoutIfMissingRefreshToken() {
        //TODO: to be implemented
    }

    @Test
    fun testAuthenticatingRequestManually() {
        //TODO: to be implemented
    }

    @Test
    fun testReset() {
        val body = readStringFromJson(app, R.raw.user_details_complete)
        val mockedResponse = MockResponse()
                .setResponseCode(200)
                .setBody(body)
        mockServer.enqueue(mockedResponse)

        val testObserver = authentication.loginUser(AuthType.EMAIL, "deepak@frollo.us", "pass1234").test()
        testObserver.awaitNextValue()

        assertTrue(authentication.loggedIn)

        authentication.reset()

        assertFalse(authentication.loggedIn)
        assertNull(authentication.user)
    }
}