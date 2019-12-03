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

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Test
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.model.testAccountBalanceReportResponseData
import us.frollo.frollosdk.model.testTransactionHistoryReportResponseData

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
    fun testResponseToReportTransactionHistory() {
        val response = testTransactionHistoryReportResponseData()
        val model = response.data[0].toReportTransactionHistory(ReportGrouping.BUDGET_CATEGORY, period = ReportPeriod.MONTH, budgetCategory = BudgetCategory.SAVINGS)
        assertEquals("2019-03", model.date)
        assertNotNull(model.value)
        assertNotNull(model.budget)
        assertEquals(ReportGrouping.BUDGET_CATEGORY, model.grouping)
        assertEquals(ReportPeriod.MONTH, model.period)
        assertEquals(BudgetCategory.SAVINGS, model.filteredBudgetCategory)
    }

    @Test
    fun testResponseToReportGroupTransactionHistory() {
        val response = testTransactionHistoryReportResponseData()
        val model = response.data[0].groups[0].toReportGroupTransactionHistory(ReportGrouping.BUDGET_CATEGORY, period = ReportPeriod.MONTH, budgetCategory = BudgetCategory.SAVINGS, date = "2019-03", reportId = 1)
        assertEquals("2019-03", model.date)
        assertNotNull(model.value)
        assertNotNull(model.budget)
        assertEquals(ReportGrouping.BUDGET_CATEGORY, model.grouping)
        assertEquals(ReportPeriod.MONTH, model.period)
        assertEquals(BudgetCategory.SAVINGS, model.filteredBudgetCategory)
        assertEquals(1L, model.linkedId)
        assertEquals("living", model.name)
        assertEquals(2, model.transactionIds?.size)
    }
}