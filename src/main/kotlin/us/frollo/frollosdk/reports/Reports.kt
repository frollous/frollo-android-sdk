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
import us.frollo.frollosdk.aggregation.Aggregation
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.base.doAsync
import us.frollo.frollosdk.base.uiThread
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.extensions.changeDateFormat
import us.frollo.frollosdk.extensions.dailyToWeekly
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.fetchAccountBalanceReports
import us.frollo.frollosdk.extensions.fetchBudgetCategoryReports
import us.frollo.frollosdk.extensions.fetchMerchantReports
import us.frollo.frollosdk.extensions.fetchTagReports
import us.frollo.frollosdk.extensions.fetchTransactionCategoryReports
import us.frollo.frollosdk.extensions.isValidDateFormat
import us.frollo.frollosdk.extensions.sqlForExistingAccountBalanceReports
import us.frollo.frollosdk.extensions.sqlForFetchingAccountBalanceReports
import us.frollo.frollosdk.extensions.sqlForStaleIdsAccountBalanceReports
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toReportAccountBalance
import us.frollo.frollosdk.mapping.toReports
import us.frollo.frollosdk.model.api.reports.AccountBalanceReportResponse
import us.frollo.frollosdk.model.api.reports.ReportsResponse
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountType
import us.frollo.frollosdk.model.coredata.reports.Report
import us.frollo.frollosdk.model.coredata.reports.ReportAccountBalance
import us.frollo.frollosdk.model.coredata.reports.ReportAccountBalanceRelation
import us.frollo.frollosdk.model.coredata.reports.ReportDateFormat
import us.frollo.frollosdk.model.coredata.reports.ReportDateFormat.Companion.DAILY
import us.frollo.frollosdk.model.coredata.reports.ReportDateFormat.Companion.DATE_PATTERN_FOR_REQUEST
import us.frollo.frollosdk.model.coredata.reports.ReportDateFormat.Companion.MONTHLY
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.coredata.reports.TransactionReportPeriod
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.ReportsAPI

/**
 * Manages all aspects of reporting of aggregation data including spending and balances
 */
