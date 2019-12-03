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
import androidx.sqlite.db.SimpleSQLiteQuery
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.aggregation.Aggregation
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.extensions.changeDateFormat
import us.frollo.frollosdk.extensions.dailyToWeekly
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.fetchAccountBalanceReports
import us.frollo.frollosdk.extensions.fetchTransactionHistoryReports
import us.frollo.frollosdk.extensions.isValidFormat
import us.frollo.frollosdk.extensions.sqlForExistingAccountBalanceReports
import us.frollo.frollosdk.extensions.sqlForFetchingAccountBalanceReports
import us.frollo.frollosdk.extensions.sqlForHistoryReports
import us.frollo.frollosdk.extensions.sqlForStaleHistoryReportIds
import us.frollo.frollosdk.extensions.sqlForStaleIdsAccountBalanceReports
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toReportAccountBalance
import us.frollo.frollosdk.mapping.toReportGroupTransactionHistory
import us.frollo.frollosdk.mapping.toReportTransactionHistory
import us.frollo.frollosdk.model.api.reports.AccountBalanceReportResponse
import us.frollo.frollosdk.model.api.reports.TransactionHistoryReportResponse
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountType
import us.frollo.frollosdk.model.coredata.reports.ReportAccountBalanceRelation
import us.frollo.frollosdk.model.coredata.reports.ReportDateFormat
import us.frollo.frollosdk.model.coredata.reports.ReportDateFormat.Companion.DAILY
import us.frollo.frollosdk.model.coredata.reports.ReportDateFormat.Companion.DATE_PATTERN_FOR_REQUEST
import us.frollo.frollosdk.model.coredata.reports.ReportDateFormat.Companion.MONTHLY
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.coredata.reports.ReportTransactionHistory
import us.frollo.frollosdk.model.coredata.reports.ReportTransactionHistoryRelation
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.ReportsAPI

/**
 * Manages all aspects of reporting of aggregation data including spending and balances
 */
class Reports(network: NetworkService, private val db: SDKDatabase, private val aggregation: Aggregation) {

    companion object {
        private const val TAG = "Reports"
    }

    private val reportsAPI: ReportsAPI = network.create(ReportsAPI::class.java)

    // Account Balance Reports

    /**
     * Fetch account balance reports from the cache
     *
     * @param fromDate Start date in the format yyyy-MM-dd to fetch reports from (inclusive). See [ReportDateFormat.DATE_PATTERN_FOR_REQUEST]
     * @param toDate End date in the format yyyy-MM-dd to fetch reports up to (inclusive). See [ReportDateFormat.DATE_PATTERN_FOR_REQUEST]
     * @param period Period that reports should be broken down by
     * @param accountId Fetch reports for a specific account ID (optional)
     * @param accountType Fetch reports for a specific account type (optional)
     *
     * @return LiveData object of Resource<List<ReportAccountBalanceRelation>> which can be observed using an Observer for future changes as well.
     */
    fun accountBalanceReports(
        fromDate: String,
        toDate: String,
        period: ReportPeriod,
        accountId: Long? = null,
        accountType: AccountType? = null
    ): LiveData<Resource<List<ReportAccountBalanceRelation>>> {
        val from = fromDate.toReportDateFormat(period)
        val to = toDate.toReportDateFormat(period)

        return Transformations.map(db.reportsAccountBalance().loadWithRelation(sqlForFetchingAccountBalanceReports(from, to, period, accountId, accountType))) { model ->
            Resource.success(model)
        }
    }

    /**
     * Advanced method to fetch account balance reports by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches account balance reports from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<ReportAccountBalanceRelation>> which can be observed using an Observer for future changes as well.
     */
    fun accountBalanceReports(query: SimpleSQLiteQuery): LiveData<Resource<List<ReportAccountBalanceRelation>>> =
            Transformations.map(db.reportsAccountBalance().loadWithRelation(query)) { model ->
                Resource.success(model)
            }

