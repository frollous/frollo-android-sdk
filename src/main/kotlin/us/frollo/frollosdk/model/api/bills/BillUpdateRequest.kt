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

package us.frollo.frollosdk.model.api.bills

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.bills.BillFrequency
import us.frollo.frollosdk.model.coredata.bills.BillStatus
import us.frollo.frollosdk.model.coredata.bills.BillType
import java.math.BigDecimal

internal data class BillUpdateRequest(
        @SerializedName("name") val name: String? = null,
        @SerializedName("bill_type") val billType: BillType? = null,
        @SerializedName("status") val status: BillStatus? = null,
        @SerializedName("frequency") val frequency: BillFrequency? = null,
        @SerializedName("next_payment_date") val nextPaymentDate: String? = null, // yyyy-MM-dd
        @SerializedName("due_amount") val dueAmount: BigDecimal? = null,
        @SerializedName("note") val notes: String? = null
)