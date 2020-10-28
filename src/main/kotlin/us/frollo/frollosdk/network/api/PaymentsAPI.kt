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
import retrofit2.http.Header
import retrofit2.http.POST
import us.frollo.frollosdk.model.api.payments.PayAnyoneRequest
import us.frollo.frollosdk.model.api.payments.PayAnyoneResponse
import us.frollo.frollosdk.model.api.payments.PaymentBPayRequest
import us.frollo.frollosdk.model.api.payments.PaymentBPayResponse
import us.frollo.frollosdk.model.api.payments.PaymentTransferRequest
import us.frollo.frollosdk.model.api.payments.PaymentTransferResponse
import us.frollo.frollosdk.model.api.payments.VerifyPayAnyoneRequest
import us.frollo.frollosdk.model.api.payments.VerifyPayAnyoneResponse
import us.frollo.frollosdk.network.NetworkHelper

internal interface PaymentsAPI {
    companion object {
        // Pay Anyone URLs
        const val URL_PAY_ANYONE = "payments/payanyone"
        const val URL_VERIFY_PAY_ANYONE = "payments/verify/payanyone"

        // Transfer URLs
        const val URL_TRANSFER = "payments/transfer"

        // BPay URLs
        const val URL_BPAY = "payments/bpay"
    }

    // Make Payment APIs

    @POST(URL_PAY_ANYONE)
    fun payAnyone(@Body request: PayAnyoneRequest, @Header(NetworkHelper.HEADER_OTP) otp: String?): Call<PayAnyoneResponse>

    @POST(URL_TRANSFER)
    fun transfer(@Body request: PaymentTransferRequest, @Header(NetworkHelper.HEADER_OTP) otp: String?): Call<PaymentTransferResponse>

    @POST(URL_BPAY)
    fun bpayPayment(@Body request: PaymentBPayRequest, @Header(NetworkHelper.HEADER_OTP) otp: String?): Call<PaymentBPayResponse>

    // Verify Payment APIs

    @POST(URL_VERIFY_PAY_ANYONE)
    fun verifyPayAnyone(@Body request: VerifyPayAnyoneRequest): Call<VerifyPayAnyoneResponse>
}
