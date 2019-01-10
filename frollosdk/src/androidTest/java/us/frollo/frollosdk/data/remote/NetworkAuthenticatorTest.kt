package us.frollo.frollosdk.data.remote

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.jraska.livedata.test
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.data.remote.api.TokenAPI
import us.frollo.frollosdk.data.remote.api.UserAPI
import us.frollo.frollosdk.error.APIError
import us.frollo.frollosdk.error.APIErrorType
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson

class NetworkAuthenticatorTest {

    @get:Rule val testRule = InstantTaskExecutorRule()

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    private lateinit var mockServer: MockWebServer
    private lateinit var keystore: Keystore
    private lateinit var preferences: Preferences
    private lateinit var network: NetworkService
    private lateinit var userAPI: UserAPI

    private fun initSetup() {
        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url("/")

        if (!FrolloSDK.isSetup) FrolloSDK.setup(app, SetupParams.Builder().serverUrl(baseUrl.toString()).build()) {}

        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        network = NetworkService(baseUrl.toString(), keystore, preferences)
        userAPI = network.create(UserAPI::class.java)
    }

    private fun tearDown() {
        mockServer.shutdown()
        network.reset()
        preferences.reset()
    }

    @Test
    fun testPreemptiveAccessTokenRefresh() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == TokenAPI.URL_TOKEN_REFRESH) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.refresh_token_valid))
                } else if (request?.path == UserAPI.URL_USER_DETAILS) {
                        return MockResponse()
                                .setResponseCode(200)
                                .setBody(readStringFromJson(app, R.raw.user_details_complete))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 30 // 30 seconds in the future falls within the 5 minute access token expiry

        val testObserver = userAPI.fetchUser().test()
        testObserver.awaitNextValue()

        assertEquals(2, mockServer.requestCount)
        assertEquals("AValidAccessTokenFromHost", keystore.decrypt(preferences.encryptedAccessToken))
        assertEquals("AValidRefreshTokenFromHost", keystore.decrypt(preferences.encryptedRefreshToken))
        assertEquals(1721259268, preferences.accessTokenExpiry)

        tearDown()
    }

    @Test
    fun testInvalidAccessTokenRefresh() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            var failedOnce = false

            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == TokenAPI.URL_TOKEN_REFRESH) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.refresh_token_valid))
                } else if (request?.path == UserAPI.URL_USER_DETAILS) {
                    if (failedOnce) {
                        return MockResponse()
                                .setResponseCode(200)
                                .setBody(readStringFromJson(app, R.raw.user_details_complete))
                    } else {
                        failedOnce = true
                        return MockResponse()
                                .setResponseCode(401)
                                .setBody(readStringFromJson(app, R.raw.error_invalid_access_token))
                    }
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900 //

        val testObserver = userAPI.fetchUser().test()
        testObserver.awaitNextValue()

        assertEquals(3, mockServer.requestCount)
        assertEquals("AValidAccessTokenFromHost", keystore.decrypt(preferences.encryptedAccessToken))
        assertEquals("AValidRefreshTokenFromHost", keystore.decrypt(preferences.encryptedRefreshToken))
        assertEquals(1721259268, preferences.accessTokenExpiry)

        tearDown()
    }

    @Test
    fun testInvalidRefreshTokenFails() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                return MockResponse()
                        .setResponseCode(401)
                        .setBody(readStringFromJson(app, R.raw.error_invalid_refresh_token))
            }
        })

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 30

        val testObserver = userAPI.fetchUser().test()
        testObserver.awaitValue()

        assertEquals(2, mockServer.requestCount)
        testObserver.assertHasValue()
        val value = Resource.fromApiResponse(testObserver.value())
        assertEquals(Resource.Status.ERROR, value.status)
        assertNotNull(value.error)
        assertTrue(value.error is APIError)
        assertEquals(APIErrorType.INVALID_REFRESH_TOKEN, (value.error as APIError).type)
        assertNull(preferences.encryptedAccessToken)
        assertNull(preferences.encryptedRefreshToken)
        assertEquals(-1, preferences.accessTokenExpiry)

        tearDown()
    }

    @Test
    fun testRequestsGetRetriedAfterRefreshingAccessToken() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            var userRequestCount = 0

            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == TokenAPI.URL_TOKEN_REFRESH) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.refresh_token_valid))
                } else if (request?.path == UserAPI.URL_USER_DETAILS) {
                    if (userRequestCount < 3) {
                        userRequestCount++
                        return MockResponse()
                                .setResponseCode(401)
                                .setBody(readStringFromJson(app, R.raw.error_invalid_access_token))
                    } else {
                        return MockResponse()
                                .setResponseCode(200)
                                .setBody(readStringFromJson(app, R.raw.user_details_complete))
                    }
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val testObserver = userAPI.fetchUser().test()
        testObserver.awaitNextValue()

        assertEquals("AValidAccessTokenFromHost", keystore.decrypt(preferences.encryptedAccessToken))
        assertEquals("AValidRefreshTokenFromHost", keystore.decrypt(preferences.encryptedRefreshToken))
        assertEquals(1721259268, preferences.accessTokenExpiry)

        var value = Resource.fromApiResponse(testObserver.value())
        assertEquals(Resource.Status.SUCCESS, value.status)
        assertNotNull(value.data)

        val testObserver2 = userAPI.fetchUser().test()
        testObserver2.awaitValue()

        value = Resource.fromApiResponse(testObserver2.value())
        assertEquals(Resource.Status.SUCCESS, value.status)
        assertNotNull(value.data)

        val testObserver3 = userAPI.fetchUser().test()
        testObserver3.awaitValue()

        value = Resource.fromApiResponse(testObserver3.value())
        assertEquals(Resource.Status.SUCCESS, value.status)
        assertNotNull(value.data)

        tearDown()
    }

    @Test
    fun testRequestsGetCancelledAfterRefreshingAccessTokenFails() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == TokenAPI.URL_TOKEN_REFRESH) {
                    return MockResponse()
                            .setResponseCode(401)
                            .setBody(readStringFromJson(app, R.raw.error_invalid_refresh_token))
                } else if (request?.path == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(401)
                            .setBody(readStringFromJson(app, R.raw.error_invalid_access_token))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val testObserver = userAPI.fetchUser().test()
        testObserver.awaitNextValue()

        assertNull(preferences.encryptedAccessToken)
        assertNull(preferences.encryptedRefreshToken)
        assertEquals(-1, preferences.accessTokenExpiry)

        var value = Resource.fromApiResponse(testObserver.value())
        assertEquals(Resource.Status.ERROR, value.status)
        assertNotNull(value.error)
        assertTrue(value.error is APIError)
        assertEquals(APIErrorType.INVALID_ACCESS_TOKEN, (value.error as APIError).type)

        val testObserver2 = userAPI.fetchUser().test()
        testObserver2.awaitValue()

        value = Resource.fromApiResponse(testObserver2.value())
        assertEquals(Resource.Status.ERROR, value.status)
        assertNotNull(value.error)
        assertTrue(value.error is APIError)
        assertEquals(APIErrorType.INVALID_ACCESS_TOKEN, (value.error as APIError).type)

        val testObserver3 = userAPI.fetchUser().test()
        testObserver3.awaitValue()

        value = Resource.fromApiResponse(testObserver3.value())
        assertEquals(Resource.Status.ERROR, value.status)
        assertNotNull(value.error)
        assertTrue(value.error is APIError)
        assertEquals(APIErrorType.INVALID_ACCESS_TOKEN, (value.error as APIError).type)

        tearDown()
    }
}