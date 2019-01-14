package us.frollo.frollosdk.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.threetenabp.AndroidThreeTen
import com.jraska.livedata.test
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

import us.frollo.frollosdk.model.testUserResponseData

class SDKDatabaseTest {

    @get:Rule val testRule = InstantTaskExecutorRule()

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private lateinit var db: SDKDatabase

    @Before
    fun setUp() {
        AndroidThreeTen.init(app)
        db = SDKDatabase.getInstance(app)
        db.clearAllTables()
    }

    @After
    fun tearDown() {
        db.clearAllTables()
    }

    @Test
    fun testUserDao() {
        assertNotNull(db)

        val testObserver = db.users().load().test()
        testObserver.awaitValue()
        assertNull(testObserver.value())

        val dataIn = testUserResponseData()
        db.users().insert(dataIn)

        val testObserver2 = db.users().load().test()
        testObserver2.awaitValue()
        assertNotNull(testObserver2.value())
        assertEquals(dataIn.userId, testObserver2.value()?.userId)

        db.users().clear()
        val testObserver3 = db.users().load().test()
        testObserver3.awaitValue()
        assertNull(testObserver3.value())
    }
}