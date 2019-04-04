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
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import java.math.BigDecimal

class ReportTransactionHistoryDaoTest {

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

        val groupData1 = testReportGroupTransactionHistoryData(id = 201, reportId = 101, date = "2018-06", linkedId = 678, linkedName = "Woolworths", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val groupData2 = testReportGroupTransactionHistoryData(id = 203, reportId = 103, date = "2018-06-02", linkedId = 567, linkedName = "Groceries", period = ReportPeriod.DAY, grouping = ReportGrouping.TRANSACTION_CATEGORY)

        val groupList = mutableListOf(groupData1, groupData2)

        db.reportGroupsTransactionHistory().insertAll(*groupList.toTypedArray())

        val data1 = testReportTransactionHistoryData(id = 100, date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data2 = testReportTransactionHistoryData(id = 101, date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data3 = testReportTransactionHistoryData(id = 102, date = "2018-06-01", period = ReportPeriod.DAY, grouping = ReportGrouping.MERCHANT)
        val data4 = testReportTransactionHistoryData(id = 103, date = "2018-06-02", period = ReportPeriod.DAY, grouping = ReportGrouping.TRANSACTION_CATEGORY)

        val list = mutableListOf(data1, data2, data3, data4)

        db.reportsTransactionHistory().insertAll(*list.toTypedArray())

        var testObserver = db.reportsTransactionHistory().load(fromDate = "2018-05", toDate = "2018-06", grouping = ReportGrouping.MERCHANT, period = ReportPeriod.MONTH, budgetCategory = null).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)
        var model = testObserver.value()[0]
        assertEquals(1, model.groups?.size)
        assertEquals(201L, model.groups?.get(0)?.groupReport?.reportGroupId)
        assertEquals(678L, model.groups?.get(0)?.merchant?.merchantId)

        testObserver = db.reportsTransactionHistory().load(fromDate = "2018-05-01", toDate = "2018-06-02", grouping = ReportGrouping.TRANSACTION_CATEGORY, period = ReportPeriod.DAY, budgetCategory = null).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)
        model = testObserver.value()[0]
        assertEquals(1, model.groups?.size)
        assertEquals(203L, model.groups?.get(0)?.groupReport?.reportGroupId)
        assertEquals(567L, model.groups?.get(0)?.transactionCategory?.transactionCategoryId)
    }

