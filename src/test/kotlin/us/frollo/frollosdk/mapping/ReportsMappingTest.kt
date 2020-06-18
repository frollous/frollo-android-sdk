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

package us.frollo.frollosdk.mapping

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.coredata.reports.TransactionReportPeriod
import us.frollo.frollosdk.model.testAccountBalanceReportResponseData
import us.frollo.frollosdk.model.testGroupReportResponseData
import us.frollo.frollosdk.model.testReportResponseData
import us.frollo.frollosdk.model.testReportsResponseData

class ReportsMappingTest {

    @Test
    fun testResponseToReportAccountBalance() {
        val response = testAccountBalanceReportResponseData()
        val model = response.data[0].accounts[0].toReportAccountBalance(period = ReportPeriod.MONTH, date = "2019-03")
        assertEquals("2019-03", model.date)
        assertNotNull(model.value)
        assertEquals("AUD", model.currency)
        assertEquals(ReportPeriod.MONTH, model.period)
        assertEquals(1L, model.accountId)
    }

    @Test
    fun testReportsResponseToReports() {
        val response = testReportsResponseData()
        val models = response.toReports(ReportGrouping.BUDGET_CATEGORY, period = TransactionReportPeriod.MONTHLY)
        assertEquals(1, models.size)
        assertEquals("2019-03-01", models[0].date)
        assertEquals(1, models[0].groups.size)
        assertEquals("2019-03-01", models[0].groups[0].date)
    }

    @Test
    fun testReportResponseToReport() {
        val response = testReportResponseData()
        val model = response.toReport(ReportGrouping.BUDGET_CATEGORY, period = TransactionReportPeriod.MONTHLY)
        assertEquals("2019-03-01", model.date)
        assertNotNull(model.value)
        assertNotNull(model.isIncome)
        assertEquals(ReportGrouping.BUDGET_CATEGORY, model.grouping)
        assertEquals(TransactionReportPeriod.MONTHLY, model.period)
        assertEquals(1, model.groups.size)
        assertEquals("2019-03-01", model.groups[0].date)
    }

    @Test
    fun testGroupReportResponseToGroupReport() {
        val response = testGroupReportResponseData()
        val model = response.toGroupReport(ReportGrouping.BUDGET_CATEGORY, period = TransactionReportPeriod.MONTHLY, date = "2019-03-01")
        assertEquals("2019-03-01", model.date)
        assertNotNull(model.value)
        assertNotNull(model.isIncome)
        assertEquals(ReportGrouping.BUDGET_CATEGORY, model.grouping)
        assertEquals(TransactionReportPeriod.MONTHLY, model.period)
        assertEquals(1L, model.linkedId)
        assertEquals("living", model.name)
        assertEquals(2, model.transactionIds?.size)
    }
}
