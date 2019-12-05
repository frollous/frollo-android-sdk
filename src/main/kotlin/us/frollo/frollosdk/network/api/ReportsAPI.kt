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
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap
import us.frollo.frollosdk.model.api.reports.AccountBalanceReportResponse
import us.frollo.frollosdk.model.api.reports.ReportsResponse

internal interface ReportsAPI {
    companion object {
        const val URL_REPORT_ACCOUNT_BALANCE = "reports/accounts/history/balances"
        const val URL_REPORTS_CATEGORIES = "reports/categories"
        const val URL_REPORTS_CATEGORY = "reports/categories/{category_id}"
        const val URL_REPORTS_MERCHANTS = "reports/merchants"
        const val URL_REPORTS_MERCHANT = "reports/merchants/{merchant_id}"
        const val URL_REPORTS_BUDGET_CATEGORIES = "reports/budget_categories"
        const val URL_REPORTS_BUDGET_CATEGORY = "reports/budget_categories/{budget_category_id}"
        const val URL_REPORTS_TAGS = "reports/tags"
        const val URL_REPORTS_TAG = "reports/tags/{tag}"
    }

    @GET(URL_REPORT_ACCOUNT_BALANCE)
    fun fetchAccountBalanceReports(@QueryMap options: Map<String, String>): Call<AccountBalanceReportResponse>

    @GET(URL_REPORTS_CATEGORIES)
    fun fetchReportsByCategory(@QueryMap options: Map<String, String>): Call<ReportsResponse>

    @GET(URL_REPORTS_CATEGORY)
    fun fetchReportsByCategory(@Path("category_id") categoryId: Long, @QueryMap options: Map<String, String>): Call<ReportsResponse>

    @GET(URL_REPORTS_MERCHANTS)
    fun fetchReportsByMerchant(@QueryMap options: Map<String, String>): Call<ReportsResponse>

    @GET(URL_REPORTS_MERCHANT)
    fun fetchReportsByMerchant(@Path("merchant_id") merchantId: Long, @QueryMap options: Map<String, String>): Call<ReportsResponse>

    @GET(URL_REPORTS_BUDGET_CATEGORIES)
    fun fetchReportsByBudgetCategory(@QueryMap options: Map<String, String>): Call<ReportsResponse>

    @GET(URL_REPORTS_BUDGET_CATEGORY)
    fun fetchReportsByBudgetCategory(@Path("budget_category_id") budgetCategoryId: Long, @QueryMap options: Map<String, String>): Call<ReportsResponse>

    @GET(URL_REPORTS_TAGS)
    fun fetchReportsByTag(@QueryMap options: Map<String, String>): Call<ReportsResponse>

    @GET(URL_REPORTS_TAG)
    fun fetchReportsByTag(@Path("tag") tag: String, @QueryMap options: Map<String, String>): Call<ReportsResponse>
}