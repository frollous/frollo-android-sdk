package us.frollo.frollosdk.authentication

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.model.oauth.OAuthTokenResponse
import us.frollo.frollosdk.preferences.Preferences

class AuthTokenTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
    private lateinit var preferences: Preferences
    private val keyStore = Keystore()
    private lateinit var authToken: AuthToken

    @Before
    fun setup() {
        preferences = Preferences(context)
        keyStore.setup()
        authToken = AuthToken(keyStore, preferences)
        authToken.clearTokens()
    }

    @After
    fun tearDown() {
        preferences.resetAll()
        keyStore.reset()
        authToken.clearTokens()
    }

    @Test
    fun testAccessToken() {
        assertNull(authToken.getAccessToken())
        val encToken = keyStore.encrypt("DummyAccessToken")
        assertNotNull(encToken)
        preferences.encryptedAccessToken = encToken
        assertEquals("DummyAccessToken", authToken.getAccessToken())
    }

    @Test
    fun testRefreshToken() {
        assertNull(authToken.getRefreshToken())
        val encToken = keyStore.encrypt("DummyRefreshToken")
        assertNotNull(encToken)
        preferences.encryptedRefreshToken = encToken
        assertEquals("DummyRefreshToken", authToken.getRefreshToken())
    }

    @Test
    fun testAccessTokenExpiry() {
        assertEquals(-1, authToken.getAccessTokenExpiry())
        preferences.accessTokenExpiry = 14529375950
        assertEquals(14529375950, authToken.getAccessTokenExpiry())
    }

    @Test
    fun testSaveTokens() {
        assertNull(authToken.getRefreshToken())
        assertNull(authToken.getAccessToken())
        assertEquals(-1, authToken.getAccessTokenExpiry())
        authToken.saveTokens(OAuthTokenResponse(
                refreshToken = "IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk",
                accessToken = "MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3",
                createdAt = 2550792999,
                expiresIn = 1800,
                tokenType = "Bearer"))
        assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", authToken.getAccessToken())
        assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", authToken.getRefreshToken())
        assertEquals(2550794799, authToken.getAccessTokenExpiry())
        assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keyStore.decrypt(preferences.encryptedAccessToken))
        assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keyStore.decrypt(preferences.encryptedRefreshToken))
        assertEquals(2550794799, preferences.accessTokenExpiry)
    }

    @Test
    fun testClearTokens() {
        authToken.clearTokens()
        assertNull(authToken.getAccessToken())
        assertNull(authToken.getRefreshToken())
        assertNull(preferences.encryptedAccessToken)
        assertNull(preferences.encryptedRefreshToken)
        assertEquals(-1, authToken.getAccessTokenExpiry())
        assertEquals(-1, preferences.accessTokenExpiry)
    }
}