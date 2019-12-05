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
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.sqlForBudget
import us.frollo.frollosdk.extensions.toBudget
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.model.api.budgets.BudgetType
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
     * Refresh all budgets from the host
     *
     *
     * @param current Fetch only currently active budgets(true) or all (false)
     * @param budgetType fetch budgets by [BudgetType]
     * @param completion Optional completion handler with optional error if the request fails
     *
     */
    fun refreshBudgets(
        current: Boolean = true,
        budgetType: BudgetType = BudgetType.BUDGET_CATEGORY,
        completion: OnFrolloSDKCompletionListener<Resource<List<Budget>>>? = null
    ) {
        val queryMap = mutableMapOf<String, String>()
        queryMap["current"] = current.toString()
        queryMap["category_type"] = budgetType.toString()
        budgetsAPI.fetchBudgets(queryMap).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshBudgets", resource.error?.localizedDescription)
                    completion?.invoke(Resource.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    db.budgetsDao().clear()
                    db.budgetsDao().clearBudgetPeriods()

                    val budgetResponseList = resource.data
                    budgetResponseList?.let {
                        val budgetList = it.map { budgetResponse -> budgetResponse.toBudget() }
                        db.budgetsDao().insertAll(*budgetList.toTypedArray())
                        completion?.invoke(Resource.success(budgetList))
                    }
                }
            }
        }
    }

    /**
     * Fetches all budgets from the local database
     */
    fun fetchAllBudgets(
        current: Boolean = false,
        budgetFrequency: BudgetFrequency? = null,
        budgetStatus: BudgetStatus? = null,
        budgetTrackingStatus: BudgetTrackingStatus? = null,
        budgetType: BudgetType? = null,
        budgetTypeValue: String? = null
    ): LiveData<Resource<List<Budget>>> =
            Transformations.map(db.budgetsDao().loadByQuery(sqlForBudget(current, budgetFrequency, budgetStatus, budgetTrackingStatus, budgetType, budgetTypeValue))) { models ->
                Resource.success(models)
            }
}
