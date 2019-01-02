package us.frollo.frollosdk.error

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import us.frollo.frollosdk.FrolloSDK

class DataErrorTest {

    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Before
    fun setUp() {
        FrolloSDK.app = app
    }

    @Test
    fun testLocalizedDescription() {
        var dataError = DataError(DataErrorType.DATABASE, DataErrorSubType.DISK_FULL)
        assertEquals(app.resources.getString(DataErrorSubType.DISK_FULL.textResource), dataError.localizedDescription)

        dataError = DataError(DataErrorType.DATABASE, DataErrorSubType.INVALID_DATA)
        assertEquals(app.resources.getString(DataErrorType.DATABASE.textResource), dataError.localizedDescription)
    }

    @Test
    fun testDebugDescription() {
        val dataError = DataError(DataErrorType.DATABASE, DataErrorSubType.DISK_FULL)
        val localizedDescription = app.resources.getString(DataErrorSubType.DISK_FULL.textResource)
        val str = "DataError: DATABASE.DISK_FULL: $localizedDescription"
        assertEquals(str, dataError.debugDescription)
    }

    @Test
    fun testDataErrorType() {
        val dataError = DataError(DataErrorType.DATABASE, DataErrorSubType.DISK_FULL)
        assertEquals(DataErrorType.DATABASE, dataError.type)
    }

    @Test
    fun testDataErrorSubType() {
        val dataError = DataError(DataErrorType.DATABASE, DataErrorSubType.DISK_FULL)
        assertEquals(DataErrorSubType.DISK_FULL, dataError.subType)
    }
}