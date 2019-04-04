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

import org.junit.Assert.*
import org.junit.Test
import us.frollo.frollosdk.model.*
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory

class ReportsMappingTest {

    @Test
    fun testResponseToReportTransactionCurrent() {
        val response = testTransactionCurrentReportResponseData()
        val model = response.days[0].toReportTransactionCurrent(ReportGrouping.BUDGET_CATEGORY, budgetCategory = BudgetCategory.SAVINGS, linkedId = 1, name = "Unknown")
        assertEquals(1, model.day)
        assertNotNull(model.amount)
        assertNotNull(model.average)
        assertNotNull(model.budget)
        assertNotNull(model.previous)
        assertEquals(1L, model.linkedId)
        assertEquals("Unknown", model.name)
        assertEquals(ReportGrouping.BUDGET_CATEGORY, model.grouping)
        assertEquals(BudgetCategory.SAVINGS, model.filteredBudgetCategory)
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