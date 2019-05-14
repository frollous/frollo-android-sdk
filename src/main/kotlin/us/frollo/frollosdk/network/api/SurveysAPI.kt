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

package us.frollo.frollosdk.network.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap
import us.frollo.frollosdk.model.coredata.surveys.Survey

internal interface SurveysAPI {
    companion object {
        const val URL_SURVEYS = "user/surveys"
        const val URL_SURVEY = "user/surveys/{survey_key}"
    }

    @GET(URL_SURVEY)
    fun fetchSurvey(@Path("survey_key") surveyKey: String, @QueryMap queryParams: Map<String, String>): Call<Survey>

    @POST(URL_SURVEYS)
    fun submitSurvey(@Body request: Survey): Call<Survey>
}