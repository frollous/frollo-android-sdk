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

package us.frollo.frollosdk.model.api.budgets

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.budgets.BudgetFrequency
import us.frollo.frollosdk.model.coredata.budgets.BudgetStatus
import us.frollo.frollosdk.model.coredata.budgets.BudgetTrackingStatus
import java.math.BigDecimal

internal data class BudgetsResponse(
    @SerializedName("id")val id: Long,
    @SerializedName("is_current")val isCurrent: Boolean,
    @SerializedName("type")val type: String,
    @SerializedName("type_value")val typeValue: String,
    @SerializedName("user_id")val userId: Long,
    @SerializedName("status")val status: BudgetStatus,
    @SerializedName("tracking_status")val trackingStatus: BudgetTrackingStatus,
    @SerializedName("frequency")val frequency: BudgetFrequency,
    @SerializedName("currency")val currency: String,
    @SerializedName("current_amount")val currentAmount: BigDecimal,
    @SerializedName("period_amount")val periodAmount: BigDecimal,
    @SerializedName("target_amount")val targetAmount: BigDecimal,
    @SerializedName("estimated_target_amount")val estimatedTargetAmount: BigDecimal,
    @SerializedName("start_date")val startDate: String, // yyyy-MM-dd
    @SerializedName("periods_count")val periodsCount: Long,
    @SerializedName("metadata")val metadata: JsonObject?,
    @SerializedName("current_period") val currentPeriod: BudgetPeriodResponse?
)