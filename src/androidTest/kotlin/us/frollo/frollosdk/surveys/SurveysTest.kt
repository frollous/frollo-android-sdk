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

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Test

import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.BaseAndroidTest
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.model.coredata.surveys.SurveyAnswerType
import us.frollo.frollosdk.model.coredata.surveys.SurveyQuestionType
import us.frollo.frollosdk.model.testSurveyData
import us.frollo.frollosdk.network.api.SurveysAPI
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SurveysTest : BaseAndroidTest() {

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
    }

    @Test
    fun testFetchSurvey() {
        initSetup()

        val signal = CountDownLatch(1)

        val surveyKey = "FINANCIAL_WELLBEING"

        val body = readStringFromJson(app, R.raw.survey_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "user/surveys/$surveyKey") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        surveys.fetchSurvey(surveyKey = surveyKey) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val model = resource.data
            assertNotNull(model)
            assertEquals(surveyKey, model?.key)
            assertEquals(1, model?.questions?.size)
            assertEquals("https://upload.wikimedia.org/wikipedia/en/e/e0/WPVG_icon_2016.svg", model?.iconUrl)
            assertEquals("This is amazing", model?.displayText)
            assertEquals("John", model?.metadata?.get("name")?.asString)
            assertEquals(SurveyQuestionType.SLIDER, model?.questions?.get(0)?.type)
            assertEquals(1L, model?.questions?.get(0)?.id)
            assertEquals(1, model?.questions?.get(0)?.answers?.size)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals("user/surveys/$surveyKey", request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testFetchSurveyFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        surveys.fetchSurvey(surveyKey = "FINANCIAL_WELLBEING") { result ->
            assertEquals(Resource.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testFetchLatestSurvey() {
        initSetup()

        val signal = CountDownLatch(1)

        val surveyKey = "FINANCIAL_WELLBEING"
        val latest = true

        val body = readStringFromJson(app, R.raw.survey_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "user/surveys/$surveyKey?latest=$latest") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        surveys.fetchSurvey(surveyKey = surveyKey, latest = latest) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val model = resource.data
            assertNotNull(model)
            assertEquals(surveyKey, model?.key)
            assertEquals(1, model?.questions?.size)
            assertEquals(SurveyQuestionType.SLIDER, model?.questions?.get(0)?.type)
            assertEquals(1L, model?.questions?.get(0)?.id)
            assertEquals(1, model?.questions?.get(0)?.answers?.size)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals("user/surveys/$surveyKey?latest=$latest", request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testSubmitSurvey() {
        initSetup()

        val signal = CountDownLatch(1)

        val testSurvey = testSurveyData()

        val body = readStringFromJson(app, R.raw.survey_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == SurveysAPI.URL_SURVEYS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        surveys.submitSurvey(survey = testSurvey) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val model = resource.data
            assertNotNull(model)
            assertEquals(testSurvey.key, model?.key)
            assertEquals(1, model?.questions?.size)
            assertEquals(SurveyQuestionType.SLIDER, model?.questions?.get(0)?.type)
            assertEquals(1L, model?.questions?.get(0)?.id)
            assertEquals(1, model?.questions?.get(0)?.answers?.size)
            assertEquals(1L, model?.questions?.get(0)?.answers?.get(0)?.id)
            assertEquals(SurveyAnswerType.SELECTION, model?.questions?.get(0)?.answers?.get(0)?.type)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(SurveysAPI.URL_SURVEYS, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testSubmitSurveyFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        surveys.submitSurvey(survey = testSurveyData()) { result ->
            assertEquals(Resource.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }
}