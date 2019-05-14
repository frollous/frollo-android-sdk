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
import us.frollo.frollosdk.mapping.toTransactionCategory
import us.frollo.frollosdk.model.testTransactionCategoryResponseData

class TransactionCategoryDaoTest {

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
        val data1 = testTransactionCategoryResponseData(transactionCategoryId = 100)
        val data2 = testTransactionCategoryResponseData(transactionCategoryId = 101)
        val data3 = testTransactionCategoryResponseData(transactionCategoryId = 102)
        val data4 = testTransactionCategoryResponseData(transactionCategoryId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactionCategories().insertAll(*list.map { it.toTransactionCategory() }.toList().toTypedArray())

        val testObserver = db.transactionCategories().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testLoadByTransactionCategoryId() {
        val data = testTransactionCategoryResponseData(transactionCategoryId = 102)
        val list = mutableListOf(testTransactionCategoryResponseData(transactionCategoryId = 101), data, testTransactionCategoryResponseData(transactionCategoryId = 103))
        db.transactionCategories().insertAll(*list.map { it.toTransactionCategory() }.toList().toTypedArray())

        val testObserver = db.transactionCategories().load(data.transactionCategoryId).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.transactionCategoryId, testObserver.value()?.transactionCategoryId)
    }

    @Test
    fun testInsertAll() {
        val data1 = testTransactionCategoryResponseData(transactionCategoryId = 100)
        val data2 = testTransactionCategoryResponseData(transactionCategoryId = 101)
        val data3 = testTransactionCategoryResponseData(transactionCategoryId = 102)
        val data4 = testTransactionCategoryResponseData(transactionCategoryId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactionCategories().insertAll(*list.map { it.toTransactionCategory() }.toList().toTypedArray())

        val testObserver = db.transactionCategories().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testTransactionCategoryResponseData()

        db.transactionCategories().insert(data.toTransactionCategory())

        val testObserver = db.transactionCategories().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(data.transactionCategoryId, testObserver.value()[0].transactionCategoryId)
    }

    @Test
    fun testGetStaleIds() {
        val data1 = testTransactionCategoryResponseData(transactionCategoryId = 100)
        val data2 = testTransactionCategoryResponseData(transactionCategoryId = 101)
        val data3 = testTransactionCategoryResponseData(transactionCategoryId = 102)
        val data4 = testTransactionCategoryResponseData(transactionCategoryId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactionCategories().insertAll(*list.map { it.toTransactionCategory() }.toList().toTypedArray())

        val staleIds = db.transactionCategories().getStaleIds(longArrayOf(100, 103)).sorted()
        assertEquals(2, staleIds.size)
        assertTrue(staleIds.containsAll(mutableListOf<Long>(101, 102)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testTransactionCategoryResponseData(transactionCategoryId = 100)
        val data2 = testTransactionCategoryResponseData(transactionCategoryId = 101)
        val data3 = testTransactionCategoryResponseData(transactionCategoryId = 102)
        val data4 = testTransactionCategoryResponseData(transactionCategoryId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactionCategories().insertAll(*list.map { it.toTransactionCategory() }.toList().toTypedArray())

        db.transactionCategories().deleteMany(longArrayOf(100, 103))

        val testObserver = db.transactionCategories().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testDelete() {
        val data1 = testTransactionCategoryResponseData(transactionCategoryId = 100)
        val data2 = testTransactionCategoryResponseData(transactionCategoryId = 101)
        val data3 = testTransactionCategoryResponseData(transactionCategoryId = 102)
        val data4 = testTransactionCategoryResponseData(transactionCategoryId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactionCategories().insertAll(*list.map { it.toTransactionCategory() }.toList().toTypedArray())

        db.transactionCategories().delete(100)

        val testObserver = db.transactionCategories().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testTransactionCategoryResponseData(transactionCategoryId = 100)
        val data2 = testTransactionCategoryResponseData(transactionCategoryId = 101)
        val data3 = testTransactionCategoryResponseData(transactionCategoryId = 102)
        val data4 = testTransactionCategoryResponseData(transactionCategoryId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactionCategories().insertAll(*list.map { it.toTransactionCategory() }.toList().toTypedArray())

        db.transactionCategories().clear()

        val testObserver = db.transactionCategories().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }
}