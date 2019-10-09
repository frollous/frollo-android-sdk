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

package us.frollo.frollosdk.model.api.aggregation.transactions

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory

internal data class TransactionUpdateRequest(
    @SerializedName("budget_category") val budgetCategory: BudgetCategory,
    @SerializedName("category_id") val categoryId: Long? = null,
    @SerializedName("included") val included: Boolean? = null,
    @SerializedName("memo") val memo: String? = null,
    @SerializedName("user_description") val userDescription: String? = null,
    @SerializedName("recategorise_all") val recategoriseAll: Boolean? = null,
    @SerializedName("budget_apply_all") val budgetCategoryApplyAll: Boolean? = null,
    @SerializedName("include_apply_all") val includeApplyAll: Boolean? = null
)