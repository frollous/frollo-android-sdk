package us.frollo.frollosdk

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.jraska.livedata.test

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.data.local.SDKDatabase
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.model.testUserResponseData
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.testutils.wait

class FrolloSDKAndroidUnitTest {

    @get:Rule val testRule = InstantTaskExecutorRule()

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private lateinit var preferences: Preferences
    private lateinit var database: SDKDatabase

    @Before
    fun resetSingletonByReflection() {
        val setup = FrolloSDK::class.java.getDeclaredField("_setup")
        setup.isAccessible = true
        setup.setBoolean(null, false)
    }

    private fun initSetup() {
        preferences = Preferences(app)
        database = SDKDatabase.getInstance(app)
    }

    private fun tearDown() {
        preferences.resetAll()
        database.clearAllTables()
    }

    @Test
    fun testSDKInitFailIfServerURLNotSet() {
        assertFalse(FrolloSDK.isSetup)

        try {
            FrolloSDK.setup(app, SetupParams.Builder().build()) { }
        } catch (e: FrolloSDKError) {
            assertEquals("Server URL cannot be empty", e.localizedMessage)
        }
    }

    @Test
    fun testSDKSetupSuccess() {
        val url = "https://api.example.com"

        assertFalse(FrolloSDK.isSetup)

        FrolloSDK.setup(app, SetupParams.Builder().serverUrl(url).build()) { error ->
            assertNull(error)

            assertTrue(FrolloSDK.isSetup)
            assertNotNull(FrolloSDK.authentication)
            assertNotNull(FrolloSDK.messages)
        }
    }

    @Test
    fun testSDKAuthenticationThrowsErrorBeforeSetup() {
        assertFalse(FrolloSDK.isSetup)

        try {
            FrolloSDK.authentication
        } catch (e: IllegalAccessException) {
            assertEquals("SDK not setup", e.localizedMessage)
        }
    }

    @Test
    fun testSDKMessagesThrowsErrorBeforeSetup() {
        assertFalse(FrolloSDK.isSetup)

        try {
            FrolloSDK.messages
        } catch (e: IllegalAccessException) {
            assertEquals("SDK not setup", e.localizedMessage)
        }
    }

    @Test
    fun testLogout() {
        initSetup()

        val url = "https://api.example.com"

        preferences.loggedIn = true
        preferences.encryptedAccessToken = "EncryptedAccessToken"
        preferences.encryptedRefreshToken = "EncryptedRefreshToken"
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        database.users().insert(testUserResponseData())

        val testObserver = database.users().load().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())

        FrolloSDK.setup(app, SetupParams.Builder().serverUrl(url).build()) { error ->
            assertNull(error)

            FrolloSDK.logout { err ->
                assertNull(err)

                assertFalse(preferences.loggedIn)
                assertNull(preferences.encryptedAccessToken)
                assertNull(preferences.encryptedRefreshToken)
                assertEquals(-1, preferences.accessTokenExpiry)

                val testObserver2 = database.users().load().test()
                testObserver2.awaitValue()
                assertNull(testObserver2.value())
            }
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testForcedLogout() {
        initSetup()

        val url = "https://api.example.com"

        preferences.loggedIn = true
        preferences.encryptedAccessToken = "EncryptedAccessToken"
        preferences.encryptedRefreshToken = "EncryptedRefreshToken"
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        database.users().insert(testUserResponseData())

        val testObserver = database.users().load().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())

        FrolloSDK.setup(app, SetupParams.Builder().serverUrl(url).build()) { error ->
            assertNull(error)

            FrolloSDK.forcedLogout()

            wait(3)

            assertFalse(preferences.loggedIn)
            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1, preferences.accessTokenExpiry)

            val testObserver2 = database.users().load().test()
            testObserver2.awaitValue()
            assertNull(testObserver2.value())
        }

        tearDown()
    }

    @Test
    fun testSDKResetSuccess() {
        initSetup()

        val url = "https://api.example.com"

        preferences.loggedIn = true
        preferences.encryptedAccessToken = "EncryptedAccessToken"
        preferences.encryptedRefreshToken = "EncryptedRefreshToken"
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        database.users().insert(testUserResponseData())

        val testObserver = database.users().load().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())

        FrolloSDK.setup(app, SetupParams.Builder().serverUrl(url).build()) { error ->
            assertNull(error)

            FrolloSDK.reset { err ->
                assertNull(err)

                assertFalse(preferences.loggedIn)
                assertNull(preferences.encryptedAccessToken)
                assertNull(preferences.encryptedRefreshToken)
                assertEquals(-1, preferences.accessTokenExpiry)

                val testObserver2 = database.users().load().test()
                testObserver2.awaitValue()
                assertNull(testObserver2.value())
            }
        }

        wait(3)

        tearDown()
    }
}