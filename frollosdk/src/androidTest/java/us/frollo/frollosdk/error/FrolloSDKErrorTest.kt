package us.frollo.frollosdk.error

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.R

class FrolloSDKErrorTest {

    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Before
    fun setUp() {
        FrolloSDK.app = app
    }

    @Test
    fun testLocalizedDescription() {
        var sdkError = FrolloSDKError()
        assertEquals(app.resources.getString(R.string.FrolloSDK_Error_Generic_UnknownError), sdkError.localizedDescription)

        sdkError = FrolloSDKError("Frollo SDK Error")
        assertEquals("Frollo SDK Error", sdkError.localizedDescription)
    }

    @Test
    fun testDebugDescription() {
        var sdkError = FrolloSDKError()
        assertEquals(app.resources.getString(R.string.FrolloSDK_Error_Generic_UnknownError), sdkError.debugDescription)

        sdkError = FrolloSDKError("Frollo SDK Error")
        assertEquals("Frollo SDK Error", sdkError.debugDescription)
    }
}