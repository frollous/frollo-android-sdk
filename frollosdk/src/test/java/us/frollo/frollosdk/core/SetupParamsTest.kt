package us.frollo.frollosdk.core

import org.junit.Assert.*
import org.junit.Test
import us.frollo.frollosdk.logging.LogLevel

class SetupParamsTest {

    @Test
    fun testBuildSetupParams() {
        val builder = SetupParams.Builder()
        val params = builder.serverUrl("https://api-test.frollo.us")
                .logLevel(LogLevel.DEBUG)
                .build()
        assertNotNull(params)
        assertEquals("https://api-test.frollo.us", params.serverUrl)
        assertEquals(LogLevel.DEBUG, params.logLevel)
    }
}