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

import androidx.sqlite.db.SimpleSQLiteQuery
import io.reactivex.Observable
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.extensions.sqlForBudgetPeriods
import us.frollo.frollosdk.extensions.sqlForBudgets
import us.frollo.frollosdk.model.coredata.budgets.Budget
import us.frollo.frollosdk.model.coredata.budgets.BudgetFrequency
import us.frollo.frollosdk.model.coredata.budgets.BudgetPeriod
import us.frollo.frollosdk.model.coredata.budgets.BudgetPeriodRelation
import us.frollo.frollosdk.model.coredata.budgets.BudgetRelation
import us.frollo.frollosdk.model.coredata.budgets.BudgetStatus
import us.frollo.frollosdk.model.coredata.budgets.BudgetTrackingStatus
import us.frollo.frollosdk.model.coredata.budgets.BudgetType
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory

/**
 * Fetch budget by ID from the cache
 *
 * @param budgetId Unique budget ID to fetch
 *
 * @return Rx Observable object of Budget which can be observed using an Observer for future changes as well.
 */
fun Budgets.fetchBudgetRx(budgetId: Long): Observable<Budget?> {
    return db.budgets().loadRx(budgetId)
}

/**
 * Fetch budget by ID from the cache along with other associated data.
 *
 * @param budgetId Unique budget ID to fetch
 *
 * @return Rx Observable object of BudgetRelation which can be observed using an Observer for future changes as well.
 */
