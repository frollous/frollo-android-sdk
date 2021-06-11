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

package us.frollo.frollosdk.payments

import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.model.api.payments.PayAnyoneRequest
import us.frollo.frollosdk.model.api.payments.PayAnyoneResponse
import us.frollo.frollosdk.model.api.payments.PaymentBPayRequest
import us.frollo.frollosdk.model.api.payments.PaymentBPayResponse
import us.frollo.frollosdk.model.api.payments.PaymentPayIdRequest
import us.frollo.frollosdk.model.api.payments.PaymentPayIdResponse
import us.frollo.frollosdk.model.api.payments.PaymentTransferRequest
import us.frollo.frollosdk.model.api.payments.PaymentTransferResponse
import us.frollo.frollosdk.model.api.payments.VerifyBSBResponse
import us.frollo.frollosdk.model.api.payments.VerifyPayAnyoneRequest
import us.frollo.frollosdk.model.api.payments.VerifyPayAnyoneResponse
import us.frollo.frollosdk.model.api.payments.VerifyPayIdRequest
import us.frollo.frollosdk.model.api.payments.VerifyPayIdResponse
import us.frollo.frollosdk.model.coredata.contacts.PayIDType
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.PaymentsAPI
import java.math.BigDecimal

/**
 * Manages all aspects of Payments
 */
class Payments(network: NetworkService) {

    companion object {
        private const val TAG = "Payments"

        /** Date format for dates associated with Payment */
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    }

    private val paymentsAPI: PaymentsAPI = network.create(PaymentsAPI::class.java)

    // Make Payments

    /**
     * Pay Anyone
     *
     * @param accountHolder Name of the payee's bank account
     * @param accountNumber Account number of the payee
     * @param amount Amount of the payment
     * @param bsb BSB of payee's bank
     * @param description Description of the payment (Optional)
     * @param paymentDate Date of the payment (Optional). See [Payments.DATE_FORMAT_PATTERN]
     * @param reference Reference of the payment (Optional)
     * @param sourceAccountId Account ID of the payment source account
     * @param securityCode Verification Code / OTP for payment
     * @param completion Optional completion handler with optional error if the request fails else PayAnyoneResponse if success
     */
    fun payAnyone(
        accountHolder: String,
        accountNumber: String,
        amount: BigDecimal,
        bsb: String,
        description: String? = null,
        paymentDate: String? = null,
        reference: String? = null,
        sourceAccountId: Long,
        securityCode: String? = null,
        completion: OnFrolloSDKCompletionListener<Resource<PayAnyoneResponse>>
    ) {
        val request = PayAnyoneRequest(
            accountHolder = accountHolder,
            accountNumber = accountNumber,
            amount = amount,
            bsb = bsb,
            description = description,
            paymentDate = paymentDate,
            reference = reference,
            sourceAccountId = sourceAccountId
        )
        paymentsAPI.payAnyone(request, otp = securityCode).enqueue { resource ->
            if (resource.status == Resource.Status.ERROR) {
                Log.e("$TAG#payAnyone", resource.error?.localizedDescription)
            }
            completion.invoke(resource)
        }
    }

    /**
     * Payment Transfer
     *
     * @param amount Amount of the payment
     * @param description Description of the payment (Optional)
     * @param destinationAccountId Account ID of destination account of the transfer
     * @param sourceAccountId Account ID of source account of the transfer
     * @param paymentDate Date of the payment (Optional). See [Payments.DATE_FORMAT_PATTERN]
     * @param securityCode Verification Code / OTP for payment
     * @param completion Optional completion handler with optional error if the request fails else PaymentTransferResponse if success
     */
    fun transferPayment(
        amount: BigDecimal,
        description: String? = null,
        destinationAccountId: Long,
        sourceAccountId: Long,
        paymentDate: String? = null,
        securityCode: String? = null,
        completion: OnFrolloSDKCompletionListener<Resource<PaymentTransferResponse>>
    ) {
        val request = PaymentTransferRequest(
            amount = amount,
            description = description,
            destinationAccountId = destinationAccountId,
            sourceAccountId = sourceAccountId,
            paymentDate = paymentDate
        )
        paymentsAPI.transfer(request, otp = securityCode).enqueue { resource ->
            if (resource.status == Resource.Status.ERROR) {
                Log.e("$TAG#transferPayment", resource.error?.localizedDescription)
            }
            completion.invoke(resource)
        }
    }

    /**
     * BPay Payment
     *
     * @param amount Amount of the payment
     * @param billerCode Biller code
     * @param crn Customer reference Number (CRN)
     * @param sourceAccountId Account ID of source account of the payment
     * @param paymentDate Date of the payment (Optional). See [Payments.DATE_FORMAT_PATTERN]
     * @param reference Reference of the payment (Optional)
     * @param securityCode Verification Code / OTP for payment
     * @param completion Optional completion handler with optional error if the request fails else PaymentTransferResponse if success
     */
    fun bpayPayment(
        amount: BigDecimal,
        billerCode: String,
        crn: String,
        sourceAccountId: Long,
        paymentDate: String? = null,
        reference: String? = null,
        securityCode: String? = null,
        completion: OnFrolloSDKCompletionListener<Resource<PaymentBPayResponse>>
    ) {
        val request = PaymentBPayRequest(
            amount = amount,
            billerCode = billerCode,
            crn = crn,
            sourceAccountId = sourceAccountId,
            paymentDate = paymentDate,
            reference = reference
        )
        paymentsAPI.bpayPayment(request, otp = securityCode).enqueue { resource ->
            if (resource.status == Resource.Status.ERROR) {
                Log.e("$TAG#bpayPayment", resource.error?.localizedDescription)
            }
            completion.invoke(resource)
        }
    }

