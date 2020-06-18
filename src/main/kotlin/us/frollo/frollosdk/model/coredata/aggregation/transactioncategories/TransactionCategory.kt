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

package us.frollo.frollosdk.model.coredata.aggregation.transactioncategories

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(
    tableName = "transaction_category",
    indices = [Index("transaction_category_id")]
)

/** Data representation of a Transaction Category */
data class TransactionCategory(

    /** Unique ID for the transaction category */
    @PrimaryKey @ColumnInfo(name = "transaction_category_id") val transactionCategoryId: Long,

    /** User defined category */
    @ColumnInfo(name = "user_defined") val userDefined: Boolean,

    /** Name of the transaction category */
    @ColumnInfo(name = "name") val name: String,

    /** Category */
    @ColumnInfo(name = "category_type") val categoryType: TransactionCategoryType,

    /** Default budget category the category is associated with. Transactions will default to this budget category when recategorised */
    @ColumnInfo(name = "default_budget_category") val defaultBudgetCategory: BudgetCategory,

    /** URL to an icon image for the category (optional) */
    @ColumnInfo(name = "icon_url") val iconUrl: String,

    /** Placement order of the transaction for determining most popular categories. Higher is more popular */
    @ColumnInfo(name = "placement") val placement: Int

) : IAdapterModel
