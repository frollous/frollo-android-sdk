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
import retrofit2.http.*
import us.frollo.frollosdk.model.api.reports.TransactionCurrentReportResponse

internal interface ReportsAPI {
    companion object {
        const val URL_REPORT_TRANSACTIONS_CURRENT = "reports/transactions/current"
    }

    @GET(URL_REPORT_TRANSACTIONS_CURRENT)
    fun fetchTransactionCurrentReports(@QueryMap options: Map<String, String>): Call<TransactionCurrentReportResponse>
}