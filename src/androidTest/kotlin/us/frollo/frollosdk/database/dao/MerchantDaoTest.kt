package us.frollo.frollosdk.database.dao

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
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.mapping.toMerchant
import us.frollo.frollosdk.model.testMerchantResponseData

class MerchantDaoTest {

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
    fun testLoadAll() {
        val data1 = testMerchantResponseData(merchantId = 100)
        val data2 = testMerchantResponseData(merchantId = 101)
        val data3 = testMerchantResponseData(merchantId = 102)
        val data4 = testMerchantResponseData(merchantId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.merchants().insertAll(*list.map { it.toMerchant() }.toList().toTypedArray())

        val testObserver = db.merchants().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testLoadByMerchantId() {
        val data = testMerchantResponseData(merchantId = 102)
        val list = mutableListOf(testMerchantResponseData(merchantId = 101), data, testMerchantResponseData(merchantId = 103))
        db.merchants().insertAll(*list.map { it.toMerchant() }.toList().toTypedArray())

        val testObserver = db.merchants().load(data.merchantId).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.merchantId, testObserver.value()?.merchantId)
    }

    @Test
    fun testInsertAll() {
        val data1 = testMerchantResponseData(merchantId = 100)
        val data2 = testMerchantResponseData(merchantId = 101)
        val data3 = testMerchantResponseData(merchantId = 102)
        val data4 = testMerchantResponseData(merchantId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.merchants().insertAll(*list.map { it.toMerchant() }.toList().toTypedArray())

        val testObserver = db.merchants().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testMerchantResponseData()

        db.merchants().insert(data.toMerchant())

        val testObserver = db.merchants().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(data.merchantId, testObserver.value()[0].merchantId)
    }

    @Test
    fun testGetStaleIds() {
        val data1 = testMerchantResponseData(merchantId = 100)
        val data2 = testMerchantResponseData(merchantId = 101)
        val data3 = testMerchantResponseData(merchantId = 102)
        val data4 = testMerchantResponseData(merchantId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.merchants().insertAll(*list.map { it.toMerchant() }.toList().toTypedArray())

        val staleIds = db.merchants().getStaleIds(longArrayOf(100, 103)).sorted()
        assertEquals(2, staleIds.size)
        assertTrue(staleIds.containsAll(mutableListOf<Long>(101, 102)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testMerchantResponseData(merchantId = 100)
        val data2 = testMerchantResponseData(merchantId = 101)
        val data3 = testMerchantResponseData(merchantId = 102)
        val data4 = testMerchantResponseData(merchantId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.merchants().insertAll(*list.map { it.toMerchant() }.toList().toTypedArray())

        db.merchants().deleteMany(longArrayOf(100, 103))

        val testObserver = db.merchants().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testDelete() {
        val data1 = testMerchantResponseData(merchantId = 100)
        val data2 = testMerchantResponseData(merchantId = 101)
        val data3 = testMerchantResponseData(merchantId = 102)
        val data4 = testMerchantResponseData(merchantId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.merchants().insertAll(*list.map { it.toMerchant() }.toList().toTypedArray())

        db.merchants().delete(100)

        val testObserver = db.merchants().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testMerchantResponseData(merchantId = 100)
        val data2 = testMerchantResponseData(merchantId = 101)
        val data3 = testMerchantResponseData(merchantId = 102)
        val data4 = testMerchantResponseData(merchantId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.merchants().insertAll(*list.map { it.toMerchant() }.toList().toTypedArray())

        db.merchants().clear()

        val testObserver = db.merchants().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }
}