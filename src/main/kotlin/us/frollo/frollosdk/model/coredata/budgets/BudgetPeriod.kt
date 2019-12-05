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
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import us.frollo.frollosdk.model.IAdapterModel
import java.math.BigDecimal

@Entity(tableName = "budget_period",
        indices = [
            Index("budget_period_id"),
            Index("budget_id")])

/** Data representation of a Budget Period */
data class BudgetPeriod(

    /** Unique ID of the Budget Period */
    @PrimaryKey
    @ColumnInfo(name = "budget_period_id") val budgetPeriodId: Long,

    /** Unique ID of the parent budget */
    @ColumnInfo(name = "budget_id") val budgetId: Long,

    /** Date the Budget Period starts. See [BudgetPeriod.DATE_FORMAT_PATTERN] for the date format pattern */
    @ColumnInfo(name = "start_date") val startDate: String, // yyyy-MM-dd

    /** End date of the Budget Period. See [BudgetPeriod.DATE_FORMAT_PATTERN] for the date format pattern */
    @ColumnInfo(name = "end_date") val endDate: String, // yyyy-MM-dd

    /** Current amount progressed against the Budget Period. Depending on [Budget.type] of the budget this is the sum of all transactions for the period */
    @ColumnInfo(name = "current_amount") val currentAmount: BigDecimal,

    /** Target amount to reach for the Budget Period */
    @ColumnInfo(name = "target_amount") val targetAmount: BigDecimal,

    /** Required amount for the Budget Period to get back or stay on track with the budget */
    @ColumnInfo(name = "required_amount") val requiredAmount: BigDecimal,

    /** Tracking Status of the Budget Period */
    @ColumnInfo(name = "tracking_status") val trackingStatus: BudgetTrackingStatus,

    /** The number of periods that belong to this budget. As they are created on the budget creation this number won't be changing. */
    @ColumnInfo(name = "index") val index: Int

) : IAdapterModel {
    companion object {
        /** Date format for dates associated with budget Period */
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    }
}