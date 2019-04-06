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
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.extensions.sqlForExistingAccountBalanceReports
import us.frollo.frollosdk.extensions.sqlForFetchingAccountBalanceReports
import us.frollo.frollosdk.extensions.sqlForStaleIdsAccountBalanceReports
import us.frollo.frollosdk.mapping.toAccount
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountType
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.testAccountResponseData
import us.frollo.frollosdk.model.testReportAccountBalanceData
import java.math.BigDecimal

class ReportAccountBalanceDaoTest {

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
        db.accounts().insert(testAccountResponseData(accountId = 234, providerAccountId = 345, accountType = AccountType.BANK).toAccount())
        db.accounts().insert(testAccountResponseData(accountId = 235, providerAccountId = 345, accountType = AccountType.CREDIT_CARD).toAccount())

        val data1 = testReportAccountBalanceData(date = "2018-01", period = ReportPeriod.MONTH, accountId = 234)
        val data2 = testReportAccountBalanceData(date = "2018-02", period = ReportPeriod.MONTH, accountId = 235)
        val data3 = testReportAccountBalanceData(date = "2018-03", period = ReportPeriod.MONTH, accountId = 234)
        val data4 = testReportAccountBalanceData(date = "2018-01-01", period = ReportPeriod.DAY, accountId = 234)
        val data5 = testReportAccountBalanceData(date = "2018-01-02", period = ReportPeriod.DAY, accountId = 234)
        val data6 = testReportAccountBalanceData(date = "2018-01-1", period = ReportPeriod.WEEK, accountId = 234)
        val data7 = testReportAccountBalanceData(date = "2018-01-2", period = ReportPeriod.WEEK, accountId = 234)

        val list = mutableListOf(data1, data2, data3, data4, data5, data6, data7)

        db.reportsAccountBalance().insert(*list.toTypedArray())

        var sql = sqlForFetchingAccountBalanceReports(fromDate = "2018-01", toDate = "2018-03", period = ReportPeriod.MONTH)
        var testObserver = db.reportsAccountBalance().loadWithRelation(sql).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
        var model = testObserver.value()[0]
        assertEquals(234L, model.account?.account?.accountId)
        model = testObserver.value()[1]
        assertEquals(235L, model.account?.account?.accountId)

