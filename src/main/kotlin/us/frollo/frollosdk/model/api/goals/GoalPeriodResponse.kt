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
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingStatus
import java.math.BigDecimal

data class GoalPeriodResponse(
        @SerializedName("id") val goalPeriodId: Long,
        @SerializedName("goal_id") val goalId: Long,
        @SerializedName("start_date") val startDate: String, // yyyy-MM-dd
        @SerializedName("end_date") val endDate: String, // yyyy-MM-dd
        @SerializedName("tracking_status") var trackingStatus: GoalTrackingStatus,
        @SerializedName("current_amount") val currentAmount: BigDecimal,
        @SerializedName("target_amount") val targetAmount: BigDecimal,
        @SerializedName("required_amount") val requiredAmount: BigDecimal
)