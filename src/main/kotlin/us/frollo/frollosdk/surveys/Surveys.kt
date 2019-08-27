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

package us.frollo.frollosdk.surveys

import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.fetchSurvey
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.model.coredata.surveys.Survey
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.SurveysAPI

/**
 * Manages fetching & submitting surveys
 */
class Surveys(network: NetworkService) {

    companion object {
        private const val TAG = "Surveys"
    }

    private val surveysAPI: SurveysAPI = network.create(SurveysAPI::class.java)

    /**
     * Fetch a specific survey by key from the host
     *
     * @param surveyKey Key of the survey to fetch
     * @param latest If true then the host will always return a blank copy of the most recent published Survey for the given key
     * @param completion Completion handler with optional error if the request fails and survey model if succeeds
     */
    fun fetchSurvey(surveyKey: String, latest: Boolean? = null, completion: OnFrolloSDKCompletionListener<Resource<Survey>>) {
        surveysAPI.fetchSurvey(surveyKey = surveyKey, latest = latest).enqueue { resource ->
            if (resource.status == Resource.Status.ERROR) {
                Log.e("$TAG#fetchSurvey", resource.error?.localizedDescription)
            }
            completion.invoke(resource)
        }
    }

    /**
     * Submit answer to a survey
     *
     * @param survey Answered survey
     * @param completion Completion handler with optional error if the request fails and survey model if succeeds (optional)
     */
    fun submitSurvey(survey: Survey, completion: OnFrolloSDKCompletionListener<Resource<Survey>>? = null) {
        surveysAPI.submitSurvey(request = survey).enqueue { resource ->
            if (resource.status == Resource.Status.ERROR) {
                Log.e("$TAG#submitSurvey", resource.error?.localizedDescription)
            }
            completion?.invoke(resource)
        }
    }
}