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
import retrofit2.http.QueryMap
import us.frollo.frollosdk.model.api.reports.AccountBalanceReportResponse
import us.frollo.frollosdk.model.api.reports.TransactionCurrentReportResponse
import us.frollo.frollosdk.model.api.reports.TransactionHistoryReportResponse

internal interface ReportsAPI {
    companion object {
        const val URL_REPORT_ACCOUNT_BALANCE = "reports/accounts/history/balances"
        const val URL_REPORT_TRANSACTIONS_CURRENT = "reports/transactions/current"
        const val URL_REPORT_TRANSACTIONS_HISTORY = "reports/transactions/history"
    }

    @GET(URL_REPORT_ACCOUNT_BALANCE)
    fun fetchAccountBalanceReports(@QueryMap options: Map<String, String>): Call<AccountBalanceReportResponse>

    @GET(URL_REPORT_TRANSACTIONS_CURRENT)
    fun fetchTransactionCurrentReports(@QueryMap options: Map<String, String>): Call<TransactionCurrentReportResponse>

    @GET(URL_REPORT_TRANSACTIONS_HISTORY)
    fun fetchTransactionHistoryReports(@QueryMap options: Map<String, String>): Call<TransactionHistoryReportResponse>
}