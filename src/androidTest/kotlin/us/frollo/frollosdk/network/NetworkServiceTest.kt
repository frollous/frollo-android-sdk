package us.frollo.frollosdk.network

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.threetenabp.AndroidThreeTen
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.authentication.OAuth
import us.frollo.frollosdk.core.testSDKConfig
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.model.oauth.OAuthTokenResponse
import us.frollo.frollosdk.network.api.TokenAPI
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath

class NetworkServiceTest {

    companion object {
        private const val TOKEN_URL = "token/"
    }

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    private lateinit var mockTokenServer: MockWebServer
    private lateinit var keystore: Keystore
    private lateinit var preferences: Preferences
    private lateinit var network: NetworkService

    @Before
    fun setUp() {
        mockTokenServer = MockWebServer()
        mockTokenServer.start()
        val baseUrl = mockTokenServer.url("/$TOKEN_URL")
        val config = testSDKConfig(tokenUrl = baseUrl.toString())

        FrolloSDK.app = app
        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        val oAuth = OAuth(config = config)
        network = NetworkService(oAuth = oAuth, keystore = keystore, pref = preferences)

        AndroidThreeTen.init(app)
    }

    @After
    fun tearDown() {
        mockTokenServer.shutdown()
        network.reset()
        preferences.resetAll()
    }

    @Test
    fun testHasTokens() {
        assertFalse(network.hasTokens())

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = 14529375950

        assertTrue(network.hasTokens())
    }

    @Test
    fun testHandleTokens() {
        assertNull(preferences.encryptedAccessToken)
        assertNull(preferences.encryptedRefreshToken)
        assertEquals(-1, preferences.accessTokenExpiry)

        network.handleTokens(OAuthTokenResponse(
                refreshToken = "IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk",
                accessToken = "MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3",
                createdAt = 2550792999,
                expiresIn = 1800,
                tokenType = "Bearer"))

        assertNotNull(preferences.encryptedAccessToken)
        assertNotNull(preferences.encryptedRefreshToken)
        assertEquals(2550794799, preferences.accessTokenExpiry)
    }

    @Test
    fun testForceRefreshingAccessTokens() {
        mockTokenServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "token/") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = 14529375950

        val newAccessToken = network.refreshTokens()

        assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", newAccessToken)
        assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keystore.decrypt(preferences.encryptedAccessToken))
        assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keystore.decrypt(preferences.encryptedRefreshToken))
        assertEquals(2550794799, preferences.accessTokenExpiry)
    }

    @Test
    fun testAuthenticateRequest() {
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val request = network.authenticateRequest(Request.Builder()
                .url("http://api.example.com/")
                .build())
        assertNotNull(request)
        assertEquals("http://api.example.com/", request.url().toString())
    }

    @Test
    fun testReset() {
        network.invalidTokenRetries = 6
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = 14529375950
        assertNotNull(preferences.encryptedAccessToken)
        assertNotNull(preferences.encryptedRefreshToken)
        assertNotEquals(-1, preferences.accessTokenExpiry)

        network.reset()

        assertEquals(0, network.invalidTokenRetries)
        assertNull(preferences.encryptedAccessToken)
        assertNull(preferences.encryptedRefreshToken)
        assertEquals(-1, preferences.accessTokenExpiry)
    }

    //TODO: SSL Pinning Tests
}