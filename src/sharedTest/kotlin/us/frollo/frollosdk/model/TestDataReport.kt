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

package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.api.reports.TransactionCurrentReportResponse
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportTransactionCurrent
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.testutils.randomNumber
import java.math.BigDecimal
import kotlin.random.Random

internal fun testTransactionCurrentReportResponseData() : TransactionCurrentReportResponse {
    val days = listOf(
            TransactionCurrentReportResponse.Report(
                    day = 1,
                    spendValue = randomNumber().toBigDecimal(),
                    previousPeriodValue = randomNumber().toBigDecimal(),
                    averageValue = randomNumber().toBigDecimal(),
                    budgetValue = randomNumber().toBigDecimal()))

    val groups = listOf(
            TransactionCurrentReportResponse.GroupReport(
                    id = 1,
                    name = "living",
                    spendValue = randomNumber().toBigDecimal(),
                    previousPeriodValue = randomNumber().toBigDecimal(),
                    averageValue = randomNumber().toBigDecimal(),
                    budgetValue = randomNumber().toBigDecimal(),
                    days = days))

    return TransactionCurrentReportResponse(days = days, groups = groups)
}

internal fun testReportTransactionCurrentData(
        day: Int, linkedId: Long? = null, linkedName: String? = null,
        grouping: ReportGrouping? = null, budgetCategory: BudgetCategory? = null,
        id: Long? = null, amount: BigDecimal? = null): ReportTransactionCurrent {
    val report = ReportTransactionCurrent(
            day = day,
            linkedId = linkedId,
            name = linkedName,
            amount = amount ?: BigDecimal(34.67),
            previous = BigDecimal(31.33),
            average = BigDecimal(29.50),
            budget = BigDecimal(30.00),
            filteredBudgetCategory = budgetCategory,
            grouping = grouping ?: ReportGrouping.values()[Random.nextInt(ReportGrouping.values().size)])

    id?.let { report.reportId = it }

    return report
}