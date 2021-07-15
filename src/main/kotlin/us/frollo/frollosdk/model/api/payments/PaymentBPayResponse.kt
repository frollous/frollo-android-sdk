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

package us.frollo.frollosdk.model.api.payments

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.payments.PaymentMode
import java.math.BigDecimal

/**
 * PaymentBPayResponse
 *
 * Represents the response after bpay payment is successful
 */
data class PaymentBPayResponse(

    /** Amount of the the payment */
    @SerializedName("amount") val amount: BigDecimal,

    /** Biller code */
    @SerializedName("biller_code") val billerCode: String,

    /** Biller name */
    @SerializedName("biller_name") val billerName: String,

    /** Customer Reference Number - CRN */
    @SerializedName("crn") val crn: String,

    /** Reference of the payment (Optional) */
    @SerializedName("reference") val reference: String?,

    /** Date of the payment */
    @SerializedName("payment_date") val paymentDate: String,

    /** Account ID of source account in the payment */
    @SerializedName("source_account_id") val sourceAccountId: Long,

    /** Account name of source account in the payment */
    @SerializedName("source_account_name") val sourceAccountName: String,

    /** Status of the payment */
    @SerializedName("status") val status: String,

    /** Transaction ID of the payment (Optional) */
    @SerializedName("transaction_id") val transactionId: Long?,

    /** Transaction reference of the payment */
    @SerializedName("transaction_reference") val transactionReference: String,

    /** Payment is duplicate (Optional) - returned only for NPP payment */
    @SerializedName("is_duplicate") val isDuplicate: Boolean?,

    /** Mode with which the payment was made (Optional) */
    @SerializedName("payment_type") val paymentMode: PaymentMode?

) : PaymentResponse
