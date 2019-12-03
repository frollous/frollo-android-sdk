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

@Entity(tableName = "budget",
        indices = [
            Index("id")])

/** Data representation of a Goal */
data class Budget(

    /** Unique ID for budget */
    @PrimaryKey
    @ColumnInfo(name = "id")val id: Long,
    @ColumnInfo(name = "is_current")val isCurrent: Boolean,
    @ColumnInfo(name = "type")val type: String,
    @ColumnInfo(name = "type_value")val typeValue: String,
    @ColumnInfo(name = "user_id")val userId: Long,
    @ColumnInfo(name = "tracking_status")val trackingStatus: BudgetTrackingStatus,
    @ColumnInfo(name = "status")val status: BudgetStatus,
    @ColumnInfo(name = "frequency")val frequency: BudgetFrequency,
    @ColumnInfo(name = "currency")val currency: String,
    @ColumnInfo(name = "current_amount")val currentAmount: BigDecimal,
    @ColumnInfo(name = "period_amount")val periodAmount: BigDecimal,
    @ColumnInfo(name = "target_amount")val targetAmount: BigDecimal,
    @ColumnInfo(name = "estimated_target_amount")val estimatedTargetAmount: BigDecimal,
    @ColumnInfo(name = "start_date")val startDate: String, // yyyy-MM-dd
    @ColumnInfo(name = "periods_count")val periodsCount: Long,
    @ColumnInfo(name = "metadata")val metadata: JsonObject?,
    @Embedded(prefix = "budget_period_") val currentPeriod: BudgetPeriod?

) : IAdapterModel {

    companion object {

        /** Date format for dates associated with Goal */
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    }
}