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

package us.frollo.frollosdk.goals

import com.jraska.livedata.test
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.BaseAndroidTest
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.mapping.toGoal
import us.frollo.frollosdk.model.coredata.goals.GoalFrequency
import us.frollo.frollosdk.model.coredata.goals.GoalStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTarget
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingType
import us.frollo.frollosdk.model.testGoalResponseData
import us.frollo.frollosdk.network.api.GoalsAPI
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import us.frollo.frollosdk.testutils.wait
import java.math.BigDecimal

class GoalsTest : BaseAndroidTest() {

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
    }

    // Goal Tests

    @Test
    fun testFetchGoalById() {
        initSetup()

        val data1 = testGoalResponseData(goalId = 100)
        val data2 = testGoalResponseData(goalId = 101)
        val data3 = testGoalResponseData(goalId = 102)

        val list = mutableListOf(data1, data2, data3)

        database.goals().insertAll(*list.map { it.toGoal() }.toList().toTypedArray())

        val testObserver = goals.fetchGoal(goalId = 101).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(101L, testObserver.value().data?.goalId)

        tearDown()
    }

    @Test
    fun testFetchGoals() {
        initSetup()

        val data1 = testGoalResponseData(goalId = 100, frequency = GoalFrequency.MONTHLY, status = GoalStatus.ACTIVE, trackingStatus = GoalTrackingStatus.ON_TRACK, accountId = 200)
        val data2 = testGoalResponseData(goalId = 101, frequency = GoalFrequency.MONTHLY, status = GoalStatus.ACTIVE, trackingStatus = GoalTrackingStatus.ON_TRACK, accountId = 200)
        val data3 = testGoalResponseData(goalId = 102, frequency = GoalFrequency.ANNUALLY, status = GoalStatus.ACTIVE, trackingStatus = GoalTrackingStatus.ON_TRACK, accountId = 200)
        val data4 = testGoalResponseData(goalId = 103, frequency = GoalFrequency.ANNUALLY, status = GoalStatus.ACTIVE, trackingStatus = GoalTrackingStatus.ON_TRACK, accountId = 200)
        val data5 = testGoalResponseData(goalId = 105, frequency = GoalFrequency.MONTHLY, status = GoalStatus.ACTIVE, trackingStatus = GoalTrackingStatus.ON_TRACK, accountId = 200)
        val data6 = testGoalResponseData(goalId = 106, frequency = GoalFrequency.MONTHLY, status = GoalStatus.CANCELLED, trackingStatus = GoalTrackingStatus.ON_TRACK, accountId = 200)
        val data7 = testGoalResponseData(goalId = 107, frequency = GoalFrequency.MONTHLY, status = GoalStatus.ACTIVE, trackingStatus = GoalTrackingStatus.AHEAD, accountId = 200)
        val data8 = testGoalResponseData(goalId = 108, frequency = GoalFrequency.MONTHLY, status = GoalStatus.ACTIVE, trackingStatus = GoalTrackingStatus.ON_TRACK, accountId = 201)
        val data9 = testGoalResponseData(goalId = 109, frequency = GoalFrequency.BIANNUALLY, status = GoalStatus.ACTIVE, trackingStatus = GoalTrackingStatus.ON_TRACK, accountId = 201)

        val list = mutableListOf(data1, data2, data3, data4, data5, data6, data7, data8, data9)

        database.goals().insertAll(*list.map { it.toGoal() }.toList().toTypedArray())

        val testObserver = goals.fetchGoals(
                accountId = 200,
                frequency = GoalFrequency.MONTHLY,
                status = GoalStatus.ACTIVE,
                trackingStatus = GoalTrackingStatus.ON_TRACK).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().data?.isNotEmpty() == true)
        assertEquals(3, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testRefreshGoalById() {
        initSetup()

        val goalId: Long = 3211

        val requestPath = "goals/$goalId"

        val body = readStringFromJson(app, R.raw.goal_id_3211)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        goals.refreshGoal(goalId = goalId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = goals.fetchGoal(goalId = goalId).test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)
            assertEquals(goalId, testObserver.value().data?.goalId)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshGoalByIdFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        goals.refreshGoal(goalId = 3211) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshGoals() {
        initSetup()

        val body = readStringFromJson(app, R.raw.goals_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == GoalsAPI.URL_GOALS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        goals.refreshGoals { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = goals.fetchGoals().test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)
            assertEquals(3, testObserver.value().data?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals(GoalsAPI.URL_GOALS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshGoalsFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        goals.refreshGoals { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testCreateGoalTargetAmount() {
        initSetup()

        val body = readStringFromJson(app, R.raw.goal_id_3211)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == GoalsAPI.URL_GOALS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        goals.createGoal(
                name = "My test goal",
                description = "The bestest test goal",
                imageUrl = "https://example.com/image.png",
                type = "Holiday",
                subType = "Winter",
                target = GoalTarget.AMOUNT,
                trackingType = GoalTrackingType.CREDIT,
                frequency = GoalFrequency.WEEKLY,
                startDate = null,
                endDate = "2019-07-15",
                periodAmount = BigDecimal(700),
                startAmount = BigDecimal(0),
                targetAmount = BigDecimal(20000),
                accountId = 123
        ) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = goals.fetchGoal(goalId = 3211).test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)
            assertEquals(3211L, testObserver.value().data?.goalId)
            assertEquals(GoalTarget.AMOUNT, testObserver.value().data?.target)
        }

        val request = mockServer.takeRequest()
        assertEquals(GoalsAPI.URL_GOALS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testCreateGoalTargetDate() {
        initSetup()

        val body = readStringFromJson(app, R.raw.goal_id_3212)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == GoalsAPI.URL_GOALS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        goals.createGoal(
                name = "My test goal",
                description = "The bestest test goal",
                imageUrl = "https://example.com/image.png",
                type = "Holiday",
                subType = "Winter",
                target = GoalTarget.DATE,
                trackingType = GoalTrackingType.CREDIT,
                frequency = GoalFrequency.WEEKLY,
                startDate = null,
                endDate = "2019-07-15",
                periodAmount = BigDecimal(700),
                startAmount = BigDecimal(0),
                targetAmount = BigDecimal(20000),
                accountId = 123
        ) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = goals.fetchGoal(goalId = 3212).test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)
            assertEquals(3212L, testObserver.value().data?.goalId)
            assertEquals(GoalTarget.DATE, testObserver.value().data?.target)
        }

        val request = mockServer.takeRequest()
        assertEquals(GoalsAPI.URL_GOALS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testCreateGoalTargetOpenEnded() {
        initSetup()

        val body = readStringFromJson(app, R.raw.goal_id_3213)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == GoalsAPI.URL_GOALS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        goals.createGoal(
                name = "My test goal",
                description = "The bestest test goal",
                imageUrl = "https://example.com/image.png",
                type = "Holiday",
                subType = "Winter",
                target = GoalTarget.OPEN_ENDED,
                trackingType = GoalTrackingType.CREDIT,
                frequency = GoalFrequency.WEEKLY,
                startDate = null,
                endDate = "2019-07-15",
                periodAmount = BigDecimal(700),
                startAmount = BigDecimal(0),
                targetAmount = BigDecimal(20000),
                accountId = 123
        ) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = goals.fetchGoal(goalId = 3213).test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)
            assertEquals(3213L, testObserver.value().data?.goalId)
            assertEquals(GoalTarget.OPEN_ENDED, testObserver.value().data?.target)
        }

        val request = mockServer.takeRequest()
        assertEquals(GoalsAPI.URL_GOALS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testCreateGoalFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        goals.createGoal(
                name = "My test goal",
                description = "The bestest test goal",
                imageUrl = "https://example.com/image.png",
                type = "Holiday",
                subType = "Winter",
                target = GoalTarget.AMOUNT,
                trackingType = GoalTrackingType.CREDIT,
                frequency = GoalFrequency.WEEKLY,
                startDate = null,
                endDate = "2019-07-15",
                periodAmount = BigDecimal(700),
                startAmount = BigDecimal(0),
                targetAmount = BigDecimal(20000),
                accountId = 123
        ) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testCreateGoalInvalidDataFails() {
        initSetup()

        goals.createGoal(
                name = "My test goal",
                description = "The bestest test goal",
                imageUrl = "https://example.com/image.png",
                type = "Holiday",
                subType = "Winter",
                target = GoalTarget.AMOUNT,
                trackingType = GoalTrackingType.CREDIT,
                frequency = GoalFrequency.WEEKLY,
                startDate = null,
                endDate = "2019-07-15",
                periodAmount = BigDecimal(700),
                startAmount = BigDecimal(0),
                targetAmount = null,
                accountId = 123
        ) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.API, (result.error as DataError).type)
            assertEquals(DataErrorSubType.INVALID_DATA, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateGoal() {
        initSetup()

        val goalId: Long = 3211

        val requestPath = "goals/$goalId"

        val body = readStringFromJson(app, R.raw.goal_id_3211)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val goal = testGoalResponseData(goalId = goalId).toGoal()

        database.goals().insert(goal)

        goals.updateGoal(goal) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = goals.fetchGoal(goalId = goalId).test()

            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(goalId, models?.goalId)
            assertEquals("Holiday Fund", models?.name)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateGoalFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        val goal = testGoalResponseData(goalId = 3211).toGoal()
        goals.updateGoal(goal) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testDeleteGoal() {
        initSetup()

        val goalId: Long = 3211

        val requestPath = "goals/$goalId"

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        database.goals().insert(testGoalResponseData(goalId = goalId).toGoal())

        var testObserver = goals.fetchGoal(goalId).test()

        testObserver.awaitValue()
        val model = testObserver.value().data
        assertNotNull(model)
        assertEquals(goalId, model?.goalId)

        goals.deleteGoal(goalId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            testObserver = goals.fetchGoal(goalId).test()

            testObserver.awaitValue()
            assertNull(testObserver.value().data)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testDeleteGoalFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        goals.deleteGoal(3211) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }
}