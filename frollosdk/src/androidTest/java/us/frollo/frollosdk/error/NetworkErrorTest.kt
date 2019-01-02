package us.frollo.frollosdk.error

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import us.frollo.frollosdk.FrolloSDK

class NetworkErrorTest {

    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Before
    fun setUp() {
        FrolloSDK.app = app
    }

    @Test
    fun testLocalizedDescription() {
        val networkError = NetworkError(NetworkErrorType.INVALID_SSL)
        assertEquals(app.resources.getString(NetworkErrorType.INVALID_SSL.textResource), networkError.localizedDescription)
    }

    @Test
    fun testDebugDescription() {
        val networkError = NetworkError(NetworkErrorType.INVALID_SSL)
        val localizedDescription = app.resources.getString(NetworkErrorType.INVALID_SSL.textResource)
        val str = "NetworkError: INVALID_SSL: $localizedDescription"
        assertEquals(str, networkError.debugDescription)
    }

    @Test
    fun testNetworkErrorType() {
        val networkError = NetworkError(NetworkErrorType.INVALID_SSL)
        assertEquals(NetworkErrorType.INVALID_SSL, networkError.type)
    }
}