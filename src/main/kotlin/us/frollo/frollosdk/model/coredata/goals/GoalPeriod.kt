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

package us.frollo.frollosdk.model.coredata.goals

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import us.frollo.frollosdk.model.IAdapterModel
import java.math.BigDecimal

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(tableName = "goal_period",
        indices = [
            Index("goal_period_id"),
            Index("goal_id")])

/** Data representation of a Goal Period */
data class GoalPeriod(

    /** Unique ID of the goal period */
    @PrimaryKey
    @ColumnInfo(name = "goal_period_id") val goalPeriodId: Long,

    /** Unique ID of the parent goal */
    @ColumnInfo(name = "goal_id") val goalId: Long,

    /** Date the goal period starts. See [GoalPeriod.DATE_FORMAT_PATTERN] for the date format pattern */
    @ColumnInfo(name = "start_date") val startDate: String, // yyyy-MM-dd

    /** End date the goal period. See [GoalPeriod.DATE_FORMAT_PATTERN] for the date format pattern */
    @ColumnInfo(name = "end_date") val endDate: String, // yyyy-MM-dd

    /** Tracking Status */
    @ColumnInfo(name = "tracking_status") val trackingStatus: GoalTrackingStatus,

    /** Current amount progressed against the goal period. Depending on `trackingType` of the goal this will include credits and/or debits towards the goal */
    @ColumnInfo(name = "current_amount") val currentAmount: BigDecimal,

    /** Target amount to reach for the goal period */
    @ColumnInfo(name = "target_amount") val targetAmount: BigDecimal,

    /** Required amount for the goal period to get back or stay on track with the goal */
    @ColumnInfo(name = "required_amount") val requiredAmount: BigDecimal

) : IAdapterModel {

    companion object {

        /** Date format for dates associated with Goal Period */
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    }
}