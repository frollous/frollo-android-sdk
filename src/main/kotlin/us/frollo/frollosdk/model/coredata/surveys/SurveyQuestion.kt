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
 * Data representation of a Survey Question
 */
data class SurveyQuestion(

    /** Unique identifier of the survey question. */
    @SerializedName("id") val id: Long,

    /** Type the survey question. Used for presentation purposes. */
    @SerializedName("type") val type: SurveyQuestionType,

    /** Title of the survey question */
    @SerializedName("title") val title: String?,

    /** Additional information for the question. */
    @SerializedName("display_text") val display_text: String?,

    /** Url of the icon associated to the question. */
    @SerializedName("icon_url") val icon_url: String?,

    /** Optional indicator indicating if this question is required to be answered by the user */
    @SerializedName("optional") val optional: Boolean?,

    /** List of answers of the survey question. */
    @SerializedName("answers") val answers: List<SurveyAnswer>
)
