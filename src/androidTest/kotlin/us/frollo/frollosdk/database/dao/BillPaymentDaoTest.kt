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

class BillPaymentDaoTest {

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
        val data1 = testBillPaymentResponseData(billPaymentId = 100, date = "2019-01-01")
        val data2 = testBillPaymentResponseData(billPaymentId = 101, date = "2019-02-01")
        val data3 = testBillPaymentResponseData(billPaymentId = 102, date = "2019-02-06")
        val data4 = testBillPaymentResponseData(billPaymentId = 103, date = "2019-04-01")
        val data5 = testBillPaymentResponseData(billPaymentId = 104, date = "2019-04-30")
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.billPayments().insertAll(*list.map { it.toBillPayment() }.toList().toTypedArray())

        val testObserver = db.billPayments().load(fromDate = "2019-02-06", toDate = "2019-04-30").test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testLoadByBillPaymentId() {
        val data1 = testBillPaymentResponseData(billPaymentId = 100, date = "2019-01-01")
        val data2 = testBillPaymentResponseData(billPaymentId = 101, date = "2019-02-01")
        val data3 = testBillPaymentResponseData(billPaymentId = 102, date = "2019-02-06")
        val data4 = testBillPaymentResponseData(billPaymentId = 103, date = "2019-04-01")
        val data5 = testBillPaymentResponseData(billPaymentId = 104, date = "2019-04-30")
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.billPayments().insertAll(*list.map { it.toBillPayment() }.toList().toTypedArray())

        val testObserver = db.billPayments().load(billPaymentId = data3.billPaymentId).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data3.billPaymentId, testObserver.value()?.billPaymentId)
    }

    @Test
    fun testLoadByBillIdDated() {
        val data1 = testBillPaymentResponseData(billPaymentId = 100, billId = 200, date = "2019-01-01")
        val data2 = testBillPaymentResponseData(billPaymentId = 101, billId = 200, date = "2019-02-01")
        val data3 = testBillPaymentResponseData(billPaymentId = 102, billId = 201, date = "2019-02-06")
        val data4 = testBillPaymentResponseData(billPaymentId = 103, billId = 200, date = "2019-04-01")
        val data5 = testBillPaymentResponseData(billPaymentId = 104, billId = 201, date = "2019-04-30")
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.billPayments().insertAll(*list.map { it.toBillPayment() }.toList().toTypedArray())

        val testObserver = db.billPayments().loadByBillId(billId = 200, fromDate = "2019-02-01", toDate = "2019-04-30").test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(2, testObserver.value()?.size)
    }

    @Test
    fun testLoadByBillId() {
        val data1 = testBillPaymentResponseData(billPaymentId = 100, billId = 200, date = "2019-01-01")
        val data2 = testBillPaymentResponseData(billPaymentId = 101, billId = 200, date = "2019-02-01")
        val data3 = testBillPaymentResponseData(billPaymentId = 102, billId = 201, date = "2019-02-06")
        val data4 = testBillPaymentResponseData(billPaymentId = 103, billId = 200, date = "2019-04-01")
        val data5 = testBillPaymentResponseData(billPaymentId = 104, billId = 201, date = "2019-04-30")
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.billPayments().insertAll(*list.map { it.toBillPayment() }.toList().toTypedArray())

        val testObserver = db.billPayments().loadByBillId(billId = 200).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(3, testObserver.value()?.size)
    }

    @Test
    fun testInsertAll() {
        val data1 = testBillPaymentResponseData(billPaymentId = 100, date = "2019-01-01")
        val data2 = testBillPaymentResponseData(billPaymentId = 101, date = "2019-02-01")
        val data3 = testBillPaymentResponseData(billPaymentId = 102, date = "2019-02-06")
        val data4 = testBillPaymentResponseData(billPaymentId = 103, date = "2019-04-01")
        val data5 = testBillPaymentResponseData(billPaymentId = 104, date = "2019-04-30")
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.billPayments().insertAll(*list.map { it.toBillPayment() }.toList().toTypedArray())

        val testObserver = db.billPayments().load(fromDate = "2019-01-01", toDate = "2019-04-30").test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(5, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testBillPaymentResponseData(billPaymentId = 100)

        db.billPayments().insert(data.toBillPayment())

        val testObserver = db.billPayments().load(billPaymentId = data.billPaymentId).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.billPaymentId, testObserver.value()?.billPaymentId)
    }

    @Test
    fun testGetIdsByBillIds() {
        val data1 = testBillPaymentResponseData(billPaymentId = 100, billId = 200, date = "2019-01-01")
        val data2 = testBillPaymentResponseData(billPaymentId = 101, billId = 200, date = "2019-02-01")
        val data3 = testBillPaymentResponseData(billPaymentId = 102, billId = 201, date = "2019-02-06")
        val data4 = testBillPaymentResponseData(billPaymentId = 103, billId = 202, date = "2019-04-01")
        val data5 = testBillPaymentResponseData(billPaymentId = 104, billId = 201, date = "2019-04-30")
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.billPayments().insertAll(*list.map { it.toBillPayment() }.toList().toTypedArray())

        val ids = db.billPayments().getIdsByBillIds(billIds = longArrayOf(200,202)).sorted()
        assertEquals(3, ids.size)
        assertTrue(ids.containsAll(mutableListOf<Long>(100, 101, 103)))
    }

    @Test
    fun testGetStaleIds() {
        val data1 = testBillPaymentResponseData(billPaymentId = 100, date = "2019-01-01")
        val data2 = testBillPaymentResponseData(billPaymentId = 101, date = "2019-02-01")
        val data3 = testBillPaymentResponseData(billPaymentId = 102, date = "2019-02-06")
        val data4 = testBillPaymentResponseData(billPaymentId = 103, date = "2019-04-01")
        val data5 = testBillPaymentResponseData(billPaymentId = 104, date = "2019-04-30")
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.billPayments().insertAll(*list.map { it.toBillPayment() }.toList().toTypedArray())

        val staleIds = db.billPayments().getStaleIds(apiIds = longArrayOf(102, 103), fromDate = "2019-02-06", toDate = "2019-04-30").sorted()

        assertEquals(1, staleIds.size)
        assertTrue(staleIds.containsAll(mutableListOf<Long>(104)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testBillPaymentResponseData(billPaymentId = 100, date = "2019-01-01")
        val data2 = testBillPaymentResponseData(billPaymentId = 101, date = "2019-02-01")
        val data3 = testBillPaymentResponseData(billPaymentId = 102, date = "2019-02-06")
        val data4 = testBillPaymentResponseData(billPaymentId = 103, date = "2019-04-01")
        val data5 = testBillPaymentResponseData(billPaymentId = 104, date = "2019-04-30")
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.billPayments().insertAll(*list.map { it.toBillPayment() }.toList().toTypedArray())

        var testObserver = db.billPayments().load(fromDate = "2019-01-01", toDate = "2019-04-30").test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(5, testObserver.value().size)

        db.billPayments().deleteMany(longArrayOf(100, 103))

        testObserver = db.billPayments().load(fromDate = "2019-01-01", toDate = "2019-04-30").test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testDelete() {
        val data1 = testBillPaymentResponseData(billPaymentId = 100, date = "2019-01-01")
        val data2 = testBillPaymentResponseData(billPaymentId = 101, date = "2019-02-01")
        val data3 = testBillPaymentResponseData(billPaymentId = 102, date = "2019-02-06")
        val data4 = testBillPaymentResponseData(billPaymentId = 103, date = "2019-04-01")
        val data5 = testBillPaymentResponseData(billPaymentId = 104, date = "2019-04-30")
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.billPayments().insertAll(*list.map { it.toBillPayment() }.toList().toTypedArray())

        var testObserver = db.billPayments().load(fromDate = "2019-01-01", toDate = "2019-04-30").test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(5, testObserver.value().size)

        db.billPayments().delete(100)

        testObserver = db.billPayments().load(fromDate = "2019-01-01", toDate = "2019-04-30").test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testBillPaymentResponseData(billPaymentId = 100, date = "2019-01-01")
        val data2 = testBillPaymentResponseData(billPaymentId = 101, date = "2019-02-01")
        val data3 = testBillPaymentResponseData(billPaymentId = 102, date = "2019-02-06")
        val data4 = testBillPaymentResponseData(billPaymentId = 103, date = "2019-04-01")
        val data5 = testBillPaymentResponseData(billPaymentId = 104, date = "2019-04-30")
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.billPayments().insertAll(*list.map { it.toBillPayment() }.toList().toTypedArray())

        var testObserver = db.billPayments().load(fromDate = "2019-01-01", toDate = "2019-04-30").test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(5, testObserver.value().size)

        db.billPayments().clear()

        testObserver = db.billPayments().load(fromDate = "2019-01-01", toDate = "2019-04-30").test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }

    @Test
    fun testLoadAllWithRelation() {
        db.bills().insert(testBillResponseData(billId = 123, accountId = 345, merchantId = 678, transactionCategoryId = 567).toBill())
        db.billPayments().insert(testBillPaymentResponseData(billPaymentId = 456, billId = 123, date = "2019-01-01").toBillPayment())

        val testObserver = db.billPayments().loadWithRelation(fromDate = "2019-01-01", toDate = "2019-04-30").test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)

        val model = testObserver.value()[0]

        assertEquals(123L, model.bill?.bill?.billId)
        assertEquals(123L, model.billPayment?.billId)
        assertEquals(456L, model.billPayment?.billPaymentId)
    }

    @Test
    fun testLoadByBillPaymentIdWithRelation() {
        db.bills().insert(testBillResponseData(billId = 123, accountId = 345, merchantId = 678, transactionCategoryId = 567).toBill())
        db.billPayments().insert(testBillPaymentResponseData(billPaymentId = 456, billId = 123, date = "2019-01-01").toBillPayment())

        val testObserver = db.billPayments().loadWithRelation(billPaymentId = 456).test()

        testObserver.awaitValue()

        val model = testObserver.value()

        assertNotNull(model)
        assertEquals(123L, model?.bill?.bill?.billId)
        assertEquals(123L, model?.billPayment?.billId)
        assertEquals(456L, model?.billPayment?.billPaymentId)
    }

    @Test
    fun testLoadByBillIdDatedWithRelation() {
        db.bills().insert(testBillResponseData(billId = 123, accountId = 345, merchantId = 678, transactionCategoryId = 567).toBill())
        db.billPayments().insert(testBillPaymentResponseData(billPaymentId = 456, billId = 123, date = "2019-01-01").toBillPayment())

        val testObserver = db.billPayments().loadByBillIdWithRelation(billId = 123, fromDate = "2019-01-01", toDate = "2019-04-30").test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)

        val model = testObserver.value()[0]

        assertEquals(123L, model.bill?.bill?.billId)
        assertEquals(123L, model.billPayment?.billId)
        assertEquals(456L, model.billPayment?.billPaymentId)
    }

    @Test
    fun testLoadByBillIdWithRelation() {
        db.bills().insert(testBillResponseData(billId = 123, accountId = 345, merchantId = 678, transactionCategoryId = 567).toBill())
        db.billPayments().insert(testBillPaymentResponseData(billPaymentId = 456, billId = 123, date = "2019-01-01").toBillPayment())

        val testObserver = db.billPayments().loadByBillIdWithRelation(billId = 123).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)

        val model = testObserver.value()[0]

        assertEquals(123L, model.bill?.bill?.billId)
        assertEquals(123L, model.billPayment?.billId)
        assertEquals(456L, model.billPayment?.billPaymentId)
    }
}