    /**
     * Refresh account balance reports from the host
     *
     * @param fromDate Start date in the format yyyy-MM-dd to fetch reports from (inclusive). See [ReportDateFormat.DATE_PATTERN_FOR_REQUEST]
     * @param toDate End date in the format yyyy-MM-dd to fetch reports up to (inclusive). See [ReportDateFormat.DATE_PATTERN_FOR_REQUEST]
     * @param period Period that reports should be broken down by
     * @param accountId Fetch reports for a specific account ID (optional)
     * @param accountType Fetch reports for a specific account type (optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshAccountBalanceReports(
        fromDate: String,
        toDate: String,
        period: ReportPeriod,
        accountId: Long? = null,
        accountType: AccountType? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        reportsAPI.fetchAccountBalanceReports(period, fromDate, toDate, accountId, accountType).enqueue { resource ->
            when (resource.status) {
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

    // Transactions History Reports

    /**
     * Fetch historic transaction reports from the cache
     *
     * @param fromDate Start date in the format yyyy-MM-dd to fetch reports from (inclusive). See [ReportDateFormat.DATE_PATTERN_FOR_REQUEST]
     * @param toDate End date in the format yyyy-MM-dd to fetch reports up to (inclusive). See [ReportDateFormat.DATE_PATTERN_FOR_REQUEST]
     * @param grouping Grouping that reports should be broken down into
     * @param period Period that reports should be broken down by
     * @param budgetCategory Budget Category to filter reports by. Leave blank to return all reports of that grouping (Optional)
     * @param transactionTag Transaction tag that reports should be filtered by (Optional)
     *
     * @return LiveData object of Resource<List<ReportTransactionHistoryRelation>> which can be observed using an Observer for future changes as well.
     */
    fun historyTransactionReports(
        fromDate: String,
        toDate: String,
        grouping: ReportGrouping,
        period: ReportPeriod,
        budgetCategory: BudgetCategory? = null,
        transactionTag: String? = null
    ): LiveData<Resource<List<ReportTransactionHistoryRelation>>> {
        val from = fromDate.toReportDateFormat(period)
        val to = toDate.toReportDateFormat(period)

        return Transformations.map(db.reportsTransactionHistory().loadByQuery(sqlForHistoryReports(
                fromDate = from,
                toDate = to,
                grouping = grouping,
                period = period,
                budgetCategory = budgetCategory,
                transactionTag = transactionTag))) { model ->
            Resource.success(model)
        }
    }

    /**
     * Advanced method to fetch historic transaction reports by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches historic transaction reports from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<ReportTransactionHistoryRelation>> which can be observed using an Observer for future changes as well.
     */
    fun historyTransactionReports(query: SimpleSQLiteQuery): LiveData<Resource<List<ReportTransactionHistoryRelation>>> =
            Transformations.map(db.reportsTransactionHistory().loadByQuery(query)) { model ->
                Resource.success(model)
            }

