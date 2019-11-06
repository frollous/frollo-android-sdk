/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.frollo.frollosdk.database.dao

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.threetenabp.AndroidThreeTen
import com.jraska.livedata.test
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
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
    fun testGetIds() {
        val data1 = testMerchantResponseData(merchantId = 100)
        val data2 = testMerchantResponseData(merchantId = 101)
        val data3 = testMerchantResponseData(merchantId = 102)
        val data4 = testMerchantResponseData(merchantId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.merchants().insertAll(*list.map { it.toMerchant() }.toList().toTypedArray())

        val staleIds = db.merchants().getIds().sorted()
        assertEquals(4, staleIds.size)
        assertTrue(staleIds.containsAll(mutableListOf<Long>(100, 101, 102, 103)))
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

    @Test
    fun testCount() {
        val data1 = testMerchantResponseData(merchantId = 100)
        val data2 = testMerchantResponseData(merchantId = 101)
        val data3 = testMerchantResponseData(merchantId = 102)
        val data4 = testMerchantResponseData(merchantId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.merchants().insertAll(*list.map { it.toMerchant() }.toList().toTypedArray())
        assertEquals(db.merchants().getMerchantsCount(), 4L)
    }

    @Test
    fun testGetIdsByOffset() {
        val data1 = testMerchantResponseData(merchantId = 100)
        val data2 = testMerchantResponseData(merchantId = 101)
        val data3 = testMerchantResponseData(merchantId = 102)
        val data4 = testMerchantResponseData(merchantId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.merchants().insertAll(*list.map { it.toMerchant() }.toList().toTypedArray())
        val data = db.merchants().getIdsByOffset(2, 2)
        assertEquals(data[0], 102L)
        assertEquals(data[1], 103L)
    }
}