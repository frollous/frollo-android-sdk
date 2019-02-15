package us.frollo.frollosdk.model.coredata.aggregation.transactions

import androidx.room.*
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Balance
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory

/**
 * Declaring the column info allows for the renaming of variables without implementing a
 * database migration, as the column name would not change.
 */
// Since `transaction` is a reserved keyword for SQLite, use `transaction_model` instead
@Entity(tableName = "transaction_model",
        indices = [Index("transaction_id"),
                   Index("account_id"),
                   Index("category_id"),
                   Index("merchant_id")])
data class Transaction(
        @PrimaryKey
        @ColumnInfo(name = "transaction_id") val transactionId: Long,
        @ColumnInfo(name = "base_type") val baseType: TransactionBaseType,
        @ColumnInfo(name = "status") val status: TransactionStatus,
        @ColumnInfo(name = "transaction_date") val transactionDate: String, // yyyy-MM-dd
        @ColumnInfo(name = "post_date") val postDate: String?, // yyyy-MM-dd
        @Embedded(prefix = "amount_") val amount: Balance,
        @Embedded(prefix = "description_") var description: TransactionDescription?,
        @ColumnInfo(name = "budget_category") var budgetCategory: BudgetCategory,
        @ColumnInfo(name = "included") var included: Boolean,
        @ColumnInfo(name = "memo") var memo: String?,
        @ColumnInfo(name = "account_id") val accountId: Long,
        @ColumnInfo(name = "category_id") var categoryId: Long,
        @ColumnInfo(name = "merchant_id") val merchantId: Long,
        @ColumnInfo(name = "bill_id") var billId: Long?,
        @ColumnInfo(name = "bill_payment_id") var billPaymentId: Long?
): IAdapterModel