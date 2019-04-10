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

package us.frollo.frollosdk.model.coredata.bills

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import us.frollo.frollosdk.model.IAdapterModel
import java.math.BigDecimal

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(tableName = "bill_payment",
        indices = [Index("bill_payment_id"),
            Index("bill_id"),
            Index("merchant_id")])

data class BillPayment(
        @PrimaryKey
        @ColumnInfo(name = "bill_payment_id") val billPaymentId: Long,
        @ColumnInfo(name = "bill_id") val billId: Long,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "merchant_id") val merchantId: Long,
        @ColumnInfo(name = "date") val date: String, // yyyy-MM-dd
        @ColumnInfo(name = "payment_status") var paymentStatus: BillPaymentStatus,
        @ColumnInfo(name = "frequency") var frequency: BillFrequency,
        @ColumnInfo(name = "amount") val amount: BigDecimal,
        @ColumnInfo(name = "unpayable") val unpayable: Boolean
): IAdapterModel {

    companion object {
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    }
}