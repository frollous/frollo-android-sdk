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
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.fetchBudgetPeriods
import us.frollo.frollosdk.extensions.fetchBudgets
import us.frollo.frollosdk.extensions.sqlForBudgetIds
import us.frollo.frollosdk.extensions.sqlForBudgetPeriodIds
import us.frollo.frollosdk.extensions.sqlForBudgetPeriods
import us.frollo.frollosdk.extensions.sqlForBudgets
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toBudget
import us.frollo.frollosdk.mapping.toBudgetPeriod
import us.frollo.frollosdk.model.api.budgets.BudgetCreateRequest
import us.frollo.frollosdk.model.api.budgets.BudgetPeriodResponse
import us.frollo.frollosdk.model.api.budgets.BudgetResponse
import us.frollo.frollosdk.model.api.budgets.BudgetUpdateRequest
import us.frollo.frollosdk.model.coredata.budgets.Budget
import us.frollo.frollosdk.model.coredata.budgets.BudgetFrequency
import us.frollo.frollosdk.model.coredata.budgets.BudgetPeriod
import us.frollo.frollosdk.model.coredata.budgets.BudgetPeriodRelation
import us.frollo.frollosdk.model.coredata.budgets.BudgetRelation
import us.frollo.frollosdk.model.coredata.budgets.BudgetStatus
import us.frollo.frollosdk.model.coredata.budgets.BudgetTrackingStatus
import us.frollo.frollosdk.model.coredata.budgets.BudgetType
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.BudgetsAPI
import java.math.BigDecimal

/** Manages user Budgets and tracking */
class Budgets(network: NetworkService, private val db: SDKDatabase) {

    companion object {
        private const val TAG = "Budgets"
    }

    private val budgetsAPI: BudgetsAPI = network.create(BudgetsAPI::class.java)

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
    fun fetchMerchantBudgets(
        merchantId: Long? = null,
        current: Boolean? = null,
        frequency: BudgetFrequency? = null,
        status: BudgetStatus? = null,
        trackingStatus: BudgetTrackingStatus? = null

    ): LiveData<Resource<List<Budget>>> =
        fetchBudgets(current, frequency, status, trackingStatus, BudgetType.MERCHANT, merchantId?.toString())

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
    fun fetchBudgetCategoryBudgets(
        budgetCategory: BudgetCategory? = null,
        current: Boolean? = null,
        frequency: BudgetFrequency? = null,
        status: BudgetStatus? = null,
        trackingStatus: BudgetTrackingStatus? = null
    ): LiveData<Resource<List<Budget>>> =
        fetchBudgets(current, frequency, status, trackingStatus, BudgetType.BUDGET_CATEGORY, budgetCategory?.toString())

    /**
     * Fetch budgets from the cache by Category
     *
     * @param categoryId Filter budgets by specific transaction category ID
     * @param current Filter budgets by currently active budgets (Optional)
     * @param frequency Filter budgets by budget frequency (Optional)
     * @param status Filter budgets by budget status (Optional)
     * @param trackingStatus Filter budgets by tracking status (Optional)
     *
     * @return LiveData object of Resource<List<Budget>> which can be observed using an Observer for future changes as well.
     *
     */
    fun fetchTransactionCategoryBudgets(
        categoryId: Long? = null,
        current: Boolean? = null,
        frequency: BudgetFrequency? = null,
        status: BudgetStatus? = null,
        trackingStatus: BudgetTrackingStatus? = null
    ): LiveData<Resource<List<Budget>>> =
        fetchBudgets(current, frequency, status, trackingStatus, BudgetType.TRANSACTION_CATEGORY, categoryId?.toString())

