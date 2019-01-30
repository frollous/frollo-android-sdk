package us.frollo.frollosdk.extensions

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType

class ErrorExtensionTest {

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Before
    fun setup() {
        FrolloSDK.app = app
    }

    @Test
    fun testDataErrorToJson() {
        val error = DataError(DataErrorType.AUTHENTICATION, DataErrorSubType.MISSING_REFRESH_TOKEN)
        assertEquals("{\"type\":\"AUTHENTICATION\",\"sub_type\":\"MISSING_REFRESH_TOKEN\"}", error.toJson())
    }
}