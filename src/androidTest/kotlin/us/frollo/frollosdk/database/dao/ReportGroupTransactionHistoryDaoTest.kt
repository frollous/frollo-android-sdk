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
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Before

import org.junit.Rule
import org.junit.Test
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.model.testReportGroupTransactionHistoryData
import java.math.BigDecimal

class ReportGroupTransactionHistoryDaoTest {

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
        val data1 = testReportGroupTransactionHistoryData(id = 100, reportId = 200, linkedId = 1, linkedName = "Unknown", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data2 = testReportGroupTransactionHistoryData(id = 101, reportId = 201, linkedId = 1, linkedName = "Unknown", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data3 = testReportGroupTransactionHistoryData(id = 102, reportId = 202, linkedId = 1, linkedName = "Unknown", date = "2018-06-01", period = ReportPeriod.DAY, grouping = ReportGrouping.MERCHANT)
        val data4 = testReportGroupTransactionHistoryData(id = 103, reportId = 203, linkedId = 1, linkedName = "Unknown", date = "2018-06-02", period = ReportPeriod.DAY, grouping = ReportGrouping.TRANSACTION_CATEGORY)
        val data5 = testReportGroupTransactionHistoryData(id = 104, reportId = 201, linkedId = 12, linkedName = "Woolworths", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data6 = testReportGroupTransactionHistoryData(id = 105, reportId = 201, linkedId = 6, linkedName = "Coles", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)

        val list = mutableListOf(data1, data2, data3, data4, data5, data6)

        db.reportGroupsTransactionHistory().insertAll(*list.toTypedArray())

        val models = db.reportGroupsTransactionHistory().load()
        assertTrue(models.isNotEmpty())
        assertEquals(6, models.size)
    }

    @Test
    fun testFindReports() {
        val data1 = testReportGroupTransactionHistoryData(id = 100, reportId = 200, linkedId = 1, linkedName = "Unknown", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data2 = testReportGroupTransactionHistoryData(id = 101, reportId = 201, linkedId = 1, linkedName = "Unknown", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data3 = testReportGroupTransactionHistoryData(id = 102, reportId = 202, linkedId = 1, linkedName = "Unknown", date = "2018-06-01", period = ReportPeriod.DAY, grouping = ReportGrouping.MERCHANT)
        val data4 = testReportGroupTransactionHistoryData(id = 103, reportId = 203, linkedId = 1, linkedName = "Unknown", date = "2018-06-02", period = ReportPeriod.DAY, grouping = ReportGrouping.TRANSACTION_CATEGORY)
        val data5 = testReportGroupTransactionHistoryData(id = 104, reportId = 201, linkedId = 12, linkedName = "Woolworths", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data6 = testReportGroupTransactionHistoryData(id = 105, reportId = 201, linkedId = 6, linkedName = "Coles", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)

        val list = mutableListOf(data1, data2, data3, data4, data5, data6)

        db.reportGroupsTransactionHistory().insertAll(*list.toTypedArray())

        val models = db.reportGroupsTransactionHistory().find(reportId = 201, linkedIds = longArrayOf(1, 12))
        assertTrue(models.isNotEmpty())
        assertEquals(2, models.size)
        assertTrue(models.map { it.reportGroupId }.toList().containsAll(mutableListOf<Long>(101, 104)))
    }

    @Test
    fun testFindStaleIds() {
        val data1 = testReportGroupTransactionHistoryData(id = 100, reportId = 200, linkedId = 1, linkedName = "Unknown", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data2 = testReportGroupTransactionHistoryData(id = 101, reportId = 201, linkedId = 1, linkedName = "Unknown", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data3 = testReportGroupTransactionHistoryData(id = 102, reportId = 202, linkedId = 1, linkedName = "Unknown", date = "2018-06-01", period = ReportPeriod.DAY, grouping = ReportGrouping.MERCHANT)
        val data4 = testReportGroupTransactionHistoryData(id = 103, reportId = 203, linkedId = 1, linkedName = "Unknown", date = "2018-06-02", period = ReportPeriod.DAY, grouping = ReportGrouping.TRANSACTION_CATEGORY)
        val data5 = testReportGroupTransactionHistoryData(id = 104, reportId = 201, linkedId = 12, linkedName = "Woolworths", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data6 = testReportGroupTransactionHistoryData(id = 105, reportId = 201, linkedId = 6, linkedName = "Coles", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)

        val list = mutableListOf(data1, data2, data3, data4, data5, data6)

        db.reportGroupsTransactionHistory().insertAll(*list.toTypedArray())

        val models = db.reportGroupsTransactionHistory().findStaleIds(reportId = 201, linkedIds = longArrayOf(1, 12))
        assertTrue(models.isNotEmpty())
        assertEquals(1, models.size)
        assertEquals(105L, models[0])
    }

    @Test
    fun testInsertAll() {
        val data1 = testReportGroupTransactionHistoryData(id = 100, reportId = 200, linkedId = 1, linkedName = "Unknown", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data2 = testReportGroupTransactionHistoryData(id = 101, reportId = 201, linkedId = 1, linkedName = "Unknown", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data3 = testReportGroupTransactionHistoryData(id = 102, reportId = 202, linkedId = 1, linkedName = "Unknown", date = "2018-06-01", period = ReportPeriod.DAY, grouping = ReportGrouping.MERCHANT)
        val data4 = testReportGroupTransactionHistoryData(id = 103, reportId = 203, linkedId = 1, linkedName = "Unknown", date = "2018-06-02", period = ReportPeriod.DAY, grouping = ReportGrouping.TRANSACTION_CATEGORY)
        val data5 = testReportGroupTransactionHistoryData(id = 104, reportId = 201, linkedId = 12, linkedName = "Woolworths", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data6 = testReportGroupTransactionHistoryData(id = 105, reportId = 201, linkedId = 6, linkedName = "Coles", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)

        val list = mutableListOf(data1, data2, data3, data4, data5, data6)

        db.reportGroupsTransactionHistory().insertAll(*list.toTypedArray())

        val models = db.reportGroupsTransactionHistory().load()
        assertTrue(models.isNotEmpty())
        assertEquals(6, models.size)
    }

    @Test
    fun testInsert() {
        val data1 = testReportGroupTransactionHistoryData(id = 101, reportId = 201, linkedId = 1, linkedName = "Unknown", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)

        db.reportGroupsTransactionHistory().insert(data1)

        val models = db.reportGroupsTransactionHistory().load()
        assertTrue(models.isNotEmpty())
        assertEquals(1, models.size)
    }

    @Test
    fun testUpdate() {
        var data1 = testReportGroupTransactionHistoryData(id = 101, reportId = 201, linkedId = 1, linkedName = "Unknown", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT, value = BigDecimal(123.0))

        db.reportGroupsTransactionHistory().insert(data1)

        var models = db.reportGroupsTransactionHistory().load()
        assertTrue(models.isNotEmpty())
        assertEquals(1, models.size)
        assertEquals(BigDecimal(123.0), models[0].value)

        data1 = testReportGroupTransactionHistoryData(id = 101, reportId = 201, linkedId = 1, linkedName = "Unknown", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT, value = BigDecimal(245.0))

        db.reportGroupsTransactionHistory().update(data1)

        models = db.reportGroupsTransactionHistory().load()
        assertTrue(models.isNotEmpty())
        assertEquals(1, models.size)
        assertEquals(BigDecimal(245.0), models[0].value)
    }

    @Test
    fun testDeleteMany() {
        val data1 = testReportGroupTransactionHistoryData(id = 100, reportId = 200, linkedId = 1, linkedName = "Unknown", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data2 = testReportGroupTransactionHistoryData(id = 101, reportId = 201, linkedId = 12, linkedName = "Woolworths", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data3 = testReportGroupTransactionHistoryData(id = 102, reportId = 202, linkedId = 6, linkedName = "Coles", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)

        val list = mutableListOf(data1, data2, data3)

        db.reportGroupsTransactionHistory().insertAll(*list.toTypedArray())

        var models = db.reportGroupsTransactionHistory().load()
        assertTrue(models.isNotEmpty())
        assertEquals(3, models.size)

        db.reportGroupsTransactionHistory().deleteMany(longArrayOf(100, 102))

        models = db.reportGroupsTransactionHistory().load()

        assertTrue(models.isNotEmpty())
        assertEquals(1, models.size)
        assertEquals(101L, models[0].reportGroupId)
    }

    @Test
    fun testDeleteByReportIds() {
        val data1 = testReportGroupTransactionHistoryData(id = 100, reportId = 200, linkedId = 1, linkedName = "Unknown", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data2 = testReportGroupTransactionHistoryData(id = 101, reportId = 201, linkedId = 12, linkedName = "Woolworths", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data3 = testReportGroupTransactionHistoryData(id = 102, reportId = 202, linkedId = 6, linkedName = "Coles", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)

        val list = mutableListOf(data1, data2, data3)

        db.reportGroupsTransactionHistory().insertAll(*list.toTypedArray())

        var models = db.reportGroupsTransactionHistory().load()
        assertTrue(models.isNotEmpty())
        assertEquals(3, models.size)

        db.reportGroupsTransactionHistory().deleteByReportIds(longArrayOf(200, 202))

        models = db.reportGroupsTransactionHistory().load()

        assertTrue(models.isNotEmpty())
        assertEquals(1, models.size)
        assertEquals(101L, models[0].reportGroupId)
    }

    @Test
    fun testClear() {
        val data1 = testReportGroupTransactionHistoryData(id = 100, reportId = 200, linkedId = 1, linkedName = "Unknown", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data2 = testReportGroupTransactionHistoryData(id = 101, reportId = 201, linkedId = 12, linkedName = "Woolworths", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data3 = testReportGroupTransactionHistoryData(id = 102, reportId = 202, linkedId = 6, linkedName = "Coles", date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)

        val list = mutableListOf(data1, data2, data3)

        db.reportGroupsTransactionHistory().insertAll(*list.toTypedArray())

        var models = db.reportGroupsTransactionHistory().load()
        assertTrue(models.isNotEmpty())
        assertEquals(3, models.size)

        db.reportGroupsTransactionHistory().clear()

        models = db.reportGroupsTransactionHistory().load()

        assertTrue(models.isEmpty())
    }
}