fun Budgets.fetchBudgetWithRelationRx(budgetId: Long): Observable<BudgetRelation?> {
    return db.budgets().loadWithRelationRx(budgetId)
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
 * @return Rx Observable object of List<Budget> which can be observed using an Observer for future changes as well.
 */
fun Budgets.fetchMerchantBudgetsRx(
    merchantId: Long? = null,
    current: Boolean? = null,
    frequency: BudgetFrequency? = null,
    status: BudgetStatus? = null,
    trackingStatus: BudgetTrackingStatus? = null
): Observable<List<Budget>> {
    return fetchBudgetsRx(current, frequency, status, trackingStatus, BudgetType.MERCHANT, merchantId?.toString())
}

/**
 * Fetch budgets from the cache by Budget Category
 *
 * @param budgetCategory Filter budgets by specific budget category
 * @param current Filter budgets by currently active budgets (Optional)
 * @param frequency Filter budgets by budget frequency (Optional)
 * @param status Filter budgets by budget status (Optional)
 * @param trackingStatus Filter budgets by tracking status (Optional)
 *
 * @return Rx Observable object of List<Budget> which can be observed using an Observer for future changes as well.
 */
fun Budgets.fetchBudgetCategoryBudgetsRx(
    budgetCategory: BudgetCategory? = null,
    current: Boolean? = null,
    frequency: BudgetFrequency? = null,
    status: BudgetStatus? = null,
    trackingStatus: BudgetTrackingStatus? = null
): Observable<List<Budget>> {
    return fetchBudgetsRx(current, frequency, status, trackingStatus, BudgetType.BUDGET_CATEGORY, budgetCategory?.toString())
}

/**
 * Fetch budgets from the cache by Category
 *
 * @param categoryId Filter budgets by specific transaction category ID
 * @param current Filter budgets by currently active budgets (Optional)
 * @param frequency Filter budgets by budget frequency (Optional)
 * @param status Filter budgets by budget status (Optional)
 * @param trackingStatus Filter budgets by tracking status (Optional)
 *
 * @return Rx Observable object of List<Budget> which can be observed using an Observer for future changes as well.
 */
fun Budgets.fetchTransactionCategoryBudgetsRx(
    categoryId: Long? = null,
    current: Boolean? = null,
    frequency: BudgetFrequency? = null,
    status: BudgetStatus? = null,
    trackingStatus: BudgetTrackingStatus? = null
): Observable<List<Budget>> {
    return fetchBudgetsRx(current, frequency, status, trackingStatus, BudgetType.TRANSACTION_CATEGORY, categoryId?.toString())
}

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
 * @return Rx Observable object of List<Budget> which can be observed using an Observer for future changes as well.
 */
fun Budgets.fetchBudgetsRx(
    current: Boolean? = null,
    frequency: BudgetFrequency? = null,
    status: BudgetStatus? = null,
    trackingStatus: BudgetTrackingStatus? = null,
    type: BudgetType? = null,
    typeValue: String? = null
): Observable<List<Budget>> {
    return db.budgets().loadByQueryRx(sqlForBudgets(current, frequency, status, trackingStatus, type, typeValue))
}

/**
 * Advanced method to fetch budgets by SQL query from the cache
 *
 * @param query SimpleSQLiteQuery: Select query which fetches goals from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<Budget> which can be observed using an Observer for future changes as well.
 */
fun Budgets.fetchBudgetsRx(query: SimpleSQLiteQuery): Observable<List<Budget>> {
    return db.budgets().loadByQueryRx(query)
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
 * @return Rx Observable object of List<Budget> which can be observed using an Observer for future changes as well.
 */
private fun Budgets.fetchMerchantBudgetsWithRelationRx(
    merchantId: Long,
    current: Boolean? = null,
    frequency: BudgetFrequency? = null,
    status: BudgetStatus? = null,
    trackingStatus: BudgetTrackingStatus? = null
): Observable<List<BudgetRelation>> {
    return fetchBudgetsWithRelationRx(current, frequency, status, trackingStatus, BudgetType.MERCHANT, merchantId.toString())
}

/**
 * Fetch budgets with relation from the cache by Budget Category
 *
 * @param budgetCategory Filter budgets by specific budget category
 * @param current Filter budgets by currently active budgets (Optional)
 * @param frequency Filter budgets by budget frequency (Optional)
 * @param status Filter budgets by budget status (Optional)
 * @param trackingStatus Filter budgets by tracking status (Optional)
 *
 * @return Rx Observable object of List<Budget> which can be observed using an Observer for future changes as well.
 */
private fun Budgets.fetchBudgetCategoryBudgetsWithRelationRx(
    current: Boolean? = null,
    frequency: BudgetFrequency? = null,
    status: BudgetStatus? = null,
    trackingStatus: BudgetTrackingStatus? = null,
    budgetCategory: BudgetCategory
): Observable<List<BudgetRelation>> {
    return fetchBudgetsWithRelationRx(current, frequency, status, trackingStatus, BudgetType.BUDGET_CATEGORY, budgetCategory.toString())
}

/**
 * Fetch budgets with relation from the cache by transaction category
 *
 * @param categoryId Filter budgets by specific transaction category ID
 * @param current Filter budgets by currently active budgets (Optional)
 * @param frequency Filter budgets by budget frequency (Optional)
 * @param status Filter budgets by budget status (Optional)
 * @param trackingStatus Filter budgets by tracking status (Optional)
 *
 * @return Rx Observable object of List<Budget> which can be observed using an Observer for future changes as well.
 */
private fun Budgets.fetchCategoryBudgetsWithRelationRx(
    categoryId: Long,
    current: Boolean? = null,
    frequency: BudgetFrequency? = null,
    status: BudgetStatus? = null,
    trackingStatus: BudgetTrackingStatus? = null
): Observable<List<BudgetRelation>> {
    return fetchBudgetsWithRelationRx(current, frequency, status, trackingStatus, BudgetType.TRANSACTION_CATEGORY, categoryId.toString())
}

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
 * @return Rx Observable object of List<BudgetRelation> which can be observed using an Observer for future changes as well.
 */
fun Budgets.fetchBudgetsWithRelationRx(
    current: Boolean? = null,
    frequency: BudgetFrequency? = null,
    status: BudgetStatus? = null,
    trackingStatus: BudgetTrackingStatus? = null,
    type: BudgetType? = null,
    typeValue: String? = null
): Observable<List<BudgetRelation>> {
    return db.budgets().loadByQueryWithRelationRx(
        sqlForBudgets(
            current = current,
            budgetFrequency = frequency,
            budgetStatus = status,
            budgetTrackingStatus = trackingStatus,
            budgetType = type,
            budgetTypeValue = typeValue
        )
    )
}

/**
 * Advanced method to fetch budgets by SQL query from the cache with associated data
 *
 * @param query SimpleSQLiteQuery: Select query which fetches goals from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<BudgetRelation> which can be observed using an Observer for future changes as well.
 */
fun Budgets.fetchBudgetWithRelationRx(query: SimpleSQLiteQuery): Observable<List<BudgetRelation>> {
    return db.budgets().loadByQueryWithRelationRx(query)
}

// Budget Periods

/**
 * Fetch budget period by ID from the cache
 *
 * @param budgetPeriodId Unique budget period ID to fetch
 *
 * @return Rx Observable object of BudgetPeriod which can be observed using an Observer for future changes as well.
 */
fun Budgets.fetchBudgetPeriodRx(budgetPeriodId: Long): Observable<BudgetPeriod?> {
    return db.budgetPeriods().loadRx(budgetPeriodId)
}

/**
 * Fetch budget periods from the cache
 *
 * @param budgetId Budget ID of the budget periods to fetch (optional)
 * @param trackingStatus Filter by the tracking status (optional)
 * @param fromDate Start date (inclusive) to fetch budgets from (optional). Please use [BudgetPeriod.DATE_FORMAT_PATTERN] for the format pattern.
 * @param toDate End date (inclusive) to fetch budgets up to (optional). Please use [BudgetPeriod.DATE_FORMAT_PATTERN] for the format pattern.
 * @param budgetStatus Filter by  status of the Budget (optional)
 *
 * @return Rx Observable object of List<BudgetPeriod> which can be observed using an Observer for future changes as well.
 */
fun Budgets.fetchBudgetPeriodsRx(
    budgetId: Long? = null,
    trackingStatus: BudgetTrackingStatus? = null,
    fromDate: String? = null,
    toDate: String? = null,
    budgetStatus: BudgetStatus? = null
): Observable<List<BudgetPeriod>> {
    return db.budgetPeriods().loadByQueryRx(
        sqlForBudgetPeriods(
            budgetId = budgetId,
            trackingStatus = trackingStatus,
            fromDate = fromDate,
            toDate = toDate,
            budgetStatus = budgetStatus
        )
    )
}

/**
 * Advanced method to fetch budget periods by SQL query from the cache
 *
 * @param query SimpleSQLiteQuery: Select query which fetches budget periods from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL query
 *
 * @return Rx Observable object of List<BudgetPeriod> which can be observed using an Observer for future changes as well.
 */
fun Budgets.fetchBudgetPeriodsRx(query: SimpleSQLiteQuery): Observable<List<BudgetPeriod>> {
    return db.budgetPeriods().loadByQueryRx(query)
}

/**
 * Advanced method to fetch budget periods from cache by SQL query from the cache with associated data
 *
 * @param query SimpleSQLiteQuery: Select query which fetches budget periods from the cache
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL query
 *
 * @return Rx Observable object of List<BudgetPeriodRelation> which can be observed using an Observer for future changes as well.
 */
fun Budgets.fetchBudgetPeriodsWithRelationRx(query: SimpleSQLiteQuery): Observable<List<BudgetPeriodRelation>> {
    return db.budgetPeriods().loadByQueryWithRelationRx(query)
}

/**
 * Fetch budget periods from cache with associated data
 *
 * @param budgetId id of the budget by which you want to fetch budget periods (Optional)
 * @param trackingStatus Filter by the tracking status (optional)
 * @param fromDate Start date (inclusive) to fetch budgets from (optional). Please use [BudgetPeriod.DATE_FORMAT_PATTERN] for the format pattern.
 * @param toDate End date (inclusive) to fetch budgets up to (optional). Please use [BudgetPeriod.DATE_FORMAT_PATTERN] for the format pattern.
 * @param budgetStatus Filter by  status of the Budget (optional)
 *
 * @return Rx Observable object of List<BudgetPeriodRelation> which can be observed using an Observer for future changes as well.
 */
fun Budgets.fetchBudgetPeriodsWithRelationRx(
    budgetId: Long? = null,
    trackingStatus: BudgetTrackingStatus? = null,
    fromDate: String? = null,
    toDate: String? = null,
    budgetStatus: BudgetStatus? = null
): Observable<List<BudgetPeriodRelation>> {
    return db.budgetPeriods().loadByQueryWithRelationRx(
        sqlForBudgetPeriods(
            budgetId = budgetId,
            trackingStatus = trackingStatus,
            fromDate = fromDate,
            toDate = toDate,
            budgetStatus = budgetStatus
        )
    )
}

/**
 * Fetch budget periods from the cache with associated data
 *
 * @param budgetPeriodId id of the budget by which you want to fetch budget periods
 *
 * @return Rx Observable object of List<BudgetPeriodRelation> which can be observed using an Observer for future changes as well.
 */
fun Budgets.fetchBudgetPeriodsWithRelationRx(budgetPeriodId: Long): Observable<BudgetPeriodRelation?> {
    return db.budgetPeriods().loadWithRelationRx(budgetPeriodId)
}
