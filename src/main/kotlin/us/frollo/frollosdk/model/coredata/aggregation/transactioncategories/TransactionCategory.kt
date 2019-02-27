package us.frollo.frollosdk.model.coredata.aggregation.transactioncategories

import androidx.room.*
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.
@Entity(tableName = "transaction_category",
        indices = [Index("transaction_category_id")])
data class TransactionCategory(
        @PrimaryKey
        @ColumnInfo(name = "transaction_category_id") val transactionCategoryId: Long,
        @ColumnInfo(name = "user_defined") val userDefined: Boolean,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "category_type") val categoryType: TransactionCategoryType,
        @ColumnInfo(name = "default_budget_category") val defaultBudgetCategory: BudgetCategory,
        @ColumnInfo(name = "icon_url") val iconUrl: String,
        @ColumnInfo(name = "placement") val placement: Int
): IAdapterModel