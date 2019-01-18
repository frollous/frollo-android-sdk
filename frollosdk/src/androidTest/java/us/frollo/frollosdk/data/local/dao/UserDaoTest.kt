package us.frollo.frollosdk.data.local.dao

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
import us.frollo.frollosdk.data.local.SDKDatabase
import us.frollo.frollosdk.model.testModifyUserResponseData

import us.frollo.frollosdk.model.testUserResponseData

class UserDaoTest {

    @get:Rule val testRule = InstantTaskExecutorRule()

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private val db = SDKDatabase.getInstance(app)

    @Before
    fun setUp() {
        AndroidThreeTen.init(app)
    }

    @After
    fun tearDown() {
        db.clearAllTables()
    }

    @Test
    fun testLoad() {
        val testObserver = db.users().load().test()
        testObserver.awaitValue()
        assertNull(testObserver.value())

        db.users().insert(testUserResponseData())

        val testObserver2 = db.users().load().test()
        testObserver2.awaitValue()
        assertNotNull(testObserver2.value())
    }

    @Test
    fun testInsert() {
        val data = testUserResponseData()
        db.users().insert(data)

        val testObserver = db.users().load().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.userId, testObserver.value()?.userId)

        db.users().insert(data.testModifyUserResponseData("New first name"))

        val testObserver2 = db.users().load().test()
        testObserver2.awaitValue()
        assertNotNull(testObserver2.value())
        assertEquals(data.userId, testObserver2.value()?.userId)
        assertEquals("New first name", testObserver2.value()?.firstName)
    }

    @Test
    fun testClear() {
        val data = testUserResponseData()
        db.users().insert(data)

        db.users().clear()
        val testObserver = db.users().load().test()
        testObserver.awaitValue()
        assertNull(testObserver.value())
    }
}