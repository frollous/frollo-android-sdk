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
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import us.frollo.frollosdk.model.IAdapterModel
import java.math.BigDecimal

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(tableName = "goal",
        indices = [
            Index("goal_id"),
            Index("account_id")])

/** Data representation of a Goal */
data class Goal(

    /** Unique ID of the goal */
    @PrimaryKey
    @ColumnInfo(name = "goal_id") val goalId: Long,

    /** Name of the goal */
    @ColumnInfo(name = "name") var name: String,

    /** Description of the goal (Optional) */
    @ColumnInfo(name = "description") var description: String?,

    /** URL of the goal image */
    @ColumnInfo(name = "image_url") var imageUrl: String?,

    /** Account ID the goal is associated with if tracking automatically (Optional) */
    @ColumnInfo(name = "account_id") val accountId: Long?,

    /** Type of the goal (Optional) */
    @ColumnInfo(name = "type") val type: String?,

    /** Sub-type of the goal (Optional) */
    @ColumnInfo(name = "sub_type") val subType: String?,

    /** Tracking Status */
    @ColumnInfo(name = "tracking_status") val trackingStatus: GoalTrackingStatus,

    /** Tracking Type */
    @ColumnInfo(name = "tracking_type") val trackingType: GoalTrackingType,

    /** Goal status */
    @ColumnInfo(name = "status") val status: GoalStatus,

    /** Frequency */
    @ColumnInfo(name = "frequency") val frequency: GoalFrequency,

    /** Target */
    @ColumnInfo(name = "target") val target: GoalTarget,

    /** Currency ISO code of the goal */
    @ColumnInfo(name = "currency") val currency: String,

    /** Current amount progressed against the goal. Depending on [trackingType] this will include credits and/or debits towards the goal */
    @ColumnInfo(name = "current_amount") val currentAmount: BigDecimal,

    /** Amount to be saved each period */
    @ColumnInfo(name = "period_amount") val periodAmount: BigDecimal,

    /** Starting amount of the goal */
    @ColumnInfo(name = "start_amount") val startAmount: BigDecimal,

    /** Target amount to reach for the goal */
    @ColumnInfo(name = "target_amount") val targetAmount: BigDecimal,

    /** Date the goal starts. See [Goal.DATE_FORMAT_PATTERN] for the date format pattern */
    @ColumnInfo(name = "start_date") val startDate: String, // yyyy-MM-dd

    /** End date of the goal. See [Goal.DATE_FORMAT_PATTERN] for the date format pattern */
    @ColumnInfo(name = "end_date") val endDate: String, // yyyy-MM-dd

    /** Estimated date the goal will be completed at the current rate of progress (Optional). See [Goal.DATE_FORMAT_PATTERN] for the date format pattern */
    @ColumnInfo(name = "estimated_end_date") val estimatedEndDate: String?, // yyyy-MM-dd

    /** Estimated amount saved at the end of the goal at the current rate of progress (Optional) */
    @ColumnInfo(name = "estimated_target_amount") val estimatedTargetAmount: BigDecimal?,

    /** Amount of periods until the goal is completed */
    @ColumnInfo(name = "periods_count") val periodsCount: Int,

    /** Current active goal period */
    @Embedded(prefix = "c_period_") val currentPeriod: GoalPeriod

) : IAdapterModel {

    companion object {

        /** Date format for dates associated with Goal */
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    }
}