class Reports(network: NetworkService, internal val db: SDKDatabase, private val aggregation: Aggregation) {

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
    fun fetchAccountBalanceReports(
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
    fun fetchAccountBalanceReports(query: SimpleSQLiteQuery): LiveData<Resource<List<ReportAccountBalanceRelation>>> =
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
                        completion = completion
                    )
                }
            }
        }
    }

    // Transactions Reports

    /**
     * Fetch transaction category reports from the host
     *
     * @param fromDate Start date in the format yyyy-MM-dd to fetch reports from (inclusive). See [Report.DATE_FORMAT_PATTERN]
     * @param toDate End date in the format yyyy-MM-dd to fetch reports up to (inclusive). See [Report.DATE_FORMAT_PATTERN]
     * @param period Period that reports should be broken down by
     * @param categoryId Transaction category ID that reports should be filtered by. Leave blank to return all reports of that grouping (Optional)
     * @param grouping Grouping that reports should be broken down into (Optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun fetchTransactionCategoryReports(
        fromDate: String,
        toDate: String,
        period: TransactionReportPeriod,
        categoryId: Long? = null,
        grouping: ReportGrouping? = null,
        completion: OnFrolloSDKCompletionListener<Resource<List<Report>>>
    ) {
        reportsAPI.fetchTransactionCategoryReports(
            fromDate = fromDate,
            toDate = toDate,
            period = period,
            grouping = grouping,
            categoryId = categoryId
        ).enqueue { resource ->

            handleTransactionReports(
                resource = resource,
                period = period,
                grouping = grouping ?: ReportGrouping.TRANSACTION_CATEGORY, // Set default grouping only while sending back result to the app
                completion = completion
            )
        }
    }

    /**
     * Fetch merchant category reports from the host
     *
     * @param fromDate Start date in the format yyyy-MM-dd to fetch reports from (inclusive). See [Report.DATE_FORMAT_PATTERN]
     * @param toDate End date in the format yyyy-MM-dd to fetch reports up to (inclusive). See [Report.DATE_FORMAT_PATTERN]
     * @param period Period that reports should be broken down by
     * @param merchantId Merchant ID that reports should be filtered by. Leave blank to return all reports of that grouping (Optional)
     * @param grouping Grouping that reports should be broken down into (Optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun fetchMerchantReports(
        fromDate: String,
        toDate: String,
        period: TransactionReportPeriod,
        merchantId: Long? = null,
        grouping: ReportGrouping? = null,
        completion: OnFrolloSDKCompletionListener<Resource<List<Report>>>
    ) {
        reportsAPI.fetchMerchantReports(
            fromDate = fromDate,
            toDate = toDate,
            period = period,
            grouping = grouping,
            merchantId = merchantId
        ).enqueue { resource ->

            handleTransactionReports(
                resource = resource,
                period = period,
                grouping = grouping ?: ReportGrouping.MERCHANT, // Set default grouping only while sending back result to the app
                completion = completion
            )
        }
    }

    /**
     * Fetch budget category reports from the host
     *
     * @param fromDate Start date in the format yyyy-MM-dd to fetch reports from (inclusive). See [Report.DATE_FORMAT_PATTERN]
     * @param toDate End date in the format yyyy-MM-dd to fetch reports up to (inclusive). See [Report.DATE_FORMAT_PATTERN]
     * @param period Period that reports should be broken down by
     * @param budgetCategory Budget Category to filter reports by. Leave blank to return all reports of that grouping (Optional)
     * @param grouping Grouping that reports should be broken down into (Optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun fetchBudgetCategoryReports(
        fromDate: String,
        toDate: String,
        period: TransactionReportPeriod,
        budgetCategory: BudgetCategory? = null,
        grouping: ReportGrouping? = null,
        completion: OnFrolloSDKCompletionListener<Resource<List<Report>>>
    ) {
        reportsAPI.fetchBudgetCategoryReports(
            fromDate = fromDate,
            toDate = toDate,
            period = period,
            grouping = grouping,
            budgetCategory = budgetCategory
        ).enqueue { resource ->

            handleTransactionReports(
                resource = resource,
                period = period,
                grouping = grouping ?: ReportGrouping.BUDGET_CATEGORY, // Set default grouping only while sending back result to the app
                completion = completion
            )
        }
    }

    /**
     * Fetch tag reports from the host
     *
     * @param fromDate Start date in the format yyyy-MM-dd to fetch reports from (inclusive). See [Report.DATE_FORMAT_PATTERN]
     * @param toDate End date in the format yyyy-MM-dd to fetch reports up to (inclusive). See [Report.DATE_FORMAT_PATTERN]
     * @param period Period that reports should be broken down by
     * @param transactionTag Transaction tag that reports should be filtered by. Leave blank to return all reports of that grouping (Optional)
     * @param grouping Grouping that reports should be broken down into (Optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun fetchTagReports(
        fromDate: String,
        toDate: String,
        period: TransactionReportPeriod,
        transactionTag: String? = null,
        grouping: ReportGrouping? = null,
        completion: OnFrolloSDKCompletionListener<Resource<List<Report>>>
    ) {
        reportsAPI.fetchTagReports(
            fromDate = fromDate,
            toDate = toDate,
            period = period,
            grouping = grouping,
            transactionTag = transactionTag
        ).enqueue { resource ->

            handleTransactionReports(
                resource = resource,
                period = period,
                grouping = grouping ?: ReportGrouping.TAG, // Set default grouping only while sending back result to the app
                completion = completion
            )
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
            val reportAccountsIds = reportsResponse.map { it.id }.toLongArray()

            // Fetch existing reports for updating
            val existingReports = db.reportsAccountBalance().find(sqlForExistingAccountBalanceReports(date, period, reportAccountsIds, accountId, accountType))

            // Sort by date
            existingReports.sortBy { it.date }

            var index = 0

            val modelsToInsert = mutableListOf<ReportAccountBalance>()
            val modelsToUpdate = mutableListOf<ReportAccountBalance>()
            reportsResponse.forEach { response ->
                val report = response.toReportAccountBalance(date, period)

                if (index < existingReports.size && existingReports[index].accountId == response.id) {
                    // Update
                    report.reportId = existingReports[index].reportId

                    modelsToUpdate.add(report)

                    index += 1
                } else {
                    // Insert
                    modelsToInsert.add(report)
                }
            }

            db.reportsAccountBalance().update(*modelsToUpdate.toTypedArray())
            db.reportsAccountBalance().insert(*modelsToInsert.toTypedArray())

            // Fetch and delete any leftovers
            val staleIds = db.reportsAccountBalance().findStaleIds(sqlForStaleIdsAccountBalanceReports(date, period, reportAccountsIds, accountId, accountType))

            db.reportsAccountBalance().deleteMany(staleIds)
        } catch (e: Exception) {
            Log.e("$TAG#handleAccountBalanceReportsForDate", e.message)
        }
    }

    private fun handleTransactionReports(
        resource: Resource<ReportsResponse>,
        period: TransactionReportPeriod,
        grouping: ReportGrouping,
        completion: OnFrolloSDKCompletionListener<Resource<List<Report>>>
    ) {
        when (resource.status) {
            Resource.Status.ERROR -> {
                Log.e("$TAG#fetchTransactionReports", resource.error?.localizedDescription)
                completion.invoke(resource.map { null })
            }
            Resource.Status.SUCCESS -> {
                completion.invoke(
                    resource.map { response ->
                        response?.toReports(grouping, period)
                    }
                )
            }
        }
    }

    @Throws(FrolloSDKError::class)
    internal fun String.toReportDateFormat(period: ReportPeriod): String {
        if (!this.isValidDateFormat(DATE_PATTERN_FOR_REQUEST)) {
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