    @Test
    fun testFindReports() {
        val data1 = testReportTransactionHistoryData(id = 100, date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data2 = testReportTransactionHistoryData(id = 101, date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data3 = testReportTransactionHistoryData(id = 102, date = "2018-06-01", period = ReportPeriod.DAY, grouping = ReportGrouping.MERCHANT)
        val data4 = testReportTransactionHistoryData(id = 103, date = "2018-06-02", period = ReportPeriod.DAY, grouping = ReportGrouping.TRANSACTION_CATEGORY)
        val data5 = testReportTransactionHistoryData(id = 104, date = "2018-07", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data6 = testReportTransactionHistoryData(id = 105, date = "2018-08", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)

        val list = mutableListOf(data1, data2, data3, data4, data5, data6)

        db.reportsTransactionHistory().insertAll(*list.toTypedArray())

        var models = db.reportsTransactionHistory().find(fromDate = "2018-05", toDate = "2018-09", grouping = ReportGrouping.MERCHANT, period = ReportPeriod.MONTH, budgetCategory = null, dates = arrayOf("2018-06", "2018-07"))
        assertTrue(models.isNotEmpty())
        assertEquals(2, models.size)
        assertTrue(models.map { it.reportId }.toList().containsAll(mutableListOf<Long>(101, 104)))

        models = db.reportsTransactionHistory().find(fromDate = "2018-05-01", toDate = "2018-06-02", grouping = ReportGrouping.TRANSACTION_CATEGORY, period = ReportPeriod.DAY, budgetCategory = null, dates = arrayOf("2018-06-02"))
        assertTrue(models.isNotEmpty())
        assertEquals(1, models.size)
        assertEquals(103L, models[0].reportId)
    }

    @Test
    fun testFindStaleIds() {
        val data1 = testReportTransactionHistoryData(id = 100, date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data2 = testReportTransactionHistoryData(id = 101, date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data3 = testReportTransactionHistoryData(id = 102, date = "2018-06-01", period = ReportPeriod.DAY, grouping = ReportGrouping.MERCHANT)
        val data4 = testReportTransactionHistoryData(id = 103, date = "2018-06-02", period = ReportPeriod.DAY, grouping = ReportGrouping.TRANSACTION_CATEGORY)
        val data5 = testReportTransactionHistoryData(id = 104, date = "2018-07", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data6 = testReportTransactionHistoryData(id = 105, date = "2018-08", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)

        val list = mutableListOf(data1, data2, data3, data4, data5, data6)

        db.reportsTransactionHistory().insertAll(*list.toTypedArray())

        var models = db.reportsTransactionHistory().findStaleIds(fromDate = "2018-05", toDate = "2018-09", grouping = ReportGrouping.MERCHANT, period = ReportPeriod.MONTH, budgetCategory = null, dates = arrayOf("2018-06", "2018-07"))
        assertTrue(models.isNotEmpty())
        assertEquals(1, models.size)
        assertEquals(105L, models[0])

        models = db.reportsTransactionHistory().findStaleIds(fromDate = "2018-05-01", toDate = "2018-06-02", grouping = ReportGrouping.TRANSACTION_CATEGORY, period = ReportPeriod.DAY, budgetCategory = null, dates = arrayOf("2018-06-02"))
        assertTrue(models.isEmpty())
    }

    @Test
    fun testInsertAll() {
        val data1 = testReportTransactionHistoryData(id = 100, date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data2 = testReportTransactionHistoryData(id = 101, date = "2018-07", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data3 = testReportTransactionHistoryData(id = 102, date = "2018-06-01", period = ReportPeriod.DAY, grouping = ReportGrouping.MERCHANT)
        val data4 = testReportTransactionHistoryData(id = 103, date = "2018-06-02", period = ReportPeriod.DAY, grouping = ReportGrouping.TRANSACTION_CATEGORY)

        val list = mutableListOf(data1, data2, data3, data4)

        db.reportsTransactionHistory().insertAll(*list.toTypedArray())

        val testObserver = db.reportsTransactionHistory().load(fromDate = "2018-05", toDate = "2018-07", grouping = ReportGrouping.MERCHANT, period = ReportPeriod.MONTH, budgetCategory = null).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data1 = testReportTransactionHistoryData(id = 100, date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)

        db.reportsTransactionHistory().insert(data1)

        val testObserver = db.reportsTransactionHistory().load(fromDate = "2018-05", toDate = "2018-06", grouping = ReportGrouping.MERCHANT, period = ReportPeriod.MONTH, budgetCategory = null).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)
    }

    @Test
    fun testUpdate() {
        var data1 = testReportTransactionHistoryData(id = 100, date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT, value = BigDecimal(123.0))

        db.reportsTransactionHistory().insert(data1)

        var testObserver = db.reportsTransactionHistory().load(fromDate = "2018-05", toDate = "2018-06", grouping = ReportGrouping.MERCHANT, period = ReportPeriod.MONTH, budgetCategory = null).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        var models = testObserver.value()
        assertEquals(1, models.size)
        assertEquals(BigDecimal(123.0), models[0].report?.value)

        data1 = testReportTransactionHistoryData(id = 100, date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT, value = BigDecimal(245.0))

        db.reportsTransactionHistory().update(data1)

        testObserver = db.reportsTransactionHistory().load(fromDate = "2018-05", toDate = "2018-06", grouping = ReportGrouping.MERCHANT, period = ReportPeriod.MONTH, budgetCategory = null).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        models = testObserver.value()
        assertEquals(1, models.size)
        assertEquals(BigDecimal(245.0), models[0].report?.value)
    }

    @Test
    fun testDeleteMany() {
        val data1 = testReportTransactionHistoryData(id = 100, date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data2 = testReportTransactionHistoryData(id = 101, date = "2018-07", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data3 = testReportTransactionHistoryData(id = 102, date = "2018-08", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data4 = testReportTransactionHistoryData(id = 103, date = "2018-06-02", period = ReportPeriod.DAY, grouping = ReportGrouping.TRANSACTION_CATEGORY)

        val list = mutableListOf(data1, data2, data3, data4)

        db.reportsTransactionHistory().insertAll(*list.toTypedArray())

        var testObserver = db.reportsTransactionHistory().load(fromDate = "2018-05", toDate = "2018-08", grouping = ReportGrouping.MERCHANT, period = ReportPeriod.MONTH, budgetCategory = null).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)

        db.reportsTransactionHistory().deleteMany(longArrayOf(101,102))

        testObserver = db.reportsTransactionHistory().load(fromDate = "2018-05", toDate = "2018-08", grouping = ReportGrouping.MERCHANT, period = ReportPeriod.MONTH, budgetCategory = null).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testReportTransactionHistoryData(id = 100, date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data2 = testReportTransactionHistoryData(id = 101, date = "2018-07", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data3 = testReportTransactionHistoryData(id = 102, date = "2018-08", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data4 = testReportTransactionHistoryData(id = 103, date = "2018-06-02", period = ReportPeriod.DAY, grouping = ReportGrouping.TRANSACTION_CATEGORY)

        val list = mutableListOf(data1, data2, data3, data4)

        db.reportsTransactionHistory().insertAll(*list.toTypedArray())

        var testObserver = db.reportsTransactionHistory().load(fromDate = "2018-05", toDate = "2018-08", grouping = ReportGrouping.MERCHANT, period = ReportPeriod.MONTH, budgetCategory = null).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)

        db.reportsTransactionHistory().clear()

        testObserver = db.reportsTransactionHistory().load(fromDate = "2018-05", toDate = "2018-08", grouping = ReportGrouping.MERCHANT, period = ReportPeriod.MONTH, budgetCategory = null).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }
}