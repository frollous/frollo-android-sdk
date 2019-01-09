package us.frollo.frollosdk.data.remote

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.threetenabp.AndroidThreeTen
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.data.remote.api.TokenAPI
import us.frollo.frollosdk.data.remote.api.UserAPI
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.model.testEmailLoginData
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.TestAPI
import us.frollo.frollosdk.testutils.get429Response
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

    private fun initSetup(url: String) {
        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url(url)

        FrolloSDK.app = app
        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        network = NetworkService(baseUrl.toString(), keystore, preferences)
        userAPI = network.create(UserAPI::class.java)
        testAPI = network.create(TestAPI::class.java)

        AndroidThreeTen.init(app)
    }

    private fun tearDown() {
        mockServer.shutdown()
        network.reset()
        preferences.reset()
    }

    @Test
    fun testRequestHeaders() {
        initSetup(UserAPI.URL_USER_DETAILS)

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val body = readStringFromJson(app, R.raw.user_details_complete)
        val mockedResponse = MockResponse()
                .setResponseCode(200)
                .setBody(body)
        mockServer.enqueue(mockedResponse)

        userAPI.fetchUser()

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
    fun testOTPHeaderAppendedToRegistrationRequest() {
        //TODO: to be implemented
    }

    @Test
    fun testOTPHeaderAppendedToResetPasswordRequest() {
        //TODO: to be implemented
    }

    @Test
    fun testAccessTokenHeaderAppendedToHostRequests() {
        initSetup(UserAPI.URL_USER_DETAILS)

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val body = readStringFromJson(app, R.raw.user_details_complete)
        val mockedResponse = MockResponse()
                .setResponseCode(200)
                .setBody(body)
        mockServer.enqueue(mockedResponse)

        userAPI.fetchUser()

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_USER_DETAILS, request.path)
        assertEquals("Bearer ExistingAccessToken", request.getHeader("Authorization"))

        tearDown()
    }

    @Test
    fun testRefreshTokenHeaderAppendedToRefreshRequests() {
        initSetup(TokenAPI.URL_TOKEN_REFRESH)

        val body = readStringFromJson(app, R.raw.refresh_token_valid)
        val mockedResponse = MockResponse()
                .setResponseCode(200)
                .setBody(body)
        mockServer.enqueue(mockedResponse)

        preferences.encryptedAccessToken = keystore.encrypt("InvalidAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")

        network.refreshTokens()

        val request = mockServer.takeRequest()
        assertEquals(TokenAPI.URL_TOKEN_REFRESH, request.path)
        assertEquals("Bearer ExistingRefreshToken", request.getHeader("Authorization"))

        tearDown()
    }

    @Test
    fun testNoHeaderAppendedToLoginRequest() {
        initSetup(UserAPI.URL_LOGIN)

        val body = readStringFromJson(app, R.raw.user_details_complete)
        val mockedResponse = MockResponse()
                .setResponseCode(200)
                .setBody(body)
        mockServer.enqueue(mockedResponse)

        userAPI.login(testEmailLoginData())

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_LOGIN, request.path)
        assertNull(request.getHeader("Authorization"))

        tearDown()
    }

    @Test
    fun testNoHeaderAppendedToExternalHostRequests() {
        //TODO: to be implemented
    }

    @Test
    fun testRateLimitRetries() {
        initSetup(UserAPI.URL_USER_DETAILS)

        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        mockServer.enqueue(get429Response())
        mockServer.enqueue(MockResponse().setResponseCode(200))

        val response = testAPI.testData().execute()
        assertTrue(response.isSuccessful)
        assertNull(response.body())
        assertEquals(2, mockServer.requestCount)

        tearDown()
    }

    @Test
    fun testAuthenticateRequestAppendExistingAccessToken() {
        initSetup(UserAPI.URL_USER_DETAILS)

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

    @Test
    fun testAuthenticateRequestAppendRefreshedAccessToken() {
        initSetup(UserAPI.URL_USER_DETAILS)

        val body = readStringFromJson(app, R.raw.refresh_token_valid)
        val mockedResponse = MockResponse()
                .setResponseCode(200)
                .setBody(body)
        mockServer.enqueue(mockedResponse)

        preferences.encryptedAccessToken = keystore.encrypt("InvalidAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) - 900

        val request = network.authenticateRequest(Request.Builder()
                .url("https://api.example.com")
                .build())
        assertNotNull(request)
        assertEquals("Bearer AValidAccessTokenFromHost", request.header("Authorization"))

        tearDown()
    }
}