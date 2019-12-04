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

import us.frollo.frollosdk.model.api.reports.AccountBalanceReportResponse
import us.frollo.frollosdk.model.api.reports.TransactionHistoryReportResponse
import us.frollo.frollosdk.model.coredata.reports.ReportAccountBalance
import us.frollo.frollosdk.model.coredata.reports.ReportGroupTransactionHistory
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.coredata.reports.ReportTransactionHistory
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.testutils.randomNumber
import java.math.BigDecimal
import kotlin.random.Random

internal fun testAccountBalanceReportResponseData(): AccountBalanceReportResponse {
    val accounts = listOf(
            AccountBalanceReportResponse.Report.BalanceReport(
                    id = 1,
                    currency = "AUD",
                    value = randomNumber().toBigDecimal()))

    val data = listOf(AccountBalanceReportResponse.Report(
            date = "2019-03",
            value = randomNumber().toBigDecimal(),
            accounts = accounts))

    return AccountBalanceReportResponse(data = data)
}

internal fun testTransactionHistoryReportResponseData(): TransactionHistoryReportResponse {
    val groups = listOf(
            TransactionHistoryReportResponse.Report.GroupReport(
                    id = 1,
                    name = "living",
                    value = randomNumber().toBigDecimal(),
                    budget = randomNumber().toBigDecimal(),
                    transactionIds = listOf(1093435, 2959945)))

    val data = listOf(TransactionHistoryReportResponse.Report(
            date = "2019-03",
            value = randomNumber().toBigDecimal(),
            budget = randomNumber().toBigDecimal(),
            groups = groups))

    return TransactionHistoryReportResponse(data = data)
}

internal fun testReportAccountBalanceData(
    date: String,
    accountId: Long? = null,
    period: ReportPeriod,
    id: Long? = null,
    value: BigDecimal? = null
): ReportAccountBalance {
    val report = ReportAccountBalance(
            date = date,
            accountId = accountId ?: randomNumber().toLong(),
            currency = "AUD",
            value = value ?: BigDecimal(34.67),
            period = period)

    id?.let { report.reportId = it }

    return report
}

internal fun testReportTransactionHistoryData(
    date: String,
    period: ReportPeriod,
    grouping: ReportGrouping? = null,
    budgetCategory: BudgetCategory? = null,
    id: Long? = null,
    value: BigDecimal? = null,
    transactionTags: List<String>? = null
): ReportTransactionHistory {
    val report = ReportTransactionHistory(
            date = date,
            value = value ?: BigDecimal(34.67),
            budget = BigDecimal(30.00),
            period = period,
            filteredBudgetCategory = budgetCategory,
            transactionTags = transactionTags,
            grouping = grouping ?: ReportGrouping.values()[Random.nextInt(ReportGrouping.values().size)])

    id?.let { report.reportId = it }

    return report
}

internal fun testReportGroupTransactionHistoryData(
    date: String,
    linkedId: Long,
    linkedName: String,
    period: ReportPeriod,
    grouping: ReportGrouping? = null,
    budgetCategory: BudgetCategory? = null,
    id: Long? = null,
    value: BigDecimal? = null,
    transactionTags: List<String>? = null,
    reportId: Long
): ReportGroupTransactionHistory {
    val report = ReportGroupTransactionHistory(
            date = date,
            linkedId = linkedId,
            name = linkedName,
            value = value ?: BigDecimal(34.67),
            budget = BigDecimal(30.00),
            transactionIds = null,
            period = period,
            filteredBudgetCategory = budgetCategory,
            transactionTags = transactionTags,
            grouping = grouping ?: ReportGrouping.values()[Random.nextInt(ReportGrouping.values().size)],
            reportId = reportId)

    id?.let { report.reportGroupId = it }

    return report
}