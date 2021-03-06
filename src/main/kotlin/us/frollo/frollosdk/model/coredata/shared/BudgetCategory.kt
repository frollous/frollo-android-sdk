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

package us.frollo.frollosdk.model.coredata.shared

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/**
 * Indicates category of the budget
 *
 * @param budgetCategoryId Unique ID for the budget category in relation to that on the host
 */
enum class BudgetCategory(val budgetCategoryId: Long) {

    /** Income budget */
    @SerializedName("income") INCOME(0),

    /** Living budget */
    @SerializedName("living") LIVING(1),

    /** Lifestyle budget */
    @SerializedName("lifestyle") LIFESTYLE(2),

    /** Savings budget */
    @SerializedName("goals") SAVINGS(3),

    /** One offs budget */
    @SerializedName("one_off") ONE_OFF(4);

    /** Enum to serialized string */
    // This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
        // Try to get the annotation value if available instead of using plain .toString()
        // Fallback to super.toString() in case annotation is not present/available
        serializedName() ?: super.toString()

    companion object {
        /**
         * Get instance of BudgetCategory from budget category ID
         *
         * @param budgetCategoryId Unique ID for the budget category in relation to that on the host
         * @return Returns BudgetCategory or null if no match
         */
        fun getById(budgetCategoryId: Long): BudgetCategory? =
            values().find { it.budgetCategoryId == budgetCategoryId }
    }
}
