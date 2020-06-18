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

package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.coredata.surveys.Survey
import us.frollo.frollosdk.model.coredata.surveys.SurveyAnswer
import us.frollo.frollosdk.model.coredata.surveys.SurveyAnswerType
import us.frollo.frollosdk.model.coredata.surveys.SurveyQuestion
import us.frollo.frollosdk.model.coredata.surveys.SurveyQuestionType
import us.frollo.frollosdk.testutils.randomBoolean
import us.frollo.frollosdk.testutils.randomString
import us.frollo.frollosdk.testutils.randomUUID

internal fun testSurveyData(): Survey {

    val answers = listOf(
        SurveyAnswer(
            id = 2,
            title = randomUUID(),
            display_text = randomString(32),
            icon_url = null,
            value = "5",
            selected = true,
            type = SurveyAnswerType.SELECTION
        )
    )

    val questions = listOf(
        SurveyQuestion(
            id = 1,
            type = SurveyQuestionType.MULTIPLE_CHOICE,
            title = randomUUID(),
            display_text = randomString(32),
            icon_url = null,
            optional = randomBoolean(),
            answers = answers
        )
    )

    return Survey(
        id = 3,
        key = "FINANCIAL_WELLBEING",
        name = randomUUID(),
        questions = questions,
        displayText = null,
        iconUrl = null,
        metadata = null
    )
}
