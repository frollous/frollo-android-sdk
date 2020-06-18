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
import us.frollo.frollosdk.model.api.bills.BillCreateRequest
import us.frollo.frollosdk.model.api.bills.BillPaymentResponse
import us.frollo.frollosdk.model.api.bills.BillPaymentUpdateRequest
import us.frollo.frollosdk.model.api.bills.BillResponse
import us.frollo.frollosdk.model.api.bills.BillUpdateRequest
import us.frollo.frollosdk.model.api.bills.BillsResponse

internal interface BillsAPI {
    companion object {
        // Bill URLs
        const val URL_BILLS = "bills"
        const val URL_BILL = "bills/{bill_id}"

        // Bill Payment URLs
        const val URL_BILL_PAYMENTS = "bills/payments"
        const val URL_BILL_PAYMENT = "bills/payments/{bill_payment_id}"
    }

    // Bill API

    @GET(URL_BILLS)
    fun fetchBills(): Call<BillsResponse>

    @GET(URL_BILL)
    fun fetchBill(@Path("bill_id") billId: Long): Call<BillResponse>

    @POST(URL_BILLS)
    fun createBill(@Body request: BillCreateRequest): Call<BillResponse>

    @PUT(URL_BILL)
    fun updateBill(@Path("bill_id") billId: Long, @Body request: BillUpdateRequest): Call<BillResponse>

    @DELETE(URL_BILL)
    fun deleteBill(@Path("bill_id") billId: Long): Call<Void>

    // Bill Payment API

    // Query parameters: {from_date, to_date, skip, count}
    @GET(URL_BILL_PAYMENTS)
    fun fetchBillPayments(@QueryMap queryParams: Map<String, String>): Call<List<BillPaymentResponse>>

    @PUT(URL_BILL_PAYMENT)
    fun updateBillPayment(@Path("bill_payment_id") billPaymentId: Long, @Body request: BillPaymentUpdateRequest): Call<BillPaymentResponse>

    @DELETE(URL_BILL_PAYMENT)
    fun deleteBillPayment(@Path("bill_payment_id") billPaymentId: Long): Call<Void>
}
