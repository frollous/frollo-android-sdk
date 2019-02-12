package us.frollo.frollosdk.auth

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.model.api.user.TokenResponse
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
        authToken.saveTokens(TokenResponse(refreshToken = "DummyRefreshToken", accessToken = "DummyAccessToken", accessTokenExp = 14529375950))
        assertEquals("DummyAccessToken", authToken.getAccessToken())
        assertEquals("DummyRefreshToken", authToken.getRefreshToken())
        assertEquals(14529375950, authToken.getAccessTokenExpiry())
        assertEquals("DummyAccessToken", keyStore.decrypt(preferences.encryptedAccessToken))
        assertEquals("DummyRefreshToken", keyStore.decrypt(preferences.encryptedRefreshToken))
        assertEquals(14529375950, preferences.accessTokenExpiry)
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