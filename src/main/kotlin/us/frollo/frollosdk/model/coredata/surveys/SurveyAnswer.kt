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
 * Data representation of a Survey Answer
 */
data class SurveyAnswer(

        /** Unique identifier of the survey answer. */
        @SerializedName("id") val id: Long,

        /** Type the survey answer. Used for presentation purposes. */
        @SerializedName("type") val type: SurveyAnswerType,

        /** Title of the survey answer */
        @SerializedName("title") val title: String?,

        /** Additional information for the answer. */
        @SerializedName("display_text") val display_text: String?,

        /** Url of the icon associated to the answer. */
        @SerializedName("icon_url") val icon_url: String?,

        /** Value of the answer */
        @SerializedName("value") var value: String,

        /** Indicates if this is the selected answer.  */
        @SerializedName("selected") var selected: Boolean
)