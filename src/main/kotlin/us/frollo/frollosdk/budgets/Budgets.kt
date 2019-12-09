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
import androidx.sqlite.db.SimpleSQLiteQuery
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
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.model.api.budgets.BudgetCreateRequest
import us.frollo.frollosdk.model.api.budgets.BudgetResponse
import us.frollo.frollosdk.model.api.budgets.BudgetUpdateRequest
import us.frollo.frollosdk.model.coredata.budgets.BudgetType
import us.frollo.frollosdk.model.coredata.budgets.Budget
import us.frollo.frollosdk.model.coredata.budgets.BudgetFrequency
import us.frollo.frollosdk.model.coredata.budgets.BudgetRelation
import us.frollo.frollosdk.model.coredata.budgets.BudgetStatus
import us.frollo.frollosdk.model.coredata.budgets.BudgetTrackingStatus
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.network.api.BudgetsAPI
import java.math.BigDecimal

/** Manages user Budgets and tracking */
class Budgets(network: NetworkService, private val db: SDKDatabase) {

    companion object {
        private const val TAG = "Budgets"
    }

    private val budgetsAPI: BudgetsAPI = network.create(BudgetsAPI::class.java)

    /**
     * Fetch budgets from the cache by Merchant
     *
     * @param merchantId Filter budgets by specific merchant
     * @param current Filter budgets by currently active budgets (Optional)
     * @param frequency Filter budgets by budget frequency (Optional)
     * @param status Filter budgets by budget status (Optional)
     * @param trackingStatus Filter budgets by tracking status (Optional)
     *
     * @return LiveData object of Resource<List<Budget>> which can be observed using an Observer for future changes as well.
     *
     */
    private fun fetchMerchantBudgets(
        merchantId: Long,
        current: Boolean? = null,
        frequency: BudgetFrequency? = null,
        status: BudgetStatus? = null,
        trackingStatus: BudgetTrackingStatus? = null

    ): LiveData<Resource<List<Budget>>> =
            fetchBudgets(current, frequency, status, trackingStatus, BudgetType.MERCHANT, merchantId.toString())

    /**
     * Fetch budgets from the cache by Budget Category
     *
     * @param budgetCategory Filter budgets by specific budget category
     * @param current Filter budgets by currently active budgets (Optional)
     * @param frequency Filter budgets by budget frequency (Optional)
     * @param status Filter budgets by budget status (Optional)
     * @param trackingStatus Filter budgets by tracking status (Optional)
     *
     * @return LiveData object of Resource<List<Budget>> which can be observed using an Observer for future changes as well.
     *
     */
    private fun fetchBudgetCategoryBudgets(
        budgetCategory: BudgetCategory,
        current: Boolean? = null,
        frequency: BudgetFrequency? = null,
        status: BudgetStatus? = null,
        trackingStatus: BudgetTrackingStatus? = null
    ): LiveData<Resource<List<Budget>>> =
            fetchBudgets(current, frequency, status, trackingStatus, BudgetType.BUDGET_CATEGORY, budgetCategory.toString())

    /**
     * Fetch budgets from the cache by Category
     *
     * @param categoryId Filter budgets by specific merchant
     * @param current Filter budgets by currently active budgets (Optional)
     * @param frequency Filter budgets by budget frequency (Optional)
     * @param status Filter budgets by budget status (Optional)
     * @param trackingStatus Filter budgets by tracking status (Optional)
     *
     * @return LiveData object of Resource<List<Budget>> which can be observed using an Observer for future changes as well.
     *
     */
    private fun fetchCategoryBudgets(
        categoryId: Long,
        current: Boolean? = null,
        frequency: BudgetFrequency? = null,
        status: BudgetStatus? = null,
        trackingStatus: BudgetTrackingStatus? = null
    ): LiveData<Resource<List<Budget>>> =
            fetchBudgets(current, frequency, status, trackingStatus, BudgetType.TRANSACTION_CATEGORY, categoryId.toString())

