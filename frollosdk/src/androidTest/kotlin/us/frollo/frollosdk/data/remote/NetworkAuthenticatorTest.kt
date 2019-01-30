package us.frollo.frollosdk.data.remote

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
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
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.data.remote.api.DeviceAPI
import us.frollo.frollosdk.data.remote.api.UserAPI
import us.frollo.frollosdk.error.*
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.wait

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
        preferences.resetAll()
    }

    @Test
    fun testPreemptiveAccessTokenRefresh() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == DeviceAPI.URL_TOKEN_REFRESH) {
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

        userAPI.fetchUser().enqueue { _, error ->
            assertNull(error)

            assertEquals(2, mockServer.requestCount)
            assertEquals("AValidAccessTokenFromHost", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("AValidRefreshTokenFromHost", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(1721259268, preferences.accessTokenExpiry)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testInvalidAccessTokenRefresh() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            var failedOnce = false

            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == DeviceAPI.URL_TOKEN_REFRESH) {
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

        userAPI.fetchUser().enqueue { _, error ->
            assertNull(error)

            assertEquals(3, mockServer.requestCount)
            assertEquals("AValidAccessTokenFromHost", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("AValidRefreshTokenFromHost", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(1721259268, preferences.accessTokenExpiry)
        }

        wait(3)

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

        userAPI.fetchUser().enqueue { _, error ->
            assertNotNull(error)

            assertEquals(1, mockServer.requestCount)

            assertTrue(error is DataError)
            assertEquals(DataErrorType.AUTHENTICATION, (error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, error.subType)

            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1, preferences.accessTokenExpiry)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRequestsGetRetriedAfterRefreshingAccessToken() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            var userRequestCount = 0

            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == DeviceAPI.URL_TOKEN_REFRESH) {
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

        userAPI.fetchUser().enqueue { response, error ->
            assertNull(error)

            assertNotNull(response)

            assertEquals("AValidAccessTokenFromHost", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("AValidRefreshTokenFromHost", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(1721259268, preferences.accessTokenExpiry)
        }

        userAPI.fetchUser().enqueue { response, error ->
            assertNull(error)

            assertNotNull(response)
        }

        userAPI.fetchUser().enqueue { response, error ->
            assertNull(error)

            assertNotNull(response)
        }

        wait(8)

        tearDown()
    }

    @Test
    fun testRequestsGetCancelledAfterRefreshingAccessTokenFails() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == DeviceAPI.URL_TOKEN_REFRESH) {
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

        userAPI.fetchUser().enqueue { response, error ->
            assertNotNull(error)

            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1, preferences.accessTokenExpiry)
        }

        userAPI.fetchUser().enqueue { response, error ->
            assertNotNull(error)
        }

        userAPI.fetchUser().enqueue { response, error ->
            assertNotNull(error)
        }

        wait(8)

        tearDown()
    }
}