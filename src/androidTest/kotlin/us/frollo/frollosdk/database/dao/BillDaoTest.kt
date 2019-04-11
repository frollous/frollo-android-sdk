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
import org.junit.Assert.*
import org.junit.Before

import org.junit.Rule
import org.junit.Test
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.mapping.*
import us.frollo.frollosdk.model.*

class BillDaoTest {

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
        val data1 = testBillResponseData(billId = 100)
        val data2 = testBillResponseData(billId = 101)
        val data3 = testBillResponseData(billId = 102)
        val data4 = testBillResponseData(billId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.bills().insertAll(*list.map { it.toBill() }.toList().toTypedArray())

        val testObserver = db.bills().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testLoadByBillId() {
        val data = testBillResponseData(billId = 102)
        val list = mutableListOf(testBillResponseData(billId = 101), data, testBillResponseData(billId = 103))
        db.bills().insertAll(*list.map { it.toBill() }.toList().toTypedArray())

        val testObserver = db.bills().load(data.billId).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.billId, testObserver.value()?.billId)
    }

    @Test
    fun testInsertAll() {
        val data1 = testBillResponseData(billId = 100)
        val data2 = testBillResponseData(billId = 101)
        val data3 = testBillResponseData(billId = 102)
        val data4 = testBillResponseData(billId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.bills().insertAll(*list.map { it.toBill() }.toList().toTypedArray())

        val testObserver = db.bills().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testBillResponseData()

        db.bills().insert(data.toBill())

        val testObserver = db.bills().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(data.billId, testObserver.value()[0].billId)
    }

    @Test
    fun testGetStaleIds() {
        val data1 = testBillResponseData(billId = 100)
        val data2 = testBillResponseData(billId = 101)
        val data3 = testBillResponseData(billId = 102)
        val data4 = testBillResponseData(billId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.bills().insertAll(*list.map { it.toBill() }.toList().toTypedArray())

        val staleIds = db.bills().getStaleIds(longArrayOf(100, 103)).sorted()

        assertEquals(2, staleIds.size)
        assertTrue(staleIds.containsAll(mutableListOf<Long>(101, 102)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testBillResponseData(billId = 100)
        val data2 = testBillResponseData(billId = 101)
        val data3 = testBillResponseData(billId = 102)
        val data4 = testBillResponseData(billId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.bills().insertAll(*list.map { it.toBill() }.toList().toTypedArray())

        db.bills().deleteMany(longArrayOf(100, 103))

        val testObserver = db.bills().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testDelete() {
        val data1 = testBillResponseData(billId = 100)
        val data2 = testBillResponseData(billId = 101)
        val data3 = testBillResponseData(billId = 102)
        val data4 = testBillResponseData(billId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.bills().insertAll(*list.map { it.toBill() }.toList().toTypedArray())

        db.bills().delete(100)

        val testObserver = db.bills().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testBillResponseData(billId = 100)
        val data2 = testBillResponseData(billId = 101)
        val data3 = testBillResponseData(billId = 102)
        val data4 = testBillResponseData(billId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.bills().insertAll(*list.map { it.toBill() }.toList().toTypedArray())

        db.bills().clear()

        val testObserver = db.bills().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }

    @Test
    fun testLoadAllWithRelation() {
        db.transactionCategories().insert(testTransactionCategoryResponseData(transactionCategoryId = 567).toTransactionCategory())
        db.merchants().insert(testMerchantResponseData(merchantId = 678).toMerchant())
        db.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        db.bills().insert(testBillResponseData(billId = 123, accountId = 345, merchantId = 678, transactionCategoryId = 567).toBill())
        db.billPayments().insert(testBillPaymentResponseData(billPaymentId = 456, billId = 123).toBillPayment())
        db.billPayments().insert(testBillPaymentResponseData(billPaymentId = 457, billId = 123).toBillPayment())

        val testObserver = db.bills().loadWithRelation().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)

        val model = testObserver.value()[0]

        assertEquals(123L, model.bill?.billId)
        assertEquals(345L, model.account?.account?.accountId)
        assertEquals(567L, model.transactionCategory?.transactionCategoryId)
        assertEquals(678L, model.merchant?.merchantId)
        assertEquals(2, model.payments?.size)
        assertEquals(456L, model.payments?.get(0)?.billPaymentId)
        assertEquals(457L, model.payments?.get(1)?.billPaymentId)
    }

    @Test
    fun testLoadByAccountIdWithRelation() {
        db.transactionCategories().insert(testTransactionCategoryResponseData(transactionCategoryId = 567).toTransactionCategory())
        db.merchants().insert(testMerchantResponseData(merchantId = 678).toMerchant())
        db.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        db.bills().insert(testBillResponseData(billId = 123, accountId = 345, merchantId = 678, transactionCategoryId = 567).toBill())
        db.billPayments().insert(testBillPaymentResponseData(billPaymentId = 456, billId = 123).toBillPayment())
        db.billPayments().insert(testBillPaymentResponseData(billPaymentId = 457, billId = 123).toBillPayment())

        val testObserver = db.bills().loadWithRelation(billId = 123).test()
        testObserver.awaitValue()

        val model = testObserver.value()

        assertEquals(123L, model?.bill?.billId)
        assertEquals(345L, model?.account?.account?.accountId)
        assertEquals(567L, model?.transactionCategory?.transactionCategoryId)
        assertEquals(678L, model?.merchant?.merchantId)
        assertEquals(2, model?.payments?.size)
        assertEquals(456L, model?.payments?.get(0)?.billPaymentId)
        assertEquals(457L, model?.payments?.get(1)?.billPaymentId)
    }
}