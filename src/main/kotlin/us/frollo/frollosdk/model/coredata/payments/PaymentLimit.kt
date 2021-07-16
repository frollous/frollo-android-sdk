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

package us.frollo.frollosdk.model.coredata.payments

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/**
 * Data representation the Payment Limit
 */
data class PaymentLimit(

    /** Type of payment limit */
    @SerializedName("type") val type: PaymentLimitType,

    /** Period of the limit */
    @SerializedName("period") val period: PaymentLimitPeriod,

    /** Limit amount for the period */
    @SerializedName("limit_amount") val limitAmount: BigDecimal,

    /** Consumed amount for the period (Optional) */
    @SerializedName("consumed_amount") val consumedAmount: BigDecimal?
)
