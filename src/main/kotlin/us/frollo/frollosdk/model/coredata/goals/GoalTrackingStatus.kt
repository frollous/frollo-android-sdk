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

/** How the user is tracking against an in progress goal */
enum class GoalTrackingStatus {

    /** ABOVE - user has made more contributions on goal than the target */
    @SerializedName("above") ABOVE,

    /** On Track - user has made equal contributions on goal as the target  */
    @SerializedName("equal") EQUAL,

    /** BELOW - user has made less contributions on goal than the target */
    @SerializedName("below") BELOW;

    /** Enum to serialized string */
    // This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
        // Try to get the annotation value if available instead of using plain .toString()
        // Fallback to super.toString() in case annotation is not present/available
        serializedName() ?: super.toString()
}
