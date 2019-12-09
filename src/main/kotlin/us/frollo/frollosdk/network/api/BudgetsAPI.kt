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

package us.frollo.frollosdk.network.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap
import us.frollo.frollosdk.model.api.budgets.BudgetCreateRequest
import us.frollo.frollosdk.model.api.budgets.BudgetPeriodResponse
import us.frollo.frollosdk.model.api.budgets.BudgetResponse
import us.frollo.frollosdk.model.api.budgets.BudgetUpdateRequest

internal interface BudgetsAPI {

    companion object {
        const val URL_BUDGETS = "budgets"
        const val URL_BUDGET = "$URL_BUDGETS/{budget_id}"

        // Budget Period URLs
        const val URL_BUDGET_PERIODS = "$URL_BUDGETS/{budget_id}/periods"
        const val URL_BUDGET_PERIOD = "$URL_BUDGETS/{budget_id}/periods/{period_id}"
    }

    // Query parameters: {current, category_type}
    @GET(URL_BUDGETS)
    fun fetchBudgets(@QueryMap queryParams: Map<String, String>): Call<List<BudgetResponse>>

    @POST(URL_BUDGETS)
    fun createBudget(budgetCreateRequest: BudgetCreateRequest): Call<BudgetResponse>

    @GET(URL_BUDGET)
    fun fetchBudget(@Path("budget_id") budgetId: Long): Call<BudgetResponse>

    @PUT(URL_BUDGET)
    fun updateBudget(@Path("budget_id") budgetId: Long, @Body request: BudgetUpdateRequest): Call<BudgetResponse>

    @DELETE(URL_BUDGET)
    fun deleteBudget(@Path("budget_id") budgetId: Long): Call<Void>

    // Budget Period API
    @GET(URL_BUDGET_PERIODS)
    fun fetchBudgetPeriods(@Path("budget_id") budgetId: Long, @QueryMap queryParams: Map<String, String>): Call<List<BudgetPeriodResponse>>

    @GET(URL_BUDGET_PERIOD)
    fun fetchBudgetPeriod(@Path("budget_id") budgetId: Long, @Path("period_id") periodId: Long): Call<BudgetPeriodResponse>
}