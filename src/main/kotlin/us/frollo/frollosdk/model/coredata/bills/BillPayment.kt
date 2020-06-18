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
    tableName = "bill_payment",
    indices = [
        Index("bill_payment_id"),
        Index("bill_id"),
        Index("merchant_id")
    ]
)

/** Data representation of a Bill payment */
data class BillPayment(

    /** Unique ID of the bill payment */
    @PrimaryKey
    @ColumnInfo(name = "bill_payment_id") val billPaymentId: Long,

    /** Bill ID of the parent bill */
    @ColumnInfo(name = "bill_id") val billId: Long,

    /** Name of the bill */
    @ColumnInfo(name = "name") val name: String,

    /** Merchant ID associated with the bill payment */
    @ColumnInfo(name = "merchant_id") val merchantId: Long?,

    /** Date of the bill payment. See [BillPayment.DATE_FORMAT_PATTERN] for the date format pattern */
    @ColumnInfo(name = "date") var date: String, // yyyy-MM-dd

    /** Status of the bill payment */
    @ColumnInfo(name = "payment_status") var paymentStatus: BillPaymentStatus,

    /** Frequency the bill payment occurs */
    @ColumnInfo(name = "frequency") val frequency: BillFrequency,

    /** Amount of the payment */
    @ColumnInfo(name = "amount") val amount: BigDecimal,

    /** Indicates if the bill payment can be marked as unpaid */
    @ColumnInfo(name = "unpayable") val unpayable: Boolean

) : IAdapterModel {

    companion object {

        /** Date format for dates associated with Bill Payment */
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    }
}
