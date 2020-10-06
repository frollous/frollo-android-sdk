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

package us.frollo.frollosdk.model.coredata.aggregation.transactions

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Balance
import us.frollo.frollosdk.model.coredata.aggregation.merchants.MerchantDetails
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

// Since `transaction` is a reserved keyword for SQLite, use `transaction_model` instead
@Entity(
    tableName = "transaction_model",
    indices = [
        Index("transaction_id"),
        Index("account_id"),
        Index("category_id"),
        Index("merchant_id")
    ]
)

/** Data representation of a Transaction */
data class Transaction(

    /** Unique ID of the transaction */
    @PrimaryKey
    @ColumnInfo(name = "transaction_id") val transactionId: Long,

    /** Transaction Base Type */
    @ColumnInfo(name = "base_type") val baseType: TransactionBaseType,

    /** Status of the transaction */
    @ColumnInfo(name = "status") val status: TransactionStatus,

    /** Date the transaction occurred, localized */
    @ColumnInfo(name = "transaction_date") val transactionDate: String, // yyyy-MM-dd

    /** Date the transaction was posted, localized (optional) */
    @ColumnInfo(name = "post_date") val postDate: String?, // yyyy-MM-dd

    /** Amount the transaction is for */
    @Embedded(prefix = "amount_") val amount: Balance,

    /** Description */
    @Embedded(prefix = "description_") var description: TransactionDescription?,

    /** Transaction's associated budget category. See [BudgetCategory] */
    @ColumnInfo(name = "budget_category") var budgetCategory: BudgetCategory,

    /** Included in budget */
    @ColumnInfo(name = "included") var included: Boolean,

    /** Memo or notes added to the transaction (optional) */
    @ColumnInfo(name = "memo") var memo: String?,

    /** Parent account ID */
    @ColumnInfo(name = "account_id") val accountId: Long,

    /** Transaction Category ID related to the transaction */
    @ColumnInfo(name = "category_id") var categoryId: Long,

    /** Merchant details related to the transaction */
    @Embedded(prefix = "merchant_") val merchant: MerchantDetails,

    /** Bill ID related to the transaction */
    @ColumnInfo(name = "bill_id") var billId: Long?,

    /** Bill Payment ID related to the transaction */
    @ColumnInfo(name = "bill_payment_id") var billPaymentId: Long?,

    /** All tags applied to this transaction */
    @ColumnInfo(name = "user_tags") var userTags: List<String>?,

    /** External ID of the aggregator */
    @ColumnInfo(name = "external_id") val externalId: String,

    /** Goal ID associated with the transaction */
    @ColumnInfo(name = "goal_id") var goalId: Long?

) : IAdapterModel {

    companion object {
        /** Date format for dates associated with Transaction */
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    }
}