    /**
     * PayID Payment
     *
     * @param payId Value of the PayID
     * @param type Type of the PayID
     * @param payIDName Name of the PayID holder
     * @param amount Amount of the payment
     * @param description Description of the payment (Optional)
     * @param paymentDate Date of the payment (Optional). See [Payments.DATE_FORMAT_PATTERN]
     * @param reference Reference of the payment (Optional)
     * @param sourceAccountId Account ID of the payment source account
     * @param securityCode Verification Code / OTP for payment
     * @param completion Optional completion handler with optional error if the request fails else PayAnyoneResponse if success
     */
    fun payIdPayment(
        payId: String,
        type: PayIDType,
        payIDName: String,
        amount: BigDecimal,
        description: String? = null,
        paymentDate: String? = null,
        reference: String? = null,
        sourceAccountId: Long,
        securityCode: String? = null,
        completion: OnFrolloSDKCompletionListener<Resource<PaymentPayIdResponse>>
    ) {
        val request = PaymentPayIdRequest(
            payId = payId,
            payIdType = type,
            payIdName = payIDName,
            amount = amount,
            description = description,
            paymentDate = paymentDate,
            reference = reference,
            sourceAccountId = sourceAccountId
        )
        paymentsAPI.payIdPayment(request, otp = securityCode).enqueue { resource ->
            if (resource.status == Resource.Status.ERROR) {
                Log.e("$TAG#payAnyoneNPPPayment", resource.error?.localizedDescription)
            }
            completion.invoke(resource)
        }
    }

    /**
     * Pay Anyone NPP Payment
     *
     * @param accountHolder Name of the payee's bank account
     * @param accountNumber Account number of the payee
     * @param amount Amount of the payment
     * @param bsb BSB of payee's bank
     * @param description Description of the payment (Optional)
     * @param paymentDate Date of the payment (Optional). See [Payments.DATE_FORMAT_PATTERN]
     * @param reference Reference of the payment (Optional)
     * @param sourceAccountId Account ID of the payment source account
     * @param securityCode Verification Code / OTP for payment
     * @param completion Optional completion handler with optional error if the request fails else PayAnyoneResponse if success
     */
    fun payAnyoneNppPayment(
        accountHolder: String,
        accountNumber: String,
        amount: BigDecimal,
        bsb: String,
        description: String? = null,
        paymentDate: String? = null,
        reference: String? = null,
        sourceAccountId: Long,
        securityCode: String? = null,
        completion: OnFrolloSDKCompletionListener<Resource<PayAnyoneResponse>>
    ) {
        val request = PayAnyoneRequest(
            accountHolder = accountHolder,
            accountNumber = accountNumber,
            amount = amount,
            bsb = bsb,
            description = description,
            paymentDate = paymentDate,
            reference = reference,
            sourceAccountId = sourceAccountId
        )
        paymentsAPI.nppPayment(request, otp = securityCode).enqueue { resource ->
            if (resource.status == Resource.Status.ERROR) {
                Log.e("$TAG#payAnyoneNPPPayment", resource.error?.localizedDescription)
            }
            completion.invoke(resource)
        }
    }

    // Verify Methods

    /**
     * Verify Pay Anyone
     *
     * @param accountHolder Name of the payee's bank account
     * @param accountNumber Account number of the payee
     * @param bsb BSB of payee's bank
     * @param completion Optional completion handler with optional error if the request fails else VerifyPayAnyoneResponse if success
     */
    fun verifyPayAnyone(
        accountHolder: String,
        accountNumber: String,
        bsb: String,
        completion: OnFrolloSDKCompletionListener<Resource<VerifyPayAnyoneResponse>>
    ) {
        val request = VerifyPayAnyoneRequest(
            accountHolder = accountHolder,
            accountNumber = accountNumber,
            bsb = bsb
        )
        paymentsAPI.verifyPayAnyone(request).enqueue { resource ->
            if (resource.status == Resource.Status.ERROR) {
                Log.e("$TAG#verifyPayAnyone", resource.error?.localizedDescription)
            }
            completion.invoke(resource)
        }
    }

    /**
     * Verify PayID
     *
     * @param payId Name of the payee's bank account
     * @param type Account number of the payee
     * @param completion Optional completion handler with optional error if the request fails else VerifyPayIdResponse if success
     */
    fun verifyPayId(
        payId: String,
        type: PayIDType,
        completion: OnFrolloSDKCompletionListener<Resource<VerifyPayIdResponse>>
    ) {
        val request = VerifyPayIdRequest(payId = payId, type = type)
        paymentsAPI.verifyPayId(request).enqueue { resource ->
            if (resource.status == Resource.Status.ERROR) {
                Log.e("$TAG#verifyPayId", resource.error?.localizedDescription)
            }
            completion.invoke(resource)
        }
    }

    /**
     * Verify BSB
     *
     * @param bsb BSB number to be verified
     * @param completion Optional completion handler with optional error if the request fails else VerifyBSBResponse if success
     */
    fun verifyBSB(bsb: String, completion: OnFrolloSDKCompletionListener<Resource<VerifyBSBResponse>>) {
        paymentsAPI.verifyBSB(bsb).enqueue { resource ->
            if (resource.status == Resource.Status.ERROR) {
                Log.e("$TAG#verifyBSB", resource.error?.localizedDescription)
            }
            completion.invoke(resource)
        }
    }
}
