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

@Entity(
    tableName = "bill",
    indices = [
        Index("bill_id"),
        Index("merchant_id"),
        Index("category_id"),
        Index("account_id")
    ]
)

/** Data representation of a Bill */
data class Bill(

    /** Unique ID of the bill */
    @PrimaryKey
    @ColumnInfo(name = "bill_id") val billId: Long,

    /** Name of the bill (Optional) */
    @ColumnInfo(name = "name") var name: String?,

    /** Additional details about the bill (Optional) */
    @ColumnInfo(name = "description") val description: String?,

    /** Bill Type */
    @ColumnInfo(name = "bill_type") var billType: BillType,

    /** Bill Status */
    @ColumnInfo(name = "status") var status: BillStatus,

    /** Last amount due (Optional) */
    @ColumnInfo(name = "last_amount") val lastAmount: BigDecimal?,

    /** Current due amount */
    @ColumnInfo(name = "due_amount") var dueAmount: BigDecimal,

    /** Average amount due */
    @ColumnInfo(name = "average_amount") val averageAmount: BigDecimal,

    /** Frequency */
    @ColumnInfo(name = "frequency") var frequency: BillFrequency,

    /** Bill Payment Status */
    @ColumnInfo(name = "payment_status") val paymentStatus: BillPaymentStatus,

    /** Last payment date (Optional). See [Bill.DATE_FORMAT_PATTERN] for the date format pattern */
    @ColumnInfo(name = "last_payment_date") val lastPaymentDate: String?, // yyyy-MM-dd

    /** Next Payment Date. See [Bill.DATE_FORMAT_PATTERN] for the date format pattern */
    @ColumnInfo(name = "next_payment_date") var nextPaymentDate: String, // yyyy-MM-dd

    /** Transaction Category associated with the bill (Optional) */
    @ColumnInfo(name = "category_id") val categoryId: Long?,

    /** Merchant ID bill is associated with (Optional) */
    @ColumnInfo(name = "merchant_id") val merchantId: Long?,

    /** Account ID bill is associated with (Optional) */
    @ColumnInfo(name = "account_id") val accountId: Long?,

    /** User notes about the bill (Optional) */
    @ColumnInfo(name = "note") var notes: String?

) : IAdapterModel {

    companion object {

        /** Date format for dates associated with Bill */
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    }
}