        sql = sqlForFetchingAccountBalanceReports(fromDate = "2018-01", toDate = "2018-03", period = ReportPeriod.MONTH, accountType = AccountType.CREDIT_CARD)
        testObserver = db.reportsAccountBalance().loadWithRelation(sql).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)
        model = testObserver.value()[0]
        assertEquals(235L, model.account?.account?.accountId)

        sql = sqlForFetchingAccountBalanceReports(fromDate = "2018-01", toDate = "2018-03", period = ReportPeriod.MONTH, accountType = AccountType.BANK)
        testObserver = db.reportsAccountBalance().loadWithRelation(sql).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
        model = testObserver.value()[0]
        assertEquals(234L, model.account?.account?.accountId)

        sql = sqlForFetchingAccountBalanceReports(fromDate = "2018-01", toDate = "2018-03", period = ReportPeriod.MONTH, accountId = 234)
        testObserver = db.reportsAccountBalance().loadWithRelation(sql).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
        model = testObserver.value()[0]
        assertEquals(234L, model.account?.account?.accountId)

        sql = sqlForFetchingAccountBalanceReports(fromDate = "2018-01", toDate = "2018-12", period = ReportPeriod.MONTH, accountId = 236)
        testObserver = db.reportsAccountBalance().loadWithRelation(sql).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }

    @Test
    fun testFindReports() {
        db.accounts().insert(testAccountResponseData(accountId = 234, providerAccountId = 345, accountType = AccountType.BANK).toAccount())
        db.accounts().insert(testAccountResponseData(accountId = 235, providerAccountId = 345, accountType = AccountType.CREDIT_CARD).toAccount())
        db.accounts().insert(testAccountResponseData(accountId = 236, providerAccountId = 345, accountType = AccountType.BANK).toAccount())

        val data1 = testReportAccountBalanceData(date = "2018-01", period = ReportPeriod.MONTH, accountId = 234)
        val data2 = testReportAccountBalanceData(date = "2018-01", period = ReportPeriod.MONTH, accountId = 235)
        val data3 = testReportAccountBalanceData(date = "2018-01", period = ReportPeriod.MONTH, accountId = 236)
        val data4 = testReportAccountBalanceData(date = "2018-02", period = ReportPeriod.MONTH, accountId = 234)
        val data5 = testReportAccountBalanceData(date = "2018-02", period = ReportPeriod.MONTH, accountId = 235)
        val data6 = testReportAccountBalanceData(date = "2018-02", period = ReportPeriod.MONTH, accountId = 236)

        val list = mutableListOf(data1, data2, data3, data4, data5, data6)

        db.reportsAccountBalance().insert(*list.toTypedArray())

        var sql = sqlForExistingAccountBalanceReports(date = "2018-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(234,235))
        var models = db.reportsAccountBalance().find(sql)
        assertEquals(2, models.size)

        sql = sqlForExistingAccountBalanceReports(date = "2018-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(234,235,236), accountType = AccountType.CREDIT_CARD)
        models = db.reportsAccountBalance().find(sql)
        assertEquals(1, models.size)

        sql = sqlForExistingAccountBalanceReports(date = "2018-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(234,235,236), accountType = AccountType.BANK)
        models = db.reportsAccountBalance().find(sql)
        assertEquals(2, models.size)

        sql = sqlForExistingAccountBalanceReports(date = "2018-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(234,235), accountId = 234)
        models = db.reportsAccountBalance().find(sql)
        assertEquals(1, models.size)

        sql = sqlForExistingAccountBalanceReports(date = "2018-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(), accountId = 238)
        models = db.reportsAccountBalance().find(sql)
        assertEquals(0, models.size)

        sql = sqlForExistingAccountBalanceReports(date = "2018-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(234,235,236), accountType = AccountType.BILL)
        models = db.reportsAccountBalance().find(sql)
        assertEquals(0, models.size)
    }

    @Test
    fun testFindStaleIds() {
        db.accounts().insert(testAccountResponseData(accountId = 234, providerAccountId = 345, accountType = AccountType.BANK).toAccount())
        db.accounts().insert(testAccountResponseData(accountId = 235, providerAccountId = 345, accountType = AccountType.CREDIT_CARD).toAccount())
        db.accounts().insert(testAccountResponseData(accountId = 236, providerAccountId = 345, accountType = AccountType.BANK).toAccount())
        db.accounts().insert(testAccountResponseData(accountId = 237, providerAccountId = 345, accountType = AccountType.CREDIT_CARD).toAccount())

        val data1 = testReportAccountBalanceData(date = "2018-01", period = ReportPeriod.MONTH, accountId = 234, id = 100)
        val data2 = testReportAccountBalanceData(date = "2018-01", period = ReportPeriod.MONTH, accountId = 235, id = 101)
        val data3 = testReportAccountBalanceData(date = "2018-01", period = ReportPeriod.MONTH, accountId = 236, id = 102)
        val data4 = testReportAccountBalanceData(date = "2018-02", period = ReportPeriod.MONTH, accountId = 234, id = 103)
        val data5 = testReportAccountBalanceData(date = "2018-02", period = ReportPeriod.MONTH, accountId = 235, id = 104)
        val data6 = testReportAccountBalanceData(date = "2018-02", period = ReportPeriod.MONTH, accountId = 236, id = 105)
        val data7 = testReportAccountBalanceData(date = "2018-01", period = ReportPeriod.MONTH, accountId = 237, id = 106)

        val list = mutableListOf(data1, data2, data3, data4, data5, data6, data7)

        db.reportsAccountBalance().insert(*list.toTypedArray())

        var sql = sqlForStaleIdsAccountBalanceReports(date = "2018-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(234,235))
        var models = db.reportsAccountBalance().find(sql)
        assertEquals(2, models.size)
        assertEquals(236L, models[0].accountId)
        assertEquals(237L, models[1].accountId)

        sql = sqlForStaleIdsAccountBalanceReports(date = "2018-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(235), accountType = AccountType.CREDIT_CARD)
        models = db.reportsAccountBalance().find(sql)
        assertEquals(1, models.size)
        assertEquals(237L, models[0].accountId)

        sql = sqlForStaleIdsAccountBalanceReports(date = "2018-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(234,236), accountType = AccountType.BANK)
        models = db.reportsAccountBalance().find(sql)
        assertEquals(0, models.size)

        sql = sqlForStaleIdsAccountBalanceReports(date = "2018-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(), accountType = AccountType.BANK)
        models = db.reportsAccountBalance().find(sql)
        assertEquals(2, models.size)

        sql = sqlForStaleIdsAccountBalanceReports(date = "2018-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(234), accountId = 234)
        models = db.reportsAccountBalance().find(sql)
        assertEquals(0, models.size)

        sql = sqlForStaleIdsAccountBalanceReports(date = "2018-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(), accountId = 234)
        models = db.reportsAccountBalance().find(sql)
        assertEquals(1, models.size)

        sql = sqlForStaleIdsAccountBalanceReports(date = "2018-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(), accountId = 238)
        models = db.reportsAccountBalance().find(sql)
        assertEquals(0, models.size)

        sql = sqlForStaleIdsAccountBalanceReports(date = "2018-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(234,235,236), accountType = AccountType.BILL)
        models = db.reportsAccountBalance().find(sql)
        assertEquals(0, models.size)
    }

    @Test
    fun testInsertAll() {
        val data1 = testReportAccountBalanceData(date = "2018-01", period = ReportPeriod.MONTH, accountId = 234)
        val data2 = testReportAccountBalanceData(date = "2018-02", period = ReportPeriod.MONTH, accountId = 235)
        val data3 = testReportAccountBalanceData(date = "2018-03", period = ReportPeriod.MONTH, accountId = 234)

        val list = mutableListOf(data1, data2, data3)

        db.reportsAccountBalance().insert(*list.toTypedArray())

        val sql = sqlForFetchingAccountBalanceReports(fromDate = "2018-01", toDate = "2018-03", period = ReportPeriod.MONTH)
        val testObserver = db.reportsAccountBalance().loadWithRelation(sql).test()
        testObserver.awaitValue()
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testUpdateAll() {
        var data1 = testReportAccountBalanceData(date = "2018-01", period = ReportPeriod.MONTH, accountId = 234, id = 100, value = BigDecimal(123.0))

        db.reportsAccountBalance().insert(data1)

        val sql = sqlForFetchingAccountBalanceReports(fromDate = "2018-01", toDate = "2018-03", period = ReportPeriod.MONTH)
        var testObserver = db.reportsAccountBalance().loadWithRelation(sql).test()
        testObserver.awaitValue()
        assertEquals(BigDecimal(123.0), testObserver.value()[0].report?.value)

        data1 = testReportAccountBalanceData(date = "2018-01", period = ReportPeriod.MONTH, accountId = 234, id = 100, value = BigDecimal(567.0))

        db.reportsAccountBalance().update(data1)

        testObserver = db.reportsAccountBalance().loadWithRelation(sql).test()
        testObserver.awaitValue()
        assertEquals(BigDecimal(567.0), testObserver.value()[0].report?.value)
    }

    @Test
    fun testDeleteMany() {
        val data1 = testReportAccountBalanceData(date = "2018-01", period = ReportPeriod.MONTH, accountId = 234, id = 100)
        val data2 = testReportAccountBalanceData(date = "2018-01", period = ReportPeriod.MONTH, accountId = 235, id = 101)
        val data3 = testReportAccountBalanceData(date = "2018-01", period = ReportPeriod.MONTH, accountId = 236, id = 102)

        val list = mutableListOf(data1, data2, data3)

        db.reportsAccountBalance().insert(*list.toTypedArray())
        val sql = sqlForFetchingAccountBalanceReports(fromDate = "2018-01", toDate = "2018-03", period = ReportPeriod.MONTH)
        var testObserver = db.reportsAccountBalance().loadWithRelation(sql).test()
        testObserver.awaitValue()
        assertEquals(3, testObserver.value().size)

        db.reportsAccountBalance().deleteMany(longArrayOf(100,101))

        testObserver = db.reportsAccountBalance().loadWithRelation(sql).test()
        testObserver.awaitValue()
        assertEquals(1, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testReportAccountBalanceData(date = "2018-01", period = ReportPeriod.MONTH, accountId = 234, id = 100)
        val data2 = testReportAccountBalanceData(date = "2018-01", period = ReportPeriod.MONTH, accountId = 235, id = 101)
        val data3 = testReportAccountBalanceData(date = "2018-01", period = ReportPeriod.MONTH, accountId = 236, id = 102)

        val list = mutableListOf(data1, data2, data3)

        db.reportsAccountBalance().insert(*list.toTypedArray())
        val sql = sqlForFetchingAccountBalanceReports(fromDate = "2018-01", toDate = "2018-03", period = ReportPeriod.MONTH)
        var testObserver = db.reportsAccountBalance().loadWithRelation(sql).test()
        testObserver.awaitValue()
        assertEquals(3, testObserver.value().size)

        db.reportsAccountBalance().clear()

        testObserver = db.reportsAccountBalance().loadWithRelation(sql).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }
}