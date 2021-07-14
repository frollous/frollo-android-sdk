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
import java.math.BigDecimal

/**
 * VerifyBPayResponse
 *
 * Represents the response of verify BPay
 */
data class VerifyBPayResponse(

    /** True if the lookup is valid, false otherwise */
    @SerializedName("valid") val valid: Boolean,

    /** Biller code (Optional) */
    @SerializedName("biller_code") val billerCode: String?,

    /** Name of the biller (Optional) */
    @SerializedName("biller_name") val billerName: String?,

    /** Customer Reference Number (Optional) */
    @SerializedName("crn") val crn: String?,

    /** Minimum payment amount for this biller (Optional) */
    @SerializedName("biller_min_amount") val billerMinAmount: BigDecimal?,

    /** Maximum payment amount for this biller (Optional) */
    @SerializedName("biller_max_amount") val billerMaxAmount: BigDecimal?
)
