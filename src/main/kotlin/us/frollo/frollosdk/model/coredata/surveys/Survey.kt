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

package us.frollo.frollosdk.model.coredata.surveys

import com.google.gson.annotations.SerializedName

/**
 * Data representation of Survey
 */
data class Survey(

        /** Unique identifier of the survey. */
        @SerializedName("id") val id: Long,

        /** Survey Key. Used for fetching the survey. */
        @SerializedName("key") val key: String,

        /** Name of the survey */
        @SerializedName("name") val name: String?,

        /** List of all questions within the survey. */
        @SerializedName("questions") val questions: List<SurveyQuestion>
)