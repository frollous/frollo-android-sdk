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

@Entity(tableName = "bill",
        indices = [Index("bill_id"),
            Index("merchant_id"),
            Index("category_id"),
            Index("account_id")])

data class Bill(
        @PrimaryKey
        @ColumnInfo(name = "bill_id") val billId: Long,
        @ColumnInfo(name = "name") var name: String?,
        @ColumnInfo(name = "description") val description: String?,
        @ColumnInfo(name = "bill_type") var billType: BillType,
        @ColumnInfo(name = "status") var status: BillStatus,
        @ColumnInfo(name = "last_amount") val lastAmount: BigDecimal?,
        @ColumnInfo(name = "due_amount") var dueAmount: BigDecimal,
        @ColumnInfo(name = "average_amount") val averageAmount: BigDecimal,
        @ColumnInfo(name = "frequency") var frequency: BillFrequency,
        @ColumnInfo(name = "payment_status") val paymentStatus: BillPaymentStatus,
        @ColumnInfo(name = "last_payment_date") val lastPaymentDate: String?, // yyyy-MM-dd
        @ColumnInfo(name = "next_payment_date") var nextPaymentDate: String, // yyyy-MM-dd
        @ColumnInfo(name = "category_id") val categoryId: Long?,
        @ColumnInfo(name = "merchant_id") val merchantId: Long?,
        @ColumnInfo(name = "account_id") val accountId: Long?,
        @ColumnInfo(name = "note") var notes: String?
): IAdapterModel {

    companion object {
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    }
}