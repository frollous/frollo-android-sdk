package us.frollo.frollosdk.base

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.error.FrolloSDKError

class ResultTest {

    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Before
    fun setup() {
        FrolloSDK.app = app
    }

    @Test
    fun testResultSuccess() {
        val result = Result.success()
        assertNotNull(result)
        assertEquals(Result.Status.SUCCESS, result.status)
        assertNull(result.error)
    }

    @Test
    fun testResultError() {
        val result = Result.error(FrolloSDKError(errorMessage = "Unauthorized"))
        assertNotNull(result)
        assertEquals(Result.Status.ERROR, result.status)
        assertNotNull(result.error)
        assertEquals("Unauthorized", result.error?.debugDescription)
        assertEquals("Unauthorized", result.error?.localizedDescription)
    }
}