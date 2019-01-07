package us.frollo.frollosdk.data.remote

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import us.frollo.frollosdk.auth.AuthToken
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.preferences.Preferences

class NetworkHelperTest {

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    private lateinit var keystore: Keystore
    private lateinit var preferences: Preferences
    private lateinit var authToken: AuthToken

    @Before
    fun setUp() {
        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        authToken = AuthToken(keystore, preferences)
    }

    @After
    fun tearDown() {
        keystore.reset()
        preferences.reset()
        authToken.clearTokens()
    }

    @Test
    fun testAccessToken() {
        val networkHelper = NetworkHelper(authToken)
        assertEquals("Bearer null", networkHelper.accessToken)
        preferences.encryptedAccessToken = keystore.encrypt("DummyAccessToken")
        assertEquals("Bearer DummyAccessToken", networkHelper.accessToken)
    }

    @Test
    fun testRefreshToken() {
        val networkHelper = NetworkHelper(authToken)
        assertEquals("Bearer null", networkHelper.refreshToken)
        preferences.encryptedRefreshToken = keystore.encrypt("DummyRefreshToken")
        assertEquals("Bearer DummyRefreshToken", networkHelper.refreshToken)
    }

    @Test
    fun testBundleId() {
        val networkHelper = NetworkHelper(authToken)
        assertNotNull(networkHelper.bundleId)
    }

    @Test
    fun testSoftwareVersion() {
        val networkHelper = NetworkHelper(authToken)
        assertNotNull(networkHelper.softwareVersion)
    }

    @Test
    fun testDeviceVersion() {
        val networkHelper = NetworkHelper(authToken)
        assertNotNull(networkHelper.deviceVersion)
    }

    @Test
    fun testUserAgent() {
        val networkHelper = NetworkHelper(authToken)
        assertNotNull(networkHelper.userAgent)
    }

    @Test
    fun testOtp() {
        val networkHelper = NetworkHelper(authToken)
        assertNotNull(networkHelper.otp)
    }
}