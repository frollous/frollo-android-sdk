package us.frollo.frollosdk.data.remote

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.threetenabp.AndroidThreeTen
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.data.remote.api.TokenAPI
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.model.api.user.TokenResponse
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson

class NetworkServiceTest {

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    private lateinit var mockServer: MockWebServer
    private lateinit var keystore: Keystore
    private lateinit var preferences: Preferences
    private lateinit var network: NetworkService

    @Before
    fun setUp() {
        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url(TokenAPI.URL_TOKEN_REFRESH)

        FrolloSDK.app = app
        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        network = NetworkService(baseUrl.toString(), keystore, preferences)

        AndroidThreeTen.init(app)
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
        network.reset()
        preferences.reset()
    }

    @Test
    fun testHandleTokens() {
        assertNull(preferences.encryptedAccessToken)
        assertNull(preferences.encryptedRefreshToken)
        assertEquals(-1, preferences.accessTokenExpiry)

        network.handleTokens(TokenResponse(refreshToken = "ValidRefreshToken", accessToken = "ValidAccessToken", accessTokenExp = 1234567890))

        assertNotNull(preferences.encryptedAccessToken)
        assertNotNull(preferences.encryptedRefreshToken)
        assertEquals(1234567890, preferences.accessTokenExpiry)
    }

    @Test
    fun testForceRefreshingAccessTokens() {
        val body = readStringFromJson(app, R.raw.refresh_token_valid)
        val mockedResponse = MockResponse()
                .setResponseCode(200)
                .setBody(body)
        mockServer.enqueue(mockedResponse)

        preferences.encryptedAccessToken = keystore.encrypt("InvalidAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ValidRefreshToken")
        preferences.accessTokenExpiry = 14529375950

        val newAccessToken = network.refreshTokens()

        assertEquals("AValidAccessTokenFromHost", newAccessToken)
        assertEquals("AValidAccessTokenFromHost", keystore.decrypt(preferences.encryptedAccessToken))
        assertEquals("AValidRefreshTokenFromHost", keystore.decrypt(preferences.encryptedRefreshToken))
        assertEquals(1721259268, preferences.accessTokenExpiry)

        val request = mockServer.takeRequest()
        assertEquals(TokenAPI.URL_TOKEN_REFRESH, request.path)
    }

    @Test
    fun testForceRefreshingInvalidAccessTokens() {
        //TODO: to be implemented
    }

    @Test
    fun testAuthenticateRequest() {
        preferences.encryptedAccessToken = keystore.encrypt("ValidAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ValidRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val request = network.authenticateRequest(Request.Builder()
                .url("http://api.example.com/")
                .build())
        assertNotNull(request)
        assertEquals("http://api.example.com/", request.url().toString())
    }

    @Test
    fun testReset() {
        preferences.encryptedAccessToken = keystore.encrypt("ValidAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ValidRefreshToken")
        preferences.accessTokenExpiry = 14529375950
        assertNotNull(preferences.encryptedAccessToken)
        assertNotNull(preferences.encryptedRefreshToken)
        assertNotEquals(-1, preferences.accessTokenExpiry)

        network.reset()

        assertNull(preferences.encryptedAccessToken)
        assertNull(preferences.encryptedRefreshToken)
        assertEquals(-1, preferences.accessTokenExpiry)
    }

    //TODO: SSL Pinning Tests
}