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

data class Goal(

    @PrimaryKey
    @ColumnInfo(name = "goal_id") val goalId: Long,

    @ColumnInfo(name = "name") var name: String,

    @ColumnInfo(name = "description") var description: String?,

    @ColumnInfo(name = "image_url") var imageUrl: String?,

    @ColumnInfo(name = "account_id") val accountId: Long?,

    @ColumnInfo(name = "type") val type: String?,

    @ColumnInfo(name = "sub_type") val subType: String?,

    @ColumnInfo(name = "tracking_status") val trackingStatus: GoalTrackingStatus,

    @ColumnInfo(name = "tracking_type") val trackingType: GoalTrackingType,

    @ColumnInfo(name = "status") val status: GoalStatus,

    @ColumnInfo(name = "frequency") val frequency: GoalFrequency,

    @ColumnInfo(name = "target") val target: GoalTarget,

    @ColumnInfo(name = "currency") val currency: String,

    @ColumnInfo(name = "current_amount") val currentAmount: BigDecimal,

    @ColumnInfo(name = "period_amount") val periodAmount: BigDecimal,

    @ColumnInfo(name = "start_amount") val startAmount: BigDecimal,

    @ColumnInfo(name = "target_amount") val targetAmount: BigDecimal,

    @ColumnInfo(name = "start_date") val startDate: String, // yyyy-MM-dd

    @ColumnInfo(name = "end_date") val endDate: String, // yyyy-MM-dd

    @ColumnInfo(name = "estimated_end_date") val estimatedEndDate: String?, // yyyy-MM-dd

    @ColumnInfo(name = "estimated_target_amount") val estimatedTargetAmount: BigDecimal?,

    @ColumnInfo(name = "periods_count") val periodsCount: Int,

    @Embedded(prefix = "c_period_") val currentPeriod: GoalPeriod

) : IAdapterModel {

    companion object {
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    }
}