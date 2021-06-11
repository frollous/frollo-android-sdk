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

/**
 * VerifyBSBResponse
 *
 * Represents the response of verify BSB
 */
data class VerifyBSBResponse(

    /** BSB Number */
    @SerializedName("bsb") val bsb: String?,

    /** Institution Mnemonic */
    @SerializedName("institution_mnemonic") val institutionMnemonic: String?,

    /** Name of the bank */
    @SerializedName("name") val name: String?,

    /** Address of the bank */
    @SerializedName("street_address") val streetAddress: String?,

    /** Suburb of the bank */
    @SerializedName("suburb") val suburb: String?,

    /** State of the bank */
    @SerializedName("state") val state: String?,

    /** Postcode of the bank */
    @SerializedName("postcode") val postcode: String?,

    /** Payment Flags */
    @SerializedName("payments_flags") val paymentsFlags: String?,

    /** Indicates if NPP is allowed for Payment */
    @SerializedName("is_npp_allowed") val isNPPAllowed: Boolean?
)
