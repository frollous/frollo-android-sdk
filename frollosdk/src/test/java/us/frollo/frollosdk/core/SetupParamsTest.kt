package us.frollo.frollosdk.core

import android.util.Log
import org.junit.Assert.*
import org.junit.Test

class SetupParamsTest {

    @Test
    fun testBuildSetupParams() {
        val builder = SetupParams.Builder()
        val params = builder.serverUrl("https://api-test.frollo.us")
                .logLevel(Log.DEBUG)
                .build()
        assertNotNull(params)
        assertEquals("https://api-test.frollo.us", params.serverUrl)
        assertEquals(Log.DEBUG, params.logLevel)
    }
}