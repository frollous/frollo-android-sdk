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

package us.frollo.frollosdk.model.api.goals

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.goals.GoalFrequency
import us.frollo.frollosdk.model.coredata.goals.GoalStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTarget
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingType
import java.math.BigDecimal

internal data class GoalResponse(
    @SerializedName("id") val goalId: Long,
    @SerializedName("name") var name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("account_id") val accountId: Long?,
    @SerializedName("type") var type: String,
    @SerializedName("sub_type") var subType: String,
    @SerializedName("tracking_status") var trackingStatus: GoalTrackingStatus,
    @SerializedName("tracking_type") var trackingType: GoalTrackingType,
    @SerializedName("status") var status: GoalStatus,
    @SerializedName("frequency") var frequency: GoalFrequency,
    @SerializedName("target") var target: GoalTarget,
    @SerializedName("currency") var currency: String,
    @SerializedName("current_amount") val currentAmount: BigDecimal,
    @SerializedName("period_amount") val periodAmount: BigDecimal,
    @SerializedName("start_amount") val startAmount: BigDecimal,
    @SerializedName("target_amount") val targetAmount: BigDecimal,
    @SerializedName("start_date") val startDate: String, // yyyy-MM-dd
    @SerializedName("end_date") val endDate: String, // yyyy-MM-dd
    @SerializedName("estimated_end_date") val estimatedEndDate: String?, // yyyy-MM-dd
    @SerializedName("estimated_target_amount") val estimatedTargetAmount: BigDecimal?,
    @SerializedName("periods_count") val periodsCount: Int,
    @SerializedName("current_period") val currentPeriod: GoalPeriodResponse?
)