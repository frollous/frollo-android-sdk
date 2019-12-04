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

import androidx.annotation.Nullable
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

@Entity(tableName = "budget",
        indices = [
            Index("budget_id")])

/** Data representation of a Budget */
data class Budget(
    /** Unique ID for budget */
    @PrimaryKey
    @ColumnInfo(name = "budget_id") val id: Long,

    /** Boolean field that says if this budget is current */
    @ColumnInfo(name = "is_current") val isCurrent: Boolean,

    /** Image url for the Budget */
    @ColumnInfo(name = "image_url") val imageUrl: String?,

    /** enum specifying if the user is on track with the budget. refer [BudgetTrackingStatus] */
    @ColumnInfo(name = "tracking_status")val trackingStatus: BudgetTrackingStatus,

    /** enum specifying the status of the budget. refer [BudgetStatus] */
    @ColumnInfo(name = "status") val status: BudgetStatus,

    /** enum specifying the period for which the budget i set. refer [BudgetFrequency] */
    @ColumnInfo(name = "frequency") val frequency: BudgetFrequency,

    /** unique id of the user */
    @ColumnInfo(name = "user_id") val userId: Long,



        /** Currency ISO code of the Budget */
    @ColumnInfo(name = "currency") val currency: String,

        /** Amount spent currently to the Budget */
    @ColumnInfo(name = "current_amount") val currentAmount: BigDecimal,

        /** Amount allocated the Budget period */
    @ColumnInfo(name = "period_amount") val periodAmount: BigDecimal,

        /** Start date of the Budget */
    @ColumnInfo(name = "start_date") val startDate: String, // yyyy-MM-dd

    /** Budget type - budget_category, category, merchant */
    @ColumnInfo(name = "type") val type: String,

    /** Budget type value - living, categoryId, merchantId */
    @ColumnInfo(name = "type_value") val typeValue: String,

        /** 'n'th time the budget is being repeated */
    @ColumnInfo(name = "periods_count") val periodsCount: Long,

        /** Metadata - custom JSON to be stored with the goal (Optional) */
    @ColumnInfo(name = "metadata") val metadata: JsonObject?,

        /** Current active budget period  */
    @Embedded(prefix = "c_period_") val currentPeriod: BudgetPeriod?
) : IAdapterModel {
    companion object {
        /** Date format for dates associated with Budget */
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    }
}