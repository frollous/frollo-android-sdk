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

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.goals.GoalFrequency
import us.frollo.frollosdk.model.coredata.goals.GoalTarget
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingType
import java.math.BigDecimal

internal data class GoalCreateRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("account_id") val accountId: Long,
    @SerializedName("tracking_type") val trackingType: GoalTrackingType,
    @SerializedName("frequency") val frequency: GoalFrequency,
    @SerializedName("target") val target: GoalTarget,
    @SerializedName("target_amount") val targetAmount: BigDecimal? = null,
    @SerializedName("period_amount") val periodAmount: BigDecimal? = null,
    @SerializedName("start_amount") val startAmount: BigDecimal? = null,
    @SerializedName("start_date") val startDate: String? = null, // yyyy-MM-dd
    @SerializedName("end_date") val endDate: String? = null, // yyyy-MM-dd
    @SerializedName("metadata") val metadata: JsonObject? = null
) {
    fun valid(): Boolean {
        return when (target) {
            GoalTarget.AMOUNT -> targetAmount != null && periodAmount != null
            GoalTarget.DATE -> endDate != null && targetAmount != null
            GoalTarget.OPEN_ENDED -> periodAmount != null && endDate != null
        }
    }
}
