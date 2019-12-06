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

package us.frollo.frollosdk.budgets

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.gson.JsonObject
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.fetchBudgets
import us.frollo.frollosdk.extensions.sqlForBudgets
import us.frollo.frollosdk.extensions.sqlForBudgetIds
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toBudget
import us.frollo.frollosdk.model.api.budgets.BudgetCreateRequest
import us.frollo.frollosdk.model.api.budgets.BudgetResponse
import us.frollo.frollosdk.model.coredata.budgets.BudgetType
import us.frollo.frollosdk.model.coredata.budgets.Budget
import us.frollo.frollosdk.model.coredata.budgets.BudgetFrequency
import us.frollo.frollosdk.model.coredata.budgets.BudgetStatus
import us.frollo.frollosdk.model.coredata.budgets.BudgetTrackingStatus
import us.frollo.frollosdk.network.api.BudgetsAPI

/** Manages user Budgets and tracking */
class Budgets(network: NetworkService, private val db: SDKDatabase) {

    companion object {
        private const val TAG = "Budgets"
    }

    private val budgetsAPI: BudgetsAPI = network.create(BudgetsAPI::class.java)

    /**
     * Fetches all budgets from the local database
     *
     * @param current Filter budgets by currently active budgets (Optional)
     * @param frequency Filter budgets by budget frequency (Optional)
     * @param status Filter budgets by budget status (Optional)
     * @param trackingStatus Filter budgets by tracking status (Optional)
     * @param type Filter budgets by budget type (Optional)
     * @param typeValue Filter budgets by budget type value (Optional)
     *
     * @return LiveData object of Resource<List<Budget>> which can be observed using an Observer for future changes as well.
     *
     */
    fun fetchBudgets(
        current: Boolean? = null,
        frequency: BudgetFrequency? = null,
        status: BudgetStatus? = null,
        trackingStatus: BudgetTrackingStatus? = null,
        type: BudgetType? = null,
        typeValue: String? = null
    ): LiveData<Resource<List<Budget>>> =
            Transformations.map(
                    db.budgets().loadByQuery(
                            sqlForBudgets(current, frequency, status, trackingStatus, type, typeValue)
                    )
            ) { models ->
                Resource.success(models)
            }

    /**
     * Refresh all budgets from the host
     *
     * @param current Filter budgets by currently active budgets (Optional)
     * @param budgetType Filter budgets by budget type (Optional)
     * @param completion Optional completion handler with optional error if the request fails
     *
     */
    fun refreshBudgets(
        current: Boolean? = null,
        budgetType: BudgetType? = null,
        completion: OnFrolloSDKCompletionListener<Result<List<Budget>>>? = null
    ) {

        budgetsAPI.fetchBudgets(current, budgetType).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshBudgets", resource.error?.localizedDescription)
                    completion?.invoke(Result.failure(Throwable("API error")))
                }
                Resource.Status.SUCCESS -> {
                    handleBudgetsResponse(response = resource.data, current = current, budgetType = budgetType, completion = completion)
                }
            }
        }
    }

    fun createBudget(
        budgetFrequency: BudgetFrequency,
        periodAmount: Long,
        type: BudgetType,
        typedValue: String,
        startDate: String?,
        imageUrl: String?,
        metadata: JsonObject?,
        completion: OnFrolloSDKCompletionListener<Resource<Budget>>? = null
    ) {
        val budgetCreateRequest = BudgetCreateRequest(budgetFrequency, periodAmount, type, typedValue, startDate, imageUrl, metadata)
        budgetsAPI.createBudget(budgetCreateRequest).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#createBudget", resource.error?.localizedDescription)
                    completion?.invoke(Resource.error(resource.error))
                }
                Resource.Status.SUCCESS -> {

                    val budgetResponseList = resource.data
                    budgetResponseList?.let {

                        val budget = it.toBudget()
                        db.budgets().insert(budget)
                        completion?.invoke(Resource.success(budget))
                    }
                }
            }
        }
    }

    private fun handleBudgetsResponse(
        response: List<BudgetResponse>?,
        current: Boolean? = null,
        budgetType: BudgetType? = null,
        completion: OnFrolloSDKCompletionListener<Result<List<Budget>>>?
    ) {
        var models = listOf<Budget>()
        response?.let { list ->
            doAsync {
                models = list.map { it.toBudget() }

                db.budgets().insertAll(*models.toTypedArray())

                val apiIds = models.map { it.budgetId }.toHashSet()
                val allBudgetIds = db.budgets().getIdsByQuery(sqlForBudgetIds(current, budgetType)).toHashSet()
                val staleIds = allBudgetIds.minus(apiIds)

                if (staleIds.isNotEmpty()) {
                    removeCachedBudgets(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Result.success(models)) }
            }
        } ?: run { completion?.invoke(Result.success(models)) } // Explicitly invoke completion callback if response is null.
    }

    // WARNING: Do not call this method on the main thread
    private fun removeCachedBudgets(budgetIds: LongArray) {
        if (budgetIds.isNotEmpty()) {
            db.budgets().deleteMany(budgetIds)

            // Manually delete budget periods associated to this goal
            // as we are not using ForeignKeys because ForeignKey constraints
            // do not allow to insert data into child table prior to parent table
            val budgetPeriodIds = db.budgetPeriods().getIdsByBudgetIds(budgetIds)
            removeCachedBudgetPeriods(budgetPeriodIds)
        }
    }

    // WARNING: Do not call this method on the main thread
    private fun removeCachedBudgetPeriods(budgetPeriodIds: LongArray) {
        if (budgetPeriodIds.isNotEmpty()) {
            db.budgetPeriods().deleteMany(budgetPeriodIds)
        }
    }
}