    /**
     * Fetch budget by ID from the cache
     *
     * @param budgetId Unique budget ID to fetch
     *
     * @return LiveData object of Resource<Budget> which can be observed using an Observer for future changes as well.
     */
    fun fetchBudget(budgetId: Long): LiveData<Resource<Budget>> =
            Transformations.map(db.budgets().load(budgetId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch budget by ID from the cache along with other associated data.
     *
     * @param budgetId Unique budget ID to fetch
     *
     * @return LiveData object of Resource<BudgetRelation> which can be observed using an Observer for future changes as well.
     */
    fun fetchBudgetWithRelation(budgetId: Long): LiveData<Resource<BudgetRelation>> =
            Transformations.map(db.budgets().loadWithRelation(budgetId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch budgets from the cache
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
     */
    fun refreshBudgets(
        current: Boolean? = null,
        budgetType: BudgetType? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        budgetsAPI.fetchBudgets(current, budgetType).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshBudgets", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleBudgetsResponse(response = resource.data, current = current, budgetType = budgetType, completion = completion)
                }
            }
        }
    }

    /**
     * Refresh a specific budget by ID from the host
     *
     * @param budgetId ID of the budget to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshBudget(budgetId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        budgetsAPI.fetchBudget(budgetId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshBudget", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleBudgetResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Update a budget on the host
     *
     * @param budget Updated budget data model
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun updateBudget(budget: Budget, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        val request = BudgetUpdateRequest(
                periodAmount = budget.periodAmount,
                imageUrl = budget.imageUrl,
                metadata = budget.metadata ?: JsonObject())

        budgetsAPI.updateBudget(budget.budgetId, request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateBudget", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleBudgetResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Cancel a specific budget by ID from the host
     *
     * @param budgetId ID of the budget to be abandoned
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun deleteBudget(budgetId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        budgetsAPI.deleteBudget(budgetId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("#deleteBudget", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    removeCachedBudgets(longArrayOf(budgetId))
                    completion?.invoke(Result.success())
                }
            }
        }
    }

    /**
     * Advanced method to fetch budgets by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches goals from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<Budget>> which can be observed using an Observer for future changes as well.
     */
    fun fetchBudgets(query: SimpleSQLiteQuery): LiveData<Resource<List<Budget>>> =
            Transformations.map(db.budgets().loadByQuery(query)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch budgets with relation from the cache by merchant
     *
     * @param merchantId Filter budgets by specific merchant
     * @param current Filter budgets by currently active budgets (Optional)
     * @param frequency Filter budgets by budget frequency (Optional)
     * @param status Filter budgets by budget status (Optional)
     * @param trackingStatus Filter budgets by tracking status (Optional)
     *
     * @return LiveData object of Resource<List<Budget>> which can be observed using an Observer for future changes as well.
     *
     */
    private fun fetchMerchantBudgetsWithRelation(
        merchantId: Long,
        current: Boolean? = null,
        frequency: BudgetFrequency? = null,
        status: BudgetStatus? = null,
        trackingStatus: BudgetTrackingStatus? = null
    ): LiveData<Resource<List<BudgetRelation>>> =
            fetchBudgetsWithRelation(current, frequency, status, trackingStatus, BudgetType.MERCHANT, merchantId.toString())

    /**
     * Fetch budgets with relation from the cache by Budget Category
     *
     * @param budgetCategory Filter budgets by specific budget category
     * @param current Filter budgets by currently active budgets (Optional)
     * @param frequency Filter budgets by budget frequency (Optional)
     * @param status Filter budgets by budget status (Optional)
     * @param trackingStatus Filter budgets by tracking status (Optional)
     *
     * @return LiveData object of Resource<List<Budget>> which can be observed using an Observer for future changes as well.
     *
     */
    private fun fetchBudgetCategoryBudgetsWithRelation(
        current: Boolean? = null,
        frequency: BudgetFrequency? = null,
        status: BudgetStatus? = null,
        trackingStatus: BudgetTrackingStatus? = null,
        budgetCategory: BudgetCategory
    ): LiveData<Resource<List<BudgetRelation>>> =
            fetchBudgetsWithRelation(current, frequency, status, trackingStatus, BudgetType.BUDGET_CATEGORY, budgetCategory.toString())

    /**
     * Fetch budgets with relation from the cache by transaction category
     *
     * @param categoryId Filter budgets by specific merchant
     * @param current Filter budgets by currently active budgets (Optional)
     * @param frequency Filter budgets by budget frequency (Optional)
     * @param status Filter budgets by budget status (Optional)
     * @param trackingStatus Filter budgets by tracking status (Optional)
     *
     * @return LiveData object of Resource<List<Budget>> which can be observed using an Observer for future changes as well.
     *
     */
    private fun fetchCategoryBudgetsWithRelation(
        categoryId: Long,
        current: Boolean? = null,
        frequency: BudgetFrequency? = null,
        status: BudgetStatus? = null,
        trackingStatus: BudgetTrackingStatus? = null
    ): LiveData<Resource<List<BudgetRelation>>> =
            fetchBudgetsWithRelation(current, frequency, status, trackingStatus, BudgetType.TRANSACTION_CATEGORY, categoryId.toString())

    /**
     * Fetch budgets from the cache with associated data
     *
     * @param current Filter by budget if its current (optional)
     * @param frequency Filter by the [BudgetFrequency] of the budget (optional)
     * @param status Filter by the [BudgetTrackingStatus] of the budget (optional)
     * @param trackingStatus Filter by [BudgetTrackingStatus] of the budget (optional)
     * @param type Filter by the [BudgetType] with which the budgetId are associated with (optional)
     * @param typeValue Either BudgetCategory or merchantId or categoryId (optional)
     *
     * @return LiveData object of Resource<List<BudgetRelation> which can be observed using an Observer for future changes as well.
     */
    fun fetchBudgetsWithRelation(
        current: Boolean?,
        frequency: BudgetFrequency? = null,
        status: BudgetStatus? = null,
        trackingStatus: BudgetTrackingStatus? = null,
        type: BudgetType? = null,
        typeValue: String? = null
    ): LiveData<Resource<List<BudgetRelation>>> =
            Transformations.map(db.budgets().loadByQueryWithRelation(
                    sqlForBudgets(
                            current = current,
                            budgetFrequency = frequency,
                            budgetStatus = status,
                            budgetTrackingStatus = trackingStatus,
                            budgetType = type,
                            budgetTypeValue = typeValue))
            ) { models ->
                Resource.success(models)
            }

    /**
     * Advanced method to fetch budgets by SQL query from the cache with associated data
     *
     * @param query SimpleSQLiteQuery: Select query which fetches goals from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<BudgetRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchBudgetWithRelation(query: SimpleSQLiteQuery): LiveData<Resource<List<BudgetRelation>>> =
            Transformations.map(db.budgets().loadByQueryWithRelation(query)) { model ->
                Resource.success(model)
            }

    /**
     * Create a new budget on the host by Budget category
     *
     * @param budgetFrequency The frequency at which you want to split up this budget. Refer [BudgetFrequency]
     * @param periodAmount Amount allocated the Budget period
     * @param budgetCategory Unique if of the category, based on which you create a budget
     * @param startDate Start date of the budget. Defaults to today (Optional)
     * @param imageUrl Image Url of the budget. Defaults to today (Optional)
     * @param metadata Metadata - custom JSON to be stored with the budget (Optional)
     * @param periodAmount Amount allocated the Budget period
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun createBudgetCategoryBudget(
        budgetFrequency: BudgetFrequency,
        periodAmount: BigDecimal,
        budgetCategory: BudgetCategory,
        startDate: String? = null,
        imageUrl: String? = null,
        metadata: JsonObject? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) = createBudget(budgetFrequency, periodAmount, BudgetType.BUDGET_CATEGORY, budgetCategory.toString(), startDate, imageUrl, metadata, completion)

    /**
     * Create a new budget on the host by transaction category
     *
     * @param budgetFrequency The frequency at which you want to split up this budget. Refer [BudgetFrequency]
     * @param periodAmount Amount allocated the Budget period
     * @param categoryId Unique if of the category, based on which you create a budget
     * @param startDate Start date of the budget. Defaults to today (Optional)
     * @param imageUrl Image Url of the budget. Defaults to today (Optional)
     * @param metadata Metadata - custom JSON to be stored with the budget (Optional)
     * @param periodAmount Amount allocated the Budget period
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun createCategoryBudget(
        budgetFrequency: BudgetFrequency,
        periodAmount: BigDecimal,
        categoryId: Long,
        startDate: String? = null,
        imageUrl: String? = null,
        metadata: JsonObject? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) = createBudget(budgetFrequency, periodAmount, BudgetType.TRANSACTION_CATEGORY, categoryId.toString(), startDate, imageUrl, metadata, completion)

    /**
     * Create a new budget on the host by merchant
     *
     * @param budgetFrequency The frequency at which you want to split up this budget. Refer [BudgetFrequency]
     * @param periodAmount Amount allocated the Budget period
     * @param merchantId Unique if of the merchant, based on which you create a budget
     * @param startDate Start date of the budget. Defaults to today (Optional)
     * @param imageUrl Image Url of the budget. Defaults to today (Optional)
     * @param metadata Metadata - custom JSON to be stored with the budget (Optional)
     * @param periodAmount Amount allocated the Budget period
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun createMerchantBudget(
        budgetFrequency: BudgetFrequency,
        periodAmount: BigDecimal,
        merchantId: Long,
        startDate: String? = null,
        imageUrl: String? = null,
        metadata: JsonObject? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) = createBudget(budgetFrequency, periodAmount, BudgetType.MERCHANT, merchantId.toString(), startDate, imageUrl, metadata, completion)

    /**
     * Create a new budget on the host
     *
     * @param budgetFrequency The frequency at which you want to split up this budget. Refer [BudgetFrequency]
     * @param periodAmount Amount allocated the Budget period
     * @param type [BudgetType]
     * @param typedValue Budget category, categoryId or merchantId
     * @param startDate Start date of the budget. Defaults to today (Optional)
     * @param imageUrl Image Url of the budget. Defaults to today (Optional)
     * @param metadata Metadata - custom JSON to be stored with the budget (Optional)
     * @param periodAmount Amount allocated the Budget period
     * @param completion Optional completion handler with optional error if the request fails
     */
    private fun createBudget(
        budgetFrequency: BudgetFrequency,
        periodAmount: BigDecimal,
        type: BudgetType,
        typedValue: String,
        startDate: String?,
        imageUrl: String?,
        metadata: JsonObject?,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        val budgetCreateRequest = BudgetCreateRequest(
                budgetFrequency = budgetFrequency,
                periodAmount = periodAmount,
                type = type,
                typedValue = typedValue,
                startDate = startDate,
                imageUrl = imageUrl,
                metadata = metadata)
        budgetsAPI.createBudget(budgetCreateRequest).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#createBudget", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleBudgetResponse(resource.data, completion)
                }
            }
        }
    }

    private fun handleBudgetResponse(response: BudgetResponse?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                val budget = response.toBudget()
                db.budgets().insert(budget)

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleBudgetsResponse(
        response: List<BudgetResponse>?,
        current: Boolean? = null,
        budgetType: BudgetType? = null,
        completion: OnFrolloSDKCompletionListener<Result>?
    ) {

        response?.let { list ->
            doAsync {
                val models = mapBudgetResponse(list)

                db.budgets().insertAll(*models.toTypedArray())

                val apiIds = models.map { it.budgetId }.toHashSet()
                val allBudgetIds = db.budgets().getIdsByQuery(sqlForBudgetIds(current, budgetType)).toHashSet()
                val staleIds = allBudgetIds.minus(apiIds)

                if (staleIds.isNotEmpty()) {
                    removeCachedBudgets(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun mapBudgetResponse(list: List<BudgetResponse>): List<Budget> = list.map { it.toBudget() }

    // WARNING: Do not call this method on the main thread
    private fun removeCachedBudgets(budgetIds: LongArray) {
        if (budgetIds.isNotEmpty()) {
            db.budgets().deleteMany(budgetIds)

            // Manually delete budget periods associated to this budget
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