    /**
     * Refresh transaction history reports from the host
     *
     * @param fromDate Start date in the format yyyy-MM-dd to fetch reports from (inclusive). See [ReportDateFormat.DATE_PATTERN_FOR_REQUEST]
     * @param toDate End date in the format yyyy-MM-dd to fetch reports up to (inclusive). See [ReportDateFormat.DATE_PATTERN_FOR_REQUEST]
     * @param grouping Grouping that reports should be broken down into
     * @param period Period that reports should be broken down by
     * @param budgetCategory Budget Category to filter reports by. Leave blank to return all reports of that grouping (Optional)
     * @param transactionTag Transaction tag that reports should be filtered by (Optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshTransactionHistoryReports(
        fromDate: String,
        toDate: String,
        grouping: ReportGrouping,
        period: ReportPeriod,
        budgetCategory: BudgetCategory? = null,
        transactionTag: String? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        reportsAPI.fetchTransactionHistoryReports(grouping, period, fromDate, toDate, budgetCategory, transactionTag).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshTransactionHistoryReports", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    val from = fromDate.toReportDateFormat(period)
                    val to = toDate.toReportDateFormat(period)

                    handleTransactionHistoryReportsResponse(
                            reportsResponse = resource.data?.data?.toMutableList(),
                            fromDate = from,
                            toDate = to,
                            grouping = grouping,
                            period = period,
                            budgetCategory = budgetCategory,
                            transactionTag = transactionTag,
                            completion = completion)
                }
            }
        }
    }

    // Response Handlers

    @Transaction
    private fun handleAccountBalanceReportsResponse(
        response: MutableList<AccountBalanceReportResponse.Report>?,
        period: ReportPeriod,
        accountId: Long?,
        accountType: AccountType?,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
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
    private fun handleAccountBalanceReportsForDate(
        reportsResponse: MutableList<AccountBalanceReportResponse.Report.BalanceReport>,
        date: String,
        period: ReportPeriod,
        accountId: Long?,
        accountType: AccountType?
    ) {
        try {
            // Sort by id
            reportsResponse.sortBy { it.id }

            // Fetch existing reports for updating
            val reportAccountsIds = reportsResponse.map { it.id }.toLongArray()

            val existingReports = db.reportsAccountBalance().find(sqlForExistingAccountBalanceReports(date, period, reportAccountsIds, accountId, accountType))

            // Sort by date
            existingReports.sortBy { it.date }

            var index = 0

            reportsResponse.forEach { response ->
                val report = response.toReportAccountBalance(date, period)

                if (index < existingReports.size && existingReports[index].accountId == response.id) {
                    // Update
                    report.reportId = existingReports[index].reportId

                    db.reportsAccountBalance().update(report)

                    index += 1
                } else {
                    // Insert
                    db.reportsAccountBalance().insert(report)
                }
            }

            // Fetch and delete any leftovers
            val staleIds = db.reportsAccountBalance().findStaleIds(sqlForStaleIdsAccountBalanceReports(date, period, reportAccountsIds, accountId, accountType))

            db.reportsAccountBalance().deleteMany(staleIds)
        } catch (e: Exception) {
            Log.e("$TAG#handleAccountBalanceReportsForDate", e.message)
        }
    }

    @Transaction
    private fun handleTransactionHistoryReportsResponse(
        reportsResponse: MutableList<TransactionHistoryReportResponse.Report>?,
        fromDate: String,
        toDate: String,
        grouping: ReportGrouping,
        period: ReportPeriod,
        budgetCategory: BudgetCategory?,
        transactionTag: String?,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        reportsResponse?.let {
            doAsync {
                try {
                    // Sort by date
                    reportsResponse.sortBy { it.date }

                    // Fetch existing reports for updating
                    val reportDates = reportsResponse.map { it.date }.toTypedArray()
                    val existingReports = db.reportsTransactionHistory().find(sqlForHistoryReports(
                            fromDate = fromDate,
                            toDate = toDate,
                            grouping = grouping,
                            period = period,
                            budgetCategory = budgetCategory,
                            dates = reportDates,
                            transactionTag = transactionTag))

                    // Sort by date
                    existingReports.sortBy { it.date }

                    var index = 0

                    reportsResponse.forEach { response ->
                        val report = response.toReportTransactionHistory(
                                grouping = grouping,
                                period = period,
                                budgetCategory = budgetCategory,
                                tags = transactionTag?.let { listOf(transactionTag) })

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
                    val staleReportIds = db.reportsTransactionHistory().findStaleIds(sqlForStaleHistoryReportIds(
                            fromDate = fromDate,
                            toDate = toDate,
                            grouping = grouping,
                            period = period,
                            budgetCategory = budgetCategory,
                            dates = reportDates,
                            transactionTag = transactionTag))

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
                    reportId = report.reportId,
                    tags = report.transactionTags)

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
            aggregation.fetchMissingMerchants(linkedIds.toSet())

        // Fetch and delete any leftovers
        val staleIds = db.reportGroupsTransactionHistory().findStaleIds(report.reportId, categoryReportIds)
        db.reportGroupsTransactionHistory().deleteMany(staleIds)
    }

    @Throws(FrolloSDKError::class)
    private fun String.toReportDateFormat(period: ReportPeriod): String {
        if (!this.isValidFormat(DATE_PATTERN_FOR_REQUEST)) {
            throw FrolloSDKError("Invalid format for from/to date")
        }

        var newDate = this

        if (period == ReportPeriod.MONTH) {
            newDate = this.changeDateFormat(from = DAILY, to = MONTHLY)
        } else if (period == ReportPeriod.WEEK) {
            newDate = this.dailyToWeekly()
        }

        return newDate
    }
}