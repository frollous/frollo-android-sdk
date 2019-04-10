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
import us.frollo.frollosdk.model.coredata.bills.BillPaymentStatus
import us.frollo.frollosdk.model.coredata.bills.BillStatus
import us.frollo.frollosdk.model.coredata.bills.BillType
import java.math.BigDecimal

internal data class BillResponse(
        @SerializedName("id") val billId: Long,
        @SerializedName("name") val name: String,
        @SerializedName("description") val description: String?,
        @SerializedName("bill_type") var billType: BillType,
        @SerializedName("status") var status: BillStatus,
        @SerializedName("last_amount") val lastAmount: BigDecimal,
        @SerializedName("due_amount") val dueAmount: BigDecimal,
        @SerializedName("average_amount") val averageAmount: BigDecimal,
        @SerializedName("frequency") var frequency: BillFrequency,
        @SerializedName("payment_status") var paymentStatus: BillPaymentStatus,
        @SerializedName("last_payment_date") val lastPaymentDate: String, // yyyy-MM-dd
        @SerializedName("next_payment_date") val nextPaymentDate: String, // yyyy-MM-dd
        @SerializedName("category") var category: Category?,
        @SerializedName("merchant") var merchant: Merchant?,
        @SerializedName("account_id") val accountId: Long,
        @SerializedName("note") var note: String?
) {
    internal data class Category(
            @SerializedName("id") val id: Long,
            @SerializedName("name") val name: String
    )

    internal data class Merchant(
            @SerializedName("id") val id: Long,
            @SerializedName("name") val name: String
    )
}