    /**
     * Fetch budgets from the cache
     *
     * @param current Filter budgets by currently active budgets (Optional)
     * @param frequency Filter budgets by budget frequency (Optional)
     * @param status Filter budgets by budget status (Optional)
     * @param trackingStatus Filter budgets by tracking status (Optional)
     * @param type Filter budgets by budget type (Optional)
     * @param typeValue Filter budgets by budget type value (Optional). This can be transaction category ID or merchant ID or budget category raw string based on the Budget Type.
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
     * @param categoryId Filter budgets by specific transaction category ID
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
        current: Boolean? = null,
        frequency: BudgetFrequency? = null,
        status: BudgetStatus? = null,
        trackingStatus: BudgetTrackingStatus? = null,
        type: BudgetType? = null,
        typeValue: String? = null
    ): LiveData<Resource<List<BudgetRelation>>> =
        Transformations.map(
            db.budgets().loadByQueryWithRelation(
                sqlForBudgets(
                    current = current,
                    budgetFrequency = frequency,
                    budgetStatus = status,
                    budgetTrackingStatus = trackingStatus,
                    budgetType = type,
                    budgetTypeValue = typeValue
                )
            )
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
     * Create a new budget on the host by Budget category
     *
     * @param budgetFrequency The frequency at which you want to split up this budget. Refer [BudgetFrequency]
     * @param periodAmount Amount allocated the Budget period
     * @param budgetCategory Budget category for which you want to create a budget
     * @param startDate Start date of the budget. Defaults to today (Optional)
     * @param imageUrl Image Url of the budget (Optional)
     * @param metadata Metadata - custom JSON to be stored with the budget (Optional)
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
     * @param categoryId Unique ID of the transaction category, based on which you create a budget
     * @param startDate Start date of the budget. Defaults to today (Optional)
     * @param imageUrl Image Url of the budget (Optional)
     * @param metadata Metadata - custom JSON to be stored with the budget (Optional)
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
     * @param merchantId Unique ID of the merchant, based on which you create a budget
     * @param startDate Start date of the budget. Defaults to today (Optional)
     * @param imageUrl Image Url of the budget (Optional)
     * @param metadata Metadata - custom JSON to be stored with the budget (Optional)
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
     * @param imageUrl Image Url of the budget (Optional)
     * @param metadata Metadata - custom JSON to be stored with the budget (Optional)
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
            metadata = metadata
        )
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
            metadata = budget.metadata ?: JsonObject()
        )

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
     * Delete a specific budget by ID from the host
     *
     * @param budgetId ID of the budget to be deleted
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

    // Budget Periods

    /**
     * Fetch budget period by ID from the cache
     *
     * @param budgetPeriodId Unique budget period ID to fetch
     *
     * @return LiveData object of Resource<BudgetPeriod> which can be observed using an Observer for future changes as well.
     */
    fun fetchBudgetPeriod(budgetPeriodId: Long): LiveData<Resource<BudgetPeriod>> =
        Transformations.map(db.budgetPeriods().load(budgetPeriodId)) { model ->
            Resource.success(model)
        }

    /**
     * Fetch budget periods from the cache
     *
     * @param budgetId Budget ID of the budget periods to fetch (optional)
     * @param trackingStatus Filter by the tracking status (optional)
     * @param fromDate Start date (inclusive) to fetch budgets from (optional). Please use [BudgetPeriod.DATE_FORMAT_PATTERN] for the format pattern.
     * @param toDate End date (inclusive) to fetch budgets up to (optional). Please use [BudgetPeriod.DATE_FORMAT_PATTERN] for the format pattern.
     *
     * @return LiveData object of Resource<List<BudgetPeriod>> which can be observed using an Observer for future changes as well.
     */
    fun fetchBudgetPeriods(
        budgetId: Long? = null,
        trackingStatus: BudgetTrackingStatus? = null,
        fromDate: String? = null,
        toDate: String? = null
    ): LiveData<Resource<List<BudgetPeriod>>> =
        Transformations.map(db.budgetPeriods().loadByQuery(sqlForBudgetPeriods(budgetId, trackingStatus, fromDate, toDate))) { models ->
            Resource.success(models)
        }

    /**
     * Advanced method to fetch budget periods by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches budget periods from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL query
     *
     * @return LiveData object of Resource<List<BudgetPeriod>> which can be observed using an Observer for future changes as well.
     */
    fun fetchBudgetPeriods(query: SimpleSQLiteQuery): LiveData<Resource<List<BudgetPeriod>>> =
        Transformations.map(db.budgetPeriods().loadByQuery(query)) { models ->
            Resource.success(models)
        }

    /**
     * Advanced method to fetch budget periods from cache by SQL query from the cache with associated data
     *
     * @param query SimpleSQLiteQuery: Select query which fetches budget periods from the cache
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL query
     *
     * @return LiveData object of Resource<List<BudgetPeriodRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchBudgetPeriodsWithRelation(query: SimpleSQLiteQuery): LiveData<Resource<List<BudgetPeriodRelation>>> =
        Transformations.map(db.budgetPeriods().loadByQueryWithRelation(query)) { models ->
            Resource.success(models)
        }

    /**
     * Fetch budget periods from cache with associated data
     *
     * @param budgetId id of the budget by which you want to fetch budget periods (Optional)
     * @param trackingStatus Filter by the tracking status (optional)
     * @param fromDate Start date (inclusive) to fetch budgets from (optional). Please use [BudgetPeriod.DATE_FORMAT_PATTERN] for the format pattern.
     * @param toDate End date (inclusive) to fetch budgets up to (optional). Please use [BudgetPeriod.DATE_FORMAT_PATTERN] for the format pattern.
     *
     * @return LiveData object of Resource<List<BudgetPeriodRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchBudgetPeriodsWithRelation(
        budgetId: Long? = null,
        trackingStatus: BudgetTrackingStatus? = null,
        fromDate: String? = null,
        toDate: String? = null
    ): LiveData<Resource<List<BudgetPeriodRelation>>> =
        Transformations.map(db.budgetPeriods().loadByQueryWithRelation(sqlForBudgetPeriods(budgetId, trackingStatus, fromDate, toDate))) { models ->
            Resource.success(models)
        }

    /**
     * Fetch budget periods from the cache with associated data
     *
     * @param budgetPeriodId id of the budget by which you want to fetch budget periods
     *
     * @return LiveData object of Resource<List<BudgetPeriodRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchBudgetPeriodsWithRelation(budgetPeriodId: Long): LiveData<Resource<BudgetPeriodRelation>> =
        Transformations.map(db.budgetPeriods().loadWithRelation(budgetPeriodId)) { models ->
            Resource.success(models)
        }

    /**
     * Refresh a budget period
     *
     * @param budgetId ID of the budget period to refresh
     * @param periodId ID of the budget period to refresh
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshBudgetPeriod(budgetId: Long, periodId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        budgetsAPI.fetchBudgetPeriod(budgetId, periodId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshBudgetPeriod", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleBudgetPeriodResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Refresh a budget periods by budget id from the host
     *
     * @param budgetId ID of the budget to fetch
     * @param fromDate Start date to refresh budget periods from (inclusive). Please use [BudgetPeriod.DATE_FORMAT_PATTERN] for the format pattern.
     * @param toDate End date to refresh budget periods up to (inclusive). Please use [BudgetPeriod.DATE_FORMAT_PATTERN] for the format pattern.
     * @param completion Optional completion handler with optional error if the request fails (Optional)
     */
    fun refreshBudgetPeriods(budgetId: Long, fromDate: String? = null, toDate: String? = null, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        budgetsAPI.fetchBudgetPeriods(budgetId, fromDate, toDate).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshBudgetPeriods", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleBudgetPeriodsResponse(resource.data, budgetId, fromDate, toDate, completion)
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

    private fun handleBudgetPeriodsResponse(response: List<BudgetPeriodResponse>?, budgetId: Long, fromDate: String? = null, toDate: String? = null, completion: ((Result) -> Unit)?) {
        response?.let {
            doAsync {

                val models = response.map { it.toBudgetPeriod() }
                db.budgetPeriods().insertAll(*models.toTypedArray())
                val apiIds = models.map { it.budgetPeriodId }.toHashSet()
                val allPeriodIds = db.budgetPeriods().getIds(sqlForBudgetPeriodIds(budgetId, fromDate, toDate)).toHashSet()
                val staleIds = allPeriodIds.minus(apiIds)

                if (staleIds.isNotEmpty()) {
                    removeCachedBudgetPeriods(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleBudgetPeriodResponse(response: BudgetPeriodResponse?, completion: ((Result) -> Unit)?) {
        response?.let {
            doAsync {
                db.budgetPeriods().insert(it.toBudgetPeriod())
                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }
}
