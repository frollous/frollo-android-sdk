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

package us.frollo.frollosdk.reports

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.room.Transaction
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.aggregation.Aggregation
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.extensions.*
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.*
import us.frollo.frollosdk.model.api.reports.AccountBalanceReportResponse
import us.frollo.frollosdk.model.api.reports.TransactionCurrentReportResponse
import us.frollo.frollosdk.model.api.reports.TransactionHistoryReportResponse
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountType
import us.frollo.frollosdk.model.coredata.reports.*
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.network.api.ReportsAPI
import java.lang.Exception

class Reports(network: NetworkService, private val db: SDKDatabase, private val aggregation: Aggregation) {

    companion object {
        private const val TAG = "Reports"
    }

    private val reportsAPI: ReportsAPI = network.create(ReportsAPI::class.java)

    private var refreshingMerchantIDs = setOf<Long>()

    // Account Balance Reports

    fun accountBalanceReports(fromDate: String, toDate: String, period: ReportPeriod,
                              accountId: Long? = null, accountType: AccountType? = null): LiveData<Resource<List<ReportAccountBalanceRelation>>> =
            Transformations.map(db.reportAccountBalance().load(sqlForFetchingAccountBalanceReports(fromDate, toDate, period, accountId, accountType))) { model ->
                Resource.success(model)
            }

