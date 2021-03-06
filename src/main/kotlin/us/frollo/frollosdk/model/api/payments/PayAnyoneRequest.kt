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

internal data class PayAnyoneRequest(
    @SerializedName("bsb") val bsb: String,
    @SerializedName("account_number") val accountNumber: String,
    @SerializedName("account_holder") val accountHolder: String,
    @SerializedName("amount") val amount: BigDecimal,
    @SerializedName("source_account_id") val sourceAccountId: Long,
    @SerializedName("payment_date") val paymentDate: String? = null, // yyyy-MM-dd
    @SerializedName("description") val description: String? = null,
    @SerializedName("reference") val reference: String? = null,
    @SerializedName("override_method") val overrideMethod: String? = null
)
