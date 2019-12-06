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
import us.frollo.frollosdk.model.coredata.budgets.BudgetType

internal data class BudgetCreateRequest(
    @SerializedName("frequency") val budgetFrequency: BudgetFrequency,
    @SerializedName("period_amount") val periodAmount: Long,
    @SerializedName("type") val type: BudgetType,
    @SerializedName("type_value") val typedValue: String,
    @SerializedName("start_date") val startDate: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("metadata") val metadata: JsonObject?
)