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

import us.frollo.frollosdk.model.api.reports.AccountBalanceReportResponse
import us.frollo.frollosdk.model.api.reports.TransactionCurrentReportResponse
import us.frollo.frollosdk.model.api.reports.TransactionHistoryReportResponse
import us.frollo.frollosdk.model.coredata.reports.ReportAccountBalance
import us.frollo.frollosdk.model.coredata.reports.ReportGroupTransactionHistory
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.coredata.reports.ReportTransactionCurrent
import us.frollo.frollosdk.model.coredata.reports.ReportTransactionHistory
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory

internal fun AccountBalanceReportResponse.Report.BalanceReport.toReportAccountBalance(date: String, period: ReportPeriod) =
        ReportAccountBalance(
                date = date,
                value = value,
                period = period,
                currency = currency,
                accountId = id)

internal fun TransactionCurrentReportResponse.Report.toReportTransactionCurrent(grouping: ReportGrouping, budgetCategory: BudgetCategory? = null, linkedId: Long?, name: String?) =
        ReportTransactionCurrent(
                day = day,
                linkedId = linkedId,
                name = name,
                amount = spendValue,
                previous = previousPeriodValue,
                average = averageValue,
                budget = budgetValue,
                filteredBudgetCategory = budgetCategory,
                grouping = grouping)

internal fun TransactionHistoryReportResponse.Report.toReportTransactionHistory(grouping: ReportGrouping, period: ReportPeriod, budgetCategory: BudgetCategory? = null) =
        ReportTransactionHistory(
                date = date,
                value = value,
                budget = budget,
                period = period,
                filteredBudgetCategory = budgetCategory,
                grouping = grouping)

internal fun TransactionHistoryReportResponse.Report.GroupReport.toReportGroupTransactionHistory(grouping: ReportGrouping, period: ReportPeriod, budgetCategory: BudgetCategory? = null, date: String, reportId: Long) =
        ReportGroupTransactionHistory(
                linkedId = id,
                name = name,
                value = value,
                budget = budget,
                transactionIds = transactionIds,
                period = period,
                date = date,
                filteredBudgetCategory = budgetCategory,
                grouping = grouping,
                reportId = reportId)