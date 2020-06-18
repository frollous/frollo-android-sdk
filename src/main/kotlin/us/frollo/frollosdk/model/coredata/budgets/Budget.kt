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

package us.frollo.frollosdk.model.coredata.budgets

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.JsonObject
import us.frollo.frollosdk.model.IAdapterModel
import java.math.BigDecimal

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(
    tableName = "budget",
    indices = [
        Index("budget_id")
    ]
)

/** Data representation of a Budget */
data class Budget(
    /** Unique ID for budget */
    @PrimaryKey
    @ColumnInfo(name = "budget_id") val budgetId: Long,

    /** Indicates if this budget is current active */
    @ColumnInfo(name = "is_current") val isCurrent: Boolean,

    /** Image URL for the Budget (Optional) */
    @ColumnInfo(name = "image_url") var imageUrl: String?,

    /** Tracking status of the user with the budget. Refer [BudgetTrackingStatus] */
    @ColumnInfo(name = "tracking_status")val trackingStatus: BudgetTrackingStatus,

    /** Status of the budget. Refer [BudgetStatus] */
    @ColumnInfo(name = "status") val status: BudgetStatus,

    /** The frequency at which you want to split up this budget. Refer [BudgetFrequency] */
    @ColumnInfo(name = "frequency") val frequency: BudgetFrequency,

    /** Unique ID of the user */
    @ColumnInfo(name = "user_id") val userId: Long,

    /** Currency ISO code of the Budget */
    @ColumnInfo(name = "currency") val currency: String,

    /** Amount spent currently to the Budget */
    @ColumnInfo(name = "current_amount") val currentAmount: BigDecimal,

    /** Amount allocated the Budget period */
    @ColumnInfo(name = "period_amount") var periodAmount: BigDecimal,

    /** Start date of the Budget */
    @ColumnInfo(name = "start_date") val startDate: String?, // yyyy-MM-dd

    /** Budget type - budget_category, category, merchant */
    @ColumnInfo(name = "type") val type: BudgetType,

    /** Budget type value - This can be budget category name (Eg: living) or categoryId or merchantId based on the Budget Type */
    @ColumnInfo(name = "type_value") val typeValue: String,

    /** The number of periods that belong to this budget. As they are created on the budget creation this number won't be changing. */
    @ColumnInfo(name = "periods_count") val periodsCount: Long,

    /** Metadata - custom JSON to be stored with the goal (Optional) */
    @ColumnInfo(name = "metadata") var metadata: JsonObject?,

    /** Current active budget period  */
    @Embedded(prefix = "c_period_") val currentPeriod: BudgetPeriod?

) : IAdapterModel {
    companion object {
        /** Date format for dates associated with Budget */
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    }
}
