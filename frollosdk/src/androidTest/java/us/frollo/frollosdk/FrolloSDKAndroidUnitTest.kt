package us.frollo.frollosdk

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import us.frollo.frollosdk.core.SetupParams

class FrolloSDKAndroidUnitTest {

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private val url = "https://api.example.com"

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testGetAuthenticationFail() {
        try {
            FrolloSDK.authentication
        } catch (e: IllegalAccessException) {
            Assert.assertEquals("SDK not setup", e.localizedMessage)
        }
    }

    @Test
    fun testSetupSuccess() {
        FrolloSDK.setup(app, SetupParams.Builder().serverUrl(url).build()) { error ->
            Assert.assertNull(error)
            Assert.assertEquals(url, FrolloSDK.serverUrl)
            Assert.assertNotNull(FrolloSDK.authentication)
        }
    }

    @Test
    fun testSetupFailEmptyServerUrl() {
        try {
            FrolloSDK.setup(app, SetupParams.Builder().serverUrl(" ").build()) { }
        } catch (e: IllegalArgumentException) {
            Assert.assertEquals("Server URL cannot be empty", e.localizedMessage)
        }
    }
}