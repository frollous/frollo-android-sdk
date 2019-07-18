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

data class GoalPeriod(

    @PrimaryKey
    @ColumnInfo(name = "goal_period_id") val goalPeriodId: Long,

    @ColumnInfo(name = "goal_id") val goalId: Long,

    @ColumnInfo(name = "start_date") val startDate: String, // yyyy-MM-dd

    @ColumnInfo(name = "end_date") val endDate: String, // yyyy-MM-dd

    @ColumnInfo(name = "tracking_status") val trackingStatus: GoalTrackingStatus,

    @ColumnInfo(name = "current_amount") val currentAmount: BigDecimal,

    @ColumnInfo(name = "target_amount") val targetAmount: BigDecimal,

    @ColumnInfo(name = "required_amount") val requiredAmount: BigDecimal

) : IAdapterModel {

    companion object {
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    }
}