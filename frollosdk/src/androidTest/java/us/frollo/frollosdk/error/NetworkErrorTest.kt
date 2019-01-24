package us.frollo.frollosdk.error

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import us.frollo.frollosdk.FrolloSDK
import javax.net.ssl.SSLException

class NetworkErrorTest {

    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Before
    fun setUp() {
        FrolloSDK.app = app
    }

    @Test
    fun testLocalizedDescription() {
        val networkError = NetworkError(SSLException("SSL Error"))
        assertEquals(app.resources.getString(NetworkErrorType.INVALID_SSL.textResource), networkError.localizedDescription)
    }

    @Test
    fun testDebugDescription() {
        val networkError = NetworkError(SSLException("SSL Error"))
        val localizedDescription = app.resources.getString(NetworkErrorType.INVALID_SSL.textResource)
        val str = "NetworkError: INVALID_SSL: $localizedDescription | SSL Error"
        assertEquals(str, networkError.debugDescription)
    }

    @Test
    fun testNetworkErrorType() {
        val networkError = NetworkError(SSLException("SSL Error"))
        assertEquals(NetworkErrorType.INVALID_SSL, networkError.type)
    }
}