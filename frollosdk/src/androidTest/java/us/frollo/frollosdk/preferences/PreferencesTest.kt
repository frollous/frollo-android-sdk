package us.frollo.frollosdk.preferences

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import us.frollo.frollosdk.auth.FeatureType
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.model.coredata.user.FeatureFlag

class PreferencesTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
    private lateinit var preferences: Preferences
    private val keyStore = Keystore()

    @Before
    fun setup() {
        preferences = Preferences(context)
        keyStore.setup()
    }

    @After
    fun tearDown() {
        preferences.reset()
        keyStore.reset()
    }

    @Test
    fun testSdkVersion() {
        assertNull(preferences.sdkVersion)
        preferences.sdkVersion = "1.0.0"
        assertEquals("1.0.0", preferences.sdkVersion)
    }

    @Test
    fun testSdkVersionHistory() {
        assertTrue(preferences.sdkVersionHistory.isEmpty())
        val versionHistory = mutableListOf("1.0.0")
        preferences.sdkVersionHistory = versionHistory
        assertEquals("1.0.0", preferences.sdkVersionHistory[0])
        versionHistory.add("1.0.1")
        preferences.sdkVersionHistory = versionHistory
        assertEquals("1.0.0", preferences.sdkVersionHistory[0])
        assertEquals("1.0.1", preferences.sdkVersionHistory[1])
    }

    @Test
    fun testLoggedIn() {
        assertFalse(preferences.loggedIn)
        preferences.loggedIn = true
        assertEquals(true, preferences.loggedIn)
    }

    @Test
    fun testFeatures() {
        assertTrue(preferences.features.isEmpty())
        val features = mutableListOf(FeatureFlag(feature = "aggregation", enabled = true))
        preferences.features = features
        assertEquals(true, preferences.isFeatureEnabled(FeatureType.AGGREGATION))
    }

    @Test
    fun testEncryptedRefreshToken() {
        assertNull(preferences.encryptedRefreshToken)
        val encToken = keyStore.encrypt("DummyRefreshToken")
        assertNotNull(encToken)
        preferences.encryptedRefreshToken = encToken
        assertEquals("DummyRefreshToken", keyStore.decrypt(preferences.encryptedRefreshToken))
    }

    @Test
    fun testEncryptedAccessToken() {
        assertNull(preferences.encryptedAccessToken)
        val encToken = keyStore.encrypt("DummyAccessToken")
        assertNotNull(encToken)
        preferences.encryptedAccessToken = encToken
        assertEquals("DummyAccessToken", keyStore.decrypt(preferences.encryptedAccessToken))
    }

    @Test
    fun testReset() {
        preferences.reset()
        assertNull(preferences.sdkVersion)
        assertTrue(preferences.sdkVersionHistory.isEmpty())
        assertFalse(preferences.loggedIn)
        assertTrue(preferences.features.isEmpty())
        assertNull(preferences.encryptedRefreshToken)
        assertNull(preferences.encryptedAccessToken)
    }
}