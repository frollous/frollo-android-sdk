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
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Before

import org.junit.Rule
import org.junit.Test
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.mapping.toMerchant
import us.frollo.frollosdk.mapping.toTransactionCategory
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.model.testMerchantResponseData
import us.frollo.frollosdk.model.testReportTransactionCurrentData
import us.frollo.frollosdk.model.testTransactionCategoryResponseData
import java.math.BigDecimal

class ReportTransactionCurrentDaoTest {

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
    fun testLoadWithRelation() {
        db.transactionCategories().insert(testTransactionCategoryResponseData(transactionCategoryId = 567).toTransactionCategory())
        db.merchants().insert(testMerchantResponseData(merchantId = 678).toMerchant())

        val data1 = testReportTransactionCurrentData(day = 1, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data2 = testReportTransactionCurrentData(day = 2, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data3 = testReportTransactionCurrentData(day = 1, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data4 = testReportTransactionCurrentData(day = 2, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)

        val data5 = testReportTransactionCurrentData(day = 1, grouping = ReportGrouping.TRANSACTION_CATEGORY, budgetCategory = BudgetCategory.LIVING)
        val data6 = testReportTransactionCurrentData(day = 2, grouping = ReportGrouping.TRANSACTION_CATEGORY, budgetCategory = BudgetCategory.LIVING)
        val data7 = testReportTransactionCurrentData(day = 1, linkedId = 567, linkedName = "Groceries", grouping = ReportGrouping.TRANSACTION_CATEGORY, budgetCategory = BudgetCategory.LIVING)
        val data8 = testReportTransactionCurrentData(day = 2, linkedId = 567, linkedName = "Groceries", grouping = ReportGrouping.TRANSACTION_CATEGORY, budgetCategory = BudgetCategory.LIVING)

        val data9 = testReportTransactionCurrentData(day = 1, grouping = ReportGrouping.BUDGET_CATEGORY)
        val data10 = testReportTransactionCurrentData(day = 2, grouping = ReportGrouping.BUDGET_CATEGORY)
        val data11 = testReportTransactionCurrentData(day = 1, linkedId = 0, linkedName = "income", grouping = ReportGrouping.BUDGET_CATEGORY)
        val data12 = testReportTransactionCurrentData(day = 2, linkedId = 0, linkedName = "income", grouping = ReportGrouping.BUDGET_CATEGORY)

        val list = mutableListOf(data1, data2, data3, data4, data5, data6, data7, data8, data9, data10, data11, data12)

        db.reportsTransactionCurrent().insertAll(*list.toTypedArray())

        var testObserver = db.reportsTransactionCurrent().load(grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
        var model = testObserver.value()[0]
        assertNull(model.merchant)
        model = testObserver.value()[3]
        assertEquals(678L, model.merchant?.merchantId)

        testObserver = db.reportsTransactionCurrent().load(grouping = ReportGrouping.MERCHANT, budgetCategory = null).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())

        testObserver = db.reportsTransactionCurrent().load(grouping = ReportGrouping.TRANSACTION_CATEGORY, budgetCategory = BudgetCategory.LIVING).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
        model = testObserver.value()[0]
        assertNull(model.transactionCategory)
        model = testObserver.value()[3]
        assertEquals(567L, model.transactionCategory?.transactionCategoryId)

        testObserver = db.reportsTransactionCurrent().load(grouping = ReportGrouping.TRANSACTION_CATEGORY, budgetCategory = null).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())

        testObserver = db.reportsTransactionCurrent().load(grouping = ReportGrouping.BUDGET_CATEGORY, budgetCategory = null).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
        model = testObserver.value()[0]
        assertNull(model.budgetCategory)
        model = testObserver.value()[3]
        assertEquals(BudgetCategory.INCOME, model.budgetCategory)

        testObserver = db.reportsTransactionCurrent().load(grouping = ReportGrouping.BUDGET_CATEGORY, budgetCategory = BudgetCategory.LIVING).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }

    @Test
    fun testFindReports() {
        val data1 = testReportTransactionCurrentData(day = 1, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data2 = testReportTransactionCurrentData(day = 2, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data3 = testReportTransactionCurrentData(day = 1, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data4 = testReportTransactionCurrentData(day = 2, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data5 = testReportTransactionCurrentData(day = 3, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data6 = testReportTransactionCurrentData(day = 4, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)

        val data7 = testReportTransactionCurrentData(day = 1, grouping = ReportGrouping.MERCHANT)
        val data8 = testReportTransactionCurrentData(day = 2, grouping = ReportGrouping.MERCHANT)
        val data9 = testReportTransactionCurrentData(day = 1, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT)
        val data10 = testReportTransactionCurrentData(day = 2, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT)
        val data11 = testReportTransactionCurrentData(day = 3, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT)
        val data12 = testReportTransactionCurrentData(day = 4, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT)

        val list = mutableListOf(data1, data2, data3, data4, data5, data6, data7, data8, data9, data10, data11, data12)

        db.reportsTransactionCurrent().insertAll(*list.toTypedArray())

        var models = db.reportsTransactionCurrent().find(grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, linkedId = 678, days = intArrayOf(1, 2, 3))
        assertTrue(models.isNotEmpty())
        assertEquals(3, models.size)

        models = db.reportsTransactionCurrent().find(grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, linkedId = null, days = intArrayOf(1))
        assertTrue(models.isNotEmpty())
        assertEquals(1, models.size)

        models = db.reportsTransactionCurrent().find(grouping = ReportGrouping.MERCHANT, budgetCategory = null, linkedId = 678, days = intArrayOf(1, 2))
        assertTrue(models.isNotEmpty())
        assertEquals(2, models.size)

        models = db.reportsTransactionCurrent().find(grouping = ReportGrouping.MERCHANT, budgetCategory = null, linkedId = null, days = intArrayOf(1, 2))
        assertTrue(models.isNotEmpty())
        assertEquals(2, models.size)
    }

    @Test
    fun testFindStaleIds() {
        val data1 = testReportTransactionCurrentData(day = 1, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, id = 100)
        val data2 = testReportTransactionCurrentData(day = 2, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, id = 101)
        val data3 = testReportTransactionCurrentData(day = 1, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, id = 102)
        val data4 = testReportTransactionCurrentData(day = 2, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, id = 103)
        val data5 = testReportTransactionCurrentData(day = 3, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, id = 104)
        val data6 = testReportTransactionCurrentData(day = 4, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, id = 105)

        val data7 = testReportTransactionCurrentData(day = 1, grouping = ReportGrouping.MERCHANT, id = 106)
        val data8 = testReportTransactionCurrentData(day = 2, grouping = ReportGrouping.MERCHANT, id = 107)
        val data9 = testReportTransactionCurrentData(day = 1, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, id = 108)
        val data10 = testReportTransactionCurrentData(day = 2, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, id = 109)
        val data11 = testReportTransactionCurrentData(day = 3, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, id = 110)
        val data12 = testReportTransactionCurrentData(day = 4, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, id = 111)

        val list = mutableListOf(data1, data2, data3, data4, data5, data6, data7, data8, data9, data10, data11, data12)

        db.reportsTransactionCurrent().insertAll(*list.toTypedArray())

        var ids = db.reportsTransactionCurrent().findStaleIds(grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, linkedId = 678, days = intArrayOf(1, 2, 3))
        assertTrue(ids.isNotEmpty())
        assertEquals(1, ids.size)
        assertEquals(105, ids[0])

        ids = db.reportsTransactionCurrent().findStaleIds(grouping = ReportGrouping.MERCHANT, budgetCategory = null, linkedId = 678, days = intArrayOf(1, 2, 4))
        assertTrue(ids.isNotEmpty())
        assertEquals(1, ids.size)
        assertEquals(110, ids[0])
    }

    @Test
    fun testInsertAll() {
        val data1 = testReportTransactionCurrentData(day = 1, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data2 = testReportTransactionCurrentData(day = 2, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data3 = testReportTransactionCurrentData(day = 1, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data4 = testReportTransactionCurrentData(day = 2, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)

        val list = mutableListOf(data1, data2, data3, data4)

        db.reportsTransactionCurrent().insertAll(*list.toTypedArray())

        val testObserver = db.reportsTransactionCurrent().load(grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testUpdateAll() {
        val data1 = testReportTransactionCurrentData(day = 1, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, id = 100)
        val data2 = testReportTransactionCurrentData(
                day = 1, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, id = 101,
                amount = BigDecimal(123.0))

        val list = mutableListOf(data1, data2)

        db.reportsTransactionCurrent().insertAll(*list.toTypedArray())

        var testObserver = db.reportsTransactionCurrent().load(grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
        var model = testObserver.value()[1]
        assertEquals(BigDecimal(123.0), model.report?.amount)

        val newData = testReportTransactionCurrentData(day = 1, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, id = 101,
                amount = BigDecimal(234.0))

        db.reportsTransactionCurrent().updateAll(newData)

        testObserver = db.reportsTransactionCurrent().load(grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
        model = testObserver.value()[1]
        assertEquals(BigDecimal(234.0), model.report?.amount)
    }

    @Test
    fun testDeleteMany() {
        val data1 = testReportTransactionCurrentData(day = 1, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, id = 100)
        val data2 = testReportTransactionCurrentData(day = 2, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, id = 101)
        val data3 = testReportTransactionCurrentData(day = 1, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, id = 102)
        val data4 = testReportTransactionCurrentData(day = 2, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, id = 103)

        val list = mutableListOf(data1, data2, data3, data4)

        db.reportsTransactionCurrent().insertAll(*list.toTypedArray())

        db.reportsTransactionCurrent().deleteMany(longArrayOf(101, 103))

        val testObserver = db.reportsTransactionCurrent().load(grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testInsertAndDeleteInTransaction() {
        val data1 = testReportTransactionCurrentData(day = 1, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, id = 100)
        val data2 = testReportTransactionCurrentData(day = 1, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, id = 101)
        val data3 = testReportTransactionCurrentData(day = 2, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, id = 102,
                amount = BigDecimal(123.0))

        val list = mutableListOf(data1, data2, data3)

        db.reportsTransactionCurrent().insertAll(*list.toTypedArray())

        var testObserver = db.reportsTransactionCurrent().load(grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
        var model = testObserver.value()[2]
        assertEquals(BigDecimal(123.0), model.report?.amount)

        val data4 = testReportTransactionCurrentData(day = 2, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, id = 103)
        val newData = testReportTransactionCurrentData(day = 1, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING, id = 102,
                amount = BigDecimal(234.0))

        db.reportsTransactionCurrent().insertAndDeleteInTransaction(new = listOf(data4), existing = listOf(newData), staleIds = listOf(101L))

        testObserver = db.reportsTransactionCurrent().load(grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
        model = testObserver.value()[0]
        assertEquals(100L, model.report?.reportId)
        model = testObserver.value()[1]
        assertEquals(102L, model.report?.reportId)
        assertEquals(BigDecimal(234.0), model.report?.amount)
        model = testObserver.value()[2]
        assertEquals(103L, model.report?.reportId)
    }

    @Test
    fun testClear() {
        val data1 = testReportTransactionCurrentData(day = 1, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data2 = testReportTransactionCurrentData(day = 2, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data3 = testReportTransactionCurrentData(day = 1, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data4 = testReportTransactionCurrentData(day = 2, linkedId = 678, linkedName = "Woolworths", grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)

        val list = mutableListOf(data1, data2, data3, data4)

        db.reportsTransactionCurrent().insertAll(*list.toTypedArray())

        var testObserver = db.reportsTransactionCurrent().load(grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)

        db.reportsTransactionCurrent().clear()

        testObserver = db.reportsTransactionCurrent().load(grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }
}