package us.frollo.frollosdk

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.error.FrolloSDKError

class FrolloSDKAndroidUnitTest {

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private val url = "https://api.example.com"

    @Before
    fun setUp() {
        FrolloSDK.app = app
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testGetAuthenticationFail() {
        assertFalse(FrolloSDK.isSetup)
        try {
            FrolloSDK.authentication
        } catch (e: FrolloSDKError) {
            assertEquals("SDK not setup", e.localizedMessage)
        }
    }

    @Test
    fun testSetupSuccess() {
        assertFalse(FrolloSDK.isSetup)
        FrolloSDK.setup(app, SetupParams.Builder().serverUrl(url).build()) { error ->
            assertNull(error)
            assertNotNull(FrolloSDK.authentication)
        }
        assertTrue(FrolloSDK.isSetup)
    }

    @Test
    fun testSetupFailEmptyServerUrl() {
        assertFalse(FrolloSDK.isSetup)
        try {
            FrolloSDK.setup(app, SetupParams.Builder().serverUrl(" ").build()) { }
        } catch (e: FrolloSDKError) {
            assertEquals("Server URL cannot be empty", e.localizedMessage)
        }
    }

    @Test
    fun testLogout() {
        //TODO: to be implemented
    }

    @Test
    fun testForcedLogout() {
        //TODO: to be implemented
    }

    @Test
    fun testReset() {
        //TODO: to be implemented
    }
}