package us.frollo.frollosdk.data.local.dao

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.threetenabp.AndroidThreeTen
import com.jraska.livedata.test
import org.junit.After
import org.junit.Assert.*
import org.junit.Before

import org.junit.Rule
import org.junit.Test
import us.frollo.frollosdk.data.local.SDKDatabase
import us.frollo.frollosdk.mapping.toTransaction
import us.frollo.frollosdk.model.testTransactionResponseData

class TransactionDaoTest {

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
        val data1 = testTransactionResponseData(transactionId = 100)
        val data2 = testTransactionResponseData(transactionId = 101)
        val data3 = testTransactionResponseData(transactionId = 102)
        val data4 = testTransactionResponseData(transactionId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val testObserver = db.transactions().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testLoadByTransactionId() {
        val data = testTransactionResponseData(transactionId = 102)
        val list = mutableListOf(testTransactionResponseData(transactionId = 101), data, testTransactionResponseData(transactionId = 103))
        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val testObserver = db.transactions().load(data.transactionId).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.transactionId, testObserver.value()?.transactionId)
    }

    @Test
    fun testLoadByProviderAccountId() {
        val data1 = testTransactionResponseData(accountId = 1)
        val data2 = testTransactionResponseData(accountId = 1)
        val data3 = testTransactionResponseData(accountId = 2)
        val data4 = testTransactionResponseData(accountId = 1)
        val list = mutableListOf(data1, data2, data3, data4)
        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val testObserver = db.transactions().loadByAccountId(accountId = 1).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testInsertAll() {
        val data1 = testTransactionResponseData(transactionId = 100)
        val data2 = testTransactionResponseData(transactionId = 101)
        val data3 = testTransactionResponseData(transactionId = 102)
        val data4 = testTransactionResponseData(transactionId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val testObserver = db.transactions().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testTransactionResponseData()

        db.transactions().insert(data.toTransaction())

        val testObserver = db.transactions().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(data.transactionId, testObserver.value()[0].transactionId)
    }

    @Test
    fun testGetStaleIds() {
        val data1 = testTransactionResponseData(transactionId = 100)
        val data2 = testTransactionResponseData(transactionId = 101)
        val data3 = testTransactionResponseData(transactionId = 102)
        val data4 = testTransactionResponseData(transactionId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val staleIds = db.transactions().getStaleIds(longArrayOf(100, 103)).sorted()
        assertEquals(2, staleIds.size)
        assertTrue(staleIds.containsAll(mutableListOf<Long>(101, 102)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testTransactionResponseData(transactionId = 100)
        val data2 = testTransactionResponseData(transactionId = 101)
        val data3 = testTransactionResponseData(transactionId = 102)
        val data4 = testTransactionResponseData(transactionId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        db.transactions().deleteMany(longArrayOf(100, 103))

        val testObserver = db.transactions().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testDelete() {
        val data1 = testTransactionResponseData(transactionId = 100)
        val data2 = testTransactionResponseData(transactionId = 101)
        val data3 = testTransactionResponseData(transactionId = 102)
        val data4 = testTransactionResponseData(transactionId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        db.transactions().delete(100)

        val testObserver = db.transactions().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testDeleteByAccountIds() {
        val data1 = testTransactionResponseData(accountId = 1)
        val data2 = testTransactionResponseData(accountId = 2)
        val data3 = testTransactionResponseData(accountId = 3)
        val data4 = testTransactionResponseData(accountId = 3)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        db.transactions().deleteByAccountIds(longArrayOf(1, 3))

        val testObserver = db.transactions().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)
    }

    @Test
    fun testDeleteByAccountId() {
        val data1 = testTransactionResponseData(accountId = 1)
        val data2 = testTransactionResponseData(accountId = 2)
        val data3 = testTransactionResponseData(accountId = 2)
        val data4 = testTransactionResponseData(accountId = 1)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        db.transactions().deleteByAccountId(1)

        val testObserver = db.transactions().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testTransactionResponseData(transactionId = 100)
        val data2 = testTransactionResponseData(transactionId = 101)
        val data3 = testTransactionResponseData(transactionId = 102)
        val data4 = testTransactionResponseData(transactionId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        db.transactions().clear()

        val testObserver = db.transactions().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }
}