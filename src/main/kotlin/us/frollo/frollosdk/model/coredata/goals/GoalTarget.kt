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

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Target of the goal to be reached */
enum class GoalTarget {

    /** Open Ended - target is not set but a regular contribution amount and end date is */
    @SerializedName("open_ended") OPEN_ENDED,

    /** Date - target to be reached by a certain date */
    @SerializedName("date") DATE,

    /** Amount - target amount to be reached */
    @SerializedName("amount") AMOUNT,

    /** Current balance - current balance as target to track goal amount */
    @SerializedName("current_balance") CURRENT_BALANCE,

    /** Available balance - available balance as target to track goal amount */
    @SerializedName("available_balance") AVAILABLE_BALANCE;

    /** Enum to serialized string */
    // This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
        // Try to get the annotation value if available instead of using plain .toString()
        // Fallback to super.toString() in case annotation is not present/available
        serializedName() ?: super.toString()
}