    fun refreshAccountBalanceReports(fromDate: String, toDate: String, period: ReportPeriod,
                                     accountId: Long? = null, accountType: AccountType? = null,
                                     completion: OnFrolloSDKCompletionListener<Result>? = null) {
        reportsAPI.fetchAccountBalanceReports(period, fromDate, toDate, accountId, accountType).enqueue { resource ->
            when(resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshAccountBalanceReports", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleAccountBalanceReportsResponse(
                            response = resource.data?.data?.toMutableList(),
                            period = period,
                            accountId = accountId,
                            accountType = accountType,
                            completion = completion)
                }
            }
        }
    }

    // Transactions Current Reports

    fun currentTransactionReports(grouping: ReportGrouping, budgetCategory: BudgetCategory? = null): LiveData<Resource<List<ReportTransactionCurrentRelation>>> =
            Transformations.map(db.reportsTransactionCurrent().load(grouping, budgetCategory)) { model ->
                Resource.success(model)
            }

    fun refreshTransactionCurrentReports(grouping: ReportGrouping, budgetCategory: BudgetCategory? = null, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        reportsAPI.fetchTransactionCurrentReports(grouping, budgetCategory).enqueue { resource ->
            when(resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshTransactionCurrentReports", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleTransactionCurrentReportsResponse(
                            response = resource.data,
                            grouping = grouping,
                            budgetCategory = budgetCategory,
                            completion = completion)
                }
            }
        }
    }

    // Transactions History Reports

    fun historyTransactionReports(fromDate: String, toDate: String, grouping: ReportGrouping,
                                  period: ReportPeriod, budgetCategory: BudgetCategory? = null): LiveData<Resource<List<ReportTransactionHistoryRelation>>> =
            Transformations.map(db.reportsTransactionHistory().load(fromDate, toDate, grouping, period, budgetCategory)) { model ->
                Resource.success(model)
            }

    fun refreshTransactionHistoryReports(fromDate: String, toDate: String, grouping: ReportGrouping,
                                         period: ReportPeriod, budgetCategory: BudgetCategory? = null,
                                         completion: OnFrolloSDKCompletionListener<Result>? = null) {
        reportsAPI.fetchTransactionHistoryReports(grouping, period, fromDate, toDate, budgetCategory).enqueue { resource ->
            when(resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshTransactionHistoryReports", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleTransactionHistoryReportsResponse(
                            reportsResponse = resource.data?.data?.toMutableList(),
                            fromDate = fromDate,
                            toDate = toDate,
                            grouping = grouping,
                            period = period,
                            budgetCategory = budgetCategory,
                            completion = completion)
                }
            }
        }
    }

    // Response Handlers

    @Transaction
    private fun handleAccountBalanceReportsResponse(response: MutableList<AccountBalanceReportResponse.Report>?,
                                                    period: ReportPeriod, accountId: Long?, accountType: AccountType?,
                                                    completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                // Sort by date
                response.sortBy { it.date }

                response.forEach { report ->
                    handleAccountBalanceReportsForDate(report.accounts.toMutableList(), report.date, period, accountId, accountType)
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    // WARNING: Do not call this method on the main thread
    @Transaction
    private fun handleAccountBalanceReportsForDate(reportsResponse: MutableList<AccountBalanceReportResponse.Report.BalanceReport>,
                                                   date: String, period: ReportPeriod, accountId: Long?, accountType: AccountType?) {
        try {
            // Sort by id
            reportsResponse.sortBy { it.id }

            // Fetch existing reports for updating
            val reportAccountsIds = reportsResponse.map { it.id }.toLongArray()

            val existingReports = db.reportAccountBalance().find(sqlForExistingAccountBalanceReports(date, period, reportAccountsIds, accountId, accountType))

            // Sort by date
            existingReports.sortBy { it.date }

            var index = 0

            reportsResponse.forEach { response ->
                val report = response.toReportAccountBalance(date, period)

                if (index < existingReports.size && existingReports[index].accountId == response.id) {
                    // Update
                    report.reportId = existingReports[index].reportId

                    db.reportAccountBalance().update(report)

                    index += 1
                } else {
                    // Insert
                    db.reportAccountBalance().insert(report)
                }
            }

            // Fetch and delete any leftovers
            val staleIds = db.reportAccountBalance().findStaleIds(sqlForStaleIdsAccountBalanceReports(date, period, reportAccountsIds, accountId, accountType))

            db.reportAccountBalance().deleteMany(staleIds)
        } catch (e: Exception) {
            Log.e("$TAG#handleAccountBalanceReportsForDate", e.message)
        }
    }

    private fun handleTransactionCurrentReportsResponse(response: TransactionCurrentReportResponse?,
                                                        grouping: ReportGrouping, budgetCategory: BudgetCategory?,
                                                        completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                val reportsToInsert = mutableListOf<ReportTransactionCurrent>()
                val reportsToUpdate = mutableListOf<ReportTransactionCurrent>()
                val idsToDelete = mutableListOf<Long>()
                val linkedIds = mutableListOf<Long>()

                handleTransactionCurrentDayReportsResponse(
                        reportsResponse = response.days.toMutableList(),
                        grouping = grouping,
                        budgetCategory = budgetCategory,
                        reportsToInsert = reportsToInsert,
                        reportsToUpdate = reportsToUpdate,
                        idsToDelete = idsToDelete)

                response.groups.forEach {
                    linkedIds.add(it.id)

                    handleTransactionCurrentDayReportsResponse(
                            reportsResponse = it.days.toMutableList(),
                            grouping = grouping,
                            budgetCategory = budgetCategory,
                            linkedId = it.id,
                            linkedName = it.name,
                            reportsToInsert = reportsToInsert,
                            reportsToUpdate = reportsToUpdate,
                            idsToDelete = idsToDelete)
                }

                // Refresh missing merchants
                if (grouping == ReportGrouping.MERCHANT)
                    fetchMissingMerchants(linkedIds.toSet())

                db.reportsTransactionCurrent().insertAndDeleteInTransaction(
                        new = reportsToInsert,
                        existing = reportsToUpdate,
                        staleIds = idsToDelete)

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    // WARNING: Do not call this method on the main thread
    private fun handleTransactionCurrentDayReportsResponse(reportsResponse: MutableList<TransactionCurrentReportResponse.Report>,
                                                           grouping: ReportGrouping, budgetCategory: BudgetCategory? = null,
                                                           linkedId: Long? = null, linkedName: String? = null,
                                                           reportsToInsert: MutableList<ReportTransactionCurrent>,
                                                           reportsToUpdate: MutableList<ReportTransactionCurrent>,
                                                           idsToDelete: MutableList<Long>) {
        try {
            // Sort by day
            reportsResponse.sortBy { it.day }

            // Fetch existing reports for updating
            val reportDays = reportsResponse.map { it.day }.toIntArray()

            val existingReports = db.reportsTransactionCurrent().find(
                    grouping = grouping,
                    budgetCategory = budgetCategory,
                    linkedId = linkedId,
                    days = reportDays)

            // Sort by day
            existingReports.sortBy { it.day }

            var index = 0

            reportsResponse.forEach { response ->
                val report = response.toReportTransactionCurrent(
                        grouping = grouping,
                        budgetCategory = budgetCategory,
                        linkedId = linkedId,
                        name = linkedName)

                if (index < existingReports.size && existingReports[index].day == response.day) {
                    // Update
                    report.reportId = existingReports[index].reportId

                    reportsToUpdate.add(report)

                    index += 1
                } else {
                    // Insert
                    reportsToInsert.add(report)
                }
            }

            // Fetch and delete any leftovers
            val staleIds = db.reportsTransactionCurrent().findStaleIds(
                    grouping = grouping,
                    budgetCategory = budgetCategory,
                    linkedId = linkedId,
                    days = reportDays)

            idsToDelete.addAll(staleIds)
        } catch (e: Exception) {
            Log.e("$TAG#handleTransactionCurrentDayReportsResponse", e.message)
        }
    }

    @Transaction
    private fun handleTransactionHistoryReportsResponse(reportsResponse: MutableList<TransactionHistoryReportResponse.Report>?,
                                                        fromDate: String, toDate: String, grouping: ReportGrouping,
                                                        period: ReportPeriod, budgetCategory: BudgetCategory?,
                                                        completion: OnFrolloSDKCompletionListener<Result>? = null) {
        reportsResponse?.let {
            doAsync {
                try {
                    // Sort by date
                    reportsResponse.sortBy { it.date }

                    // Fetch existing reports for updating
                    val reportDates = reportsResponse.map { it.date }.toTypedArray()

                    val existingReports = db.reportsTransactionHistory().find(
                            fromDate = fromDate,
                            toDate = toDate,
                            grouping = grouping,
                            period = period,
                            budgetCategory = budgetCategory,
                            dates = reportDates)

                    // Sort by date
                    existingReports.sortBy { it.date }

                    var index = 0

                    reportsResponse.forEach { response ->
                        val report = response.toReportTransactionHistory(grouping = grouping, period = period, budgetCategory = budgetCategory)

                        if (index < existingReports.size && existingReports[index].date == response.date) {
                            // Update
                            report.reportId = existingReports[index].reportId

                            db.reportsTransactionHistory().update(report)

                            index += 1
                        } else {
                            // Insert
                            report.reportId = db.reportsTransactionHistory().insert(report)
                        }

                        handleTransactionHistoryGroupReportsResponse(response.groups.toMutableList(), report)
                    }

                    // Fetch and delete any leftovers
                    val staleReportIds = db.reportsTransactionHistory().findStaleIds(
                            fromDate = fromDate,
                            toDate = toDate,
                            grouping = grouping,
                            period = period,
                            budgetCategory = budgetCategory,
                            dates = reportDates)

                    db.reportsTransactionHistory().deleteMany(staleReportIds)
                    db.reportGroupsTransactionHistory().deleteByReportIds(staleReportIds)
                } catch (e: Exception) {
                    Log.e("$TAG#handleTransactionHistoryReportsResponse", e.message)
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    // WARNING: Do not call this method on the main thread
    @Transaction
    private fun handleTransactionHistoryGroupReportsResponse(groupsResponse: MutableList<TransactionHistoryReportResponse.Report.GroupReport>, report: ReportTransactionHistory) {
        val linkedIds = mutableListOf<Long>()

        // Sort by linked id
        groupsResponse.sortBy { it.id }
        val categoryReportIds = groupsResponse.map { it.id }.toLongArray()

        val existingReportGroups = db.reportGroupsTransactionHistory().find(report.reportId, categoryReportIds)

        // Sort by linked id
        existingReportGroups.sortBy { it.linkedId }

        var index = 0

        groupsResponse.forEach { response ->
            linkedIds.add(response.id)

            val reportGroup = response.toReportGroupTransactionHistory(
                    grouping = report.grouping,
                    period = report.period,
                    budgetCategory = report.filteredBudgetCategory,
                    date = report.date,
                    reportId = report.reportId)

            if (index < existingReportGroups.size && existingReportGroups[index].linkedId == response.id) {
                // Update
                reportGroup.reportGroupId = existingReportGroups[index].reportGroupId

                db.reportGroupsTransactionHistory().update(reportGroup)

                index += 1
            } else {
                // Insert
                db.reportGroupsTransactionHistory().insert(reportGroup)
            }
        }

        // Refresh missing merchants
        if (report.grouping == ReportGrouping.MERCHANT)
            fetchMissingMerchants(linkedIds.toSet())

        // Fetch and delete any leftovers
        val staleIds = db.reportGroupsTransactionHistory().findStaleIds(report.reportId, categoryReportIds)
        db.reportGroupsTransactionHistory().deleteMany(staleIds)
    }

    private fun fetchMissingMerchants(merchantIds: Set<Long>) {
        doAsync {
            val existingMerchantIds = db.merchants().getIds().toSet()
            val missingMerchantIds = merchantIds.compareToFindMissingItems(existingMerchantIds).minus(refreshingMerchantIDs)
            if (missingMerchantIds.isNotEmpty()) {
                refreshingMerchantIDs = refreshingMerchantIDs.plus(missingMerchantIds)
                aggregation.refreshMerchants(missingMerchantIds.toLongArray())
            }
        }
    }
}