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

package us.frollo.frollosdk.model.display.reports

import org.junit.Assert.assertEquals
import org.junit.Test
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportTransactionCurrentRelation
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.model.testReportTransactionCurrentData

class ReportTransactionCurrentDisplayTest {

    @Test
    fun testListOfReportTransactionCurrentRelationToDisplay() {
        val reports = listOf(
                ReportTransactionCurrentRelation(report = testReportTransactionCurrentData(day = 1, grouping = ReportGrouping.BUDGET_CATEGORY, budgetCategory = BudgetCategory.LIVING)),
                ReportTransactionCurrentRelation(report = testReportTransactionCurrentData(
                        day = 2, linkedId = 1, linkedName = "living", grouping = ReportGrouping.BUDGET_CATEGORY, budgetCategory = BudgetCategory.LIVING))
        )

        val display = reports.toDisplay()

        assertEquals(1, display.overallReports.size)
        assertEquals(1, display.overallReports[0].day)
        assertEquals(1, display.groupReports.size)
        assertEquals(2, display.groupReports[0].report?.day)
    }
}