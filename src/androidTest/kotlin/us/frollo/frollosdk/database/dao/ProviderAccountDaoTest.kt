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
import us.frollo.frollosdk.mapping.toProviderAccount
import us.frollo.frollosdk.model.testProviderAccountResponseData

class ProviderAccountDaoTest {

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
        val data1 = testProviderAccountResponseData(providerAccountId = 1)
        val data2 = testProviderAccountResponseData(providerAccountId = 2)
        val data3 = testProviderAccountResponseData(providerAccountId = 3)
        val data4 = testProviderAccountResponseData(providerAccountId = 4)
        val list = mutableListOf(data1, data2, data3, data4)

        db.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        val testObserver = db.providerAccounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testLoadByProviderAccountId() {
        val data = testProviderAccountResponseData(providerAccountId = 102)
        val list = mutableListOf(testProviderAccountResponseData(providerAccountId = 101), data, testProviderAccountResponseData(providerAccountId = 103))
        db.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        val testObserver = db.providerAccounts().load(data.providerAccountId).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.providerAccountId, testObserver.value()?.providerAccountId)
    }

    @Test
    fun testLoadByProviderId() {
        val data1 = testProviderAccountResponseData(providerId = 1)
        val data2 = testProviderAccountResponseData(providerId = 2)
        val data3 = testProviderAccountResponseData(providerId = 1)
        val data4 = testProviderAccountResponseData(providerId = 1)
        val list = mutableListOf(data1, data2, data3, data4)
        db.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        val testObserver = db.providerAccounts().loadByProviderId(providerId = 1).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testInsertAll() {
        val data1 = testProviderAccountResponseData(providerAccountId = 1)
        val data2 = testProviderAccountResponseData(providerAccountId = 2)
        val data3 = testProviderAccountResponseData(providerAccountId = 3)
        val data4 = testProviderAccountResponseData(providerAccountId = 4)
        val list = mutableListOf(data1, data2, data3, data4)

        db.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        val testObserver = db.providerAccounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testProviderAccountResponseData()

        db.providerAccounts().insert(data.toProviderAccount())

        val testObserver = db.providerAccounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(data.providerAccountId, testObserver.value()[0].providerAccountId)
    }

    @Test
    fun testGetStaleIds() {
        val data1 = testProviderAccountResponseData(providerAccountId = 100)
        val data2 = testProviderAccountResponseData(providerAccountId = 101)
        val data3 = testProviderAccountResponseData(providerAccountId = 102)
        val data4 = testProviderAccountResponseData(providerAccountId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        val staleIds = db.providerAccounts().getStaleIds(longArrayOf(100, 103)).sorted()
        assertEquals(2, staleIds.size)
        assertTrue(staleIds.containsAll(mutableListOf<Long>(101, 102)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testProviderAccountResponseData(providerAccountId = 100)
        val data2 = testProviderAccountResponseData(providerAccountId = 101)
        val data3 = testProviderAccountResponseData(providerAccountId = 102)
        val data4 = testProviderAccountResponseData(providerAccountId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        db.providerAccounts().deleteMany(longArrayOf(100, 103))

        val testObserver = db.providerAccounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testDelete() {
        val data1 = testProviderAccountResponseData(providerAccountId = 100)
        val data2 = testProviderAccountResponseData(providerAccountId = 101)
        val data3 = testProviderAccountResponseData(providerAccountId = 102)
        val data4 = testProviderAccountResponseData(providerAccountId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        db.providerAccounts().delete(100)

        val testObserver = db.providerAccounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testProviderAccountResponseData(providerAccountId = 100)
        val data2 = testProviderAccountResponseData(providerAccountId = 101)
        val data3 = testProviderAccountResponseData(providerAccountId = 102)
        val data4 = testProviderAccountResponseData(providerAccountId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        db.providerAccounts().clear()

        val testObserver = db.providerAccounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }
}