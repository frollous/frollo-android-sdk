package us.frollo.frollosdk.database

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.threetenabp.AndroidThreeTen
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test

class SDKDatabaseTest {

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private lateinit var db: SDKDatabase

    @Before
    fun setUp() {
        AndroidThreeTen.init(app)
    }

    @Test
    fun testDBCreate() {
        db = SDKDatabase.getInstance(app)
        assertNotNull(db)
    }

    //TODO: Migration_1_2 test
}