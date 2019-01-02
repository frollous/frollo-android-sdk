package us.frollo.frollosdk.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.threetenabp.AndroidThreeTen
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

import us.frollo.frollosdk.model.testDataUserResponse

class SDKDatabaseTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

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
        var user = db.users().load()
        assertNull(user)

        val dataIn = testDataUserResponse()
        db.users().insert(dataIn)

        val dataOut = db.users().load()
        assertNotNull(dataOut)
        assertEquals(dataIn.userId, dataOut?.userId)

        /*val liveData = db.users().loadAsLiveData()
        assertNotNull(liveData)
        val lifecycleRegistry = LifecycleRegistry(mock(LifecycleOwner::class.java))
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        val lifecycle = lifecycleRegistry as Lifecycle
        liveData.observe({lifecycle}) {
            assertEquals(dataIn.userId, it?.userId)
        }*/

        db.users().clear()
        user = db.users().load()
        assertNull(user)
    }

    @Test
    fun testDatabaseReset() {
        db.users().insert(testDataUserResponse())
        assertNotNull(db.users().load())

        db.reset()

        assertNull(db.users().load())
    }
}