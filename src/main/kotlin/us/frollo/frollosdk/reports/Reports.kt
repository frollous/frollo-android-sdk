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
import us.frollo.frollosdk.model.api.reports.TransactionCurrentReportResponse
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportTransactionCurrent
import us.frollo.frollosdk.model.coredata.reports.ReportTransactionCurrentRelation
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.network.api.ReportsAPI
import java.lang.Exception

class Reports(network: NetworkService, private val db: SDKDatabase, private val aggregation: Aggregation) {

    companion object {
        private const val TAG = "Reports"
    }

    private val reportsAPI: ReportsAPI = network.create(ReportsAPI::class.java)

    private var refreshingMerchantIDs = setOf<Long>()

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

    // Response Handlers

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
            reportsResponse.sortBy { it.day }

            val reportDays = reportsResponse.map { it.day }.toIntArray()

            val existingReports = db.reportsTransactionCurrent().find(
                    grouping = grouping,
                    budgetCategory = budgetCategory,
                    linkedId = linkedId,
                    days = reportDays)

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