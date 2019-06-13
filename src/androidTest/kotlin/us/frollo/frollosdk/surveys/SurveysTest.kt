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

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Test

import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.authentication.Authentication
import us.frollo.frollosdk.authentication.OAuth2Helper
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.testSDKConfig
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.model.coredata.surveys.SurveyAnswerType
import us.frollo.frollosdk.model.coredata.surveys.SurveyQuestionType
import us.frollo.frollosdk.model.testSurveyData
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.SurveysAPI
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import us.frollo.frollosdk.testutils.wait

class SurveysTest {

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private lateinit var mockServer: MockWebServer
    private lateinit var preferences: Preferences
    private lateinit var keystore: Keystore
    private lateinit var network: NetworkService

    private lateinit var surveys: Surveys

    private fun initSetup() {
        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url("/")

        val config = testSDKConfig(serverUrl = baseUrl.toString())
        if (!FrolloSDK.isSetup) FrolloSDK.setup(app, config) {}

        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        val oAuth = OAuth2Helper(config = config)
        network = NetworkService(oAuth2Helper = oAuth, keystore = keystore, pref = preferences)

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val database = SDKDatabase.getInstance(app)
        val authentication = Authentication(oAuth, network, preferences, FrolloSDK)
        surveys = Surveys(network, authentication)
    }

    private fun tearDown() {
        mockServer.shutdown()
        preferences.resetAll()
    }

    @Test
    fun testFetchSurvey() {
        initSetup()

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
            assertEquals(SurveyQuestionType.SLIDER, model?.questions?.get(0)?.type)
            assertEquals(1L, model?.questions?.get(0)?.id)
            assertEquals(1, model?.questions?.get(0)?.answers?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals("user/surveys/$surveyKey", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchSurveyFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        surveys.fetchSurvey(surveyKey = "FINANCIAL_WELLBEING") { result ->
            assertEquals(Resource.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchLatestSurvey() {
        initSetup()

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
        }

        val request = mockServer.takeRequest()
        assertEquals("user/surveys/$surveyKey?latest=$latest", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testSubmitSurvey() {
        initSetup()

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
        }

        val request = mockServer.takeRequest()
        assertEquals(SurveysAPI.URL_SURVEYS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testSubmitSurveyFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        surveys.submitSurvey(survey = testSurveyData()) { result ->
            assertEquals(Resource.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }
}