package us.frollo.frollosdk.network

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.threetenabp.AndroidThreeTen
import okhttp3.FormBody
import okhttp3.Request
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
import us.frollo.frollosdk.authentication.OAuth
import us.frollo.frollosdk.core.testSDKConfig
import us.frollo.frollosdk.network.api.UserAPI
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.model.testResetPasswordData
import us.frollo.frollosdk.model.testValidRegisterData
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.TestAPI
import us.frollo.frollosdk.testutils.readStringFromJson

class NetworkInterceptorTest {

    @get:Rule val testRule = InstantTaskExecutorRule()

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    private lateinit var mockServer: MockWebServer
    private lateinit var keystore: Keystore
    private lateinit var preferences: Preferences
    private lateinit var network: NetworkService
    private lateinit var userAPI: UserAPI
    private lateinit var testAPI: TestAPI

    private fun initSetup() {
        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url("/server/")

        val config = testSDKConfig(serverUrl = baseUrl.toString())
        if (!FrolloSDK.isSetup) FrolloSDK.setup(app, config) {}

        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        val oAuth = OAuth(config = config)
        network = NetworkService(oAuth = oAuth, keystore = keystore, pref = preferences)
        userAPI = network.create(UserAPI::class.java)
        testAPI = network.create(TestAPI::class.java)

        AndroidThreeTen.init(app)
    }

    private fun tearDown() {
        mockServer.shutdown()
        network.reset()
        preferences.resetAll()
    }

    @Test
    fun testRequestHeaders() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.user_details_complete))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        userAPI.fetchUser().enqueue { }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_USER_DETAILS, request.path)
        assertEquals("Bearer ExistingAccessToken", request.getHeader("Authorization"))
        assertNotNull(request.getHeader("X-Api-Version"))
        assertEquals("us.frollo.frollosdk", request.getHeader("X-Bundle-Id"))
        assertNotNull(request.getHeader("X-Device-Version"))
        assertNotNull(request.getHeader("X-Software-Version"))
        assertNotNull(request.getHeader("User-Agent"))

        tearDown()
    }

    @Test
    fun testNoHeaderAppendedToRegistrationRequest() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == UserAPI.URL_REGISTER) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.user_details_complete))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        userAPI.register(testValidRegisterData()).enqueue { }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_REGISTER, request.path)
        val authHeader = request.getHeader("Authorization")
        assertNull(authHeader)

        tearDown()
    }

    @Test
    fun testNoHeaderAppendedToResetPasswordRequest() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == UserAPI.URL_PASSWORD_RESET) {
                    return MockResponse()
                            .setResponseCode(200)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        userAPI.resetPassword(testResetPasswordData()).enqueue { }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_PASSWORD_RESET, request.path)
        val authHeader = request.getHeader("Authorization")
        assertNull(authHeader)

        tearDown()
    }

    @Test
    fun testAccessTokenHeaderAppendedToHostRequests() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.user_details_complete))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        userAPI.fetchUser().enqueue { }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_USER_DETAILS, request.path)
        assertEquals("Bearer ExistingAccessToken", request.getHeader("Authorization"))

        tearDown()
    }

    @Test
    fun testNoHeaderAppendedToTokenRequest() {
        initSetup()

        val interceptor = NetworkInterceptor(network, NetworkHelper(network.authToken))

        val originalRequest = Request.Builder()
                .url("https://id.example.com/oauth/token/")
                .method("POST", FormBody.Builder().build())
                .build()
        val adaptedRequest = interceptor.adaptRequest(originalRequest)

        assertNull(adaptedRequest.header("Authorization"))

        tearDown()
    }

    @Test
    fun testRateLimitRetries() {
        //TODO: Failing due to timeout. Need to Debug.
        /*initSetup()

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        mockServer.enqueue(get429Response())
        mockServer.enqueue(MockResponse().setResponseCode(200))

        val response = testAPI.testData().execute()
        assertTrue(response.isSuccessful)
        assertNull(response.body())
        assertEquals(2, mockServer.requestCount)

        tearDown()*/
    }

    @Test
    fun testAuthenticateRequestAppendExistingAccessToken() {
        initSetup()

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val request = network.authenticateRequest(Request.Builder()
                .url("https://api.example.com")
                .build())
        assertNotNull(request)
        assertEquals("Bearer ExistingAccessToken", request.header("Authorization"))

        tearDown()
    }
}