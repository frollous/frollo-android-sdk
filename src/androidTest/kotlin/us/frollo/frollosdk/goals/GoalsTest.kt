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
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.mapping.toGoal
import us.frollo.frollosdk.mapping.toGoalPeriod
import us.frollo.frollosdk.model.coredata.goals.GoalFrequency
import us.frollo.frollosdk.model.coredata.goals.GoalStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTarget
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingType
import us.frollo.frollosdk.model.testGoalPeriodResponseData
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

        clearLoggedInPreferences()

        goals.refreshGoal(goalId = 3211) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)
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

        goals.refreshGoals { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val testObserver = goals.fetchGoals().test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)
            assertEquals(3, testObserver.value().data?.size)
            var metadata = testObserver.value().data?.first()?.metadata
            assertEquals(true, metadata?.get("seen")?.asBoolean)

            metadata = testObserver.value().data?.get(1)?.metadata
            assertEquals("Holiday", metadata?.get("type")?.asString)
        }

        val request = mockServer.takeRequest()
        assertEquals(GoalsAPI.URL_GOALS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshGoalsFiltered() {
        initSetup()

        val status = GoalStatus.CANCELLED
        val trackingStatus = GoalTrackingStatus.ON_TRACK

        val requestPath = "goals?status=$status&tracking_status=$trackingStatus"

        val body = readStringFromJson(app, R.raw.goals_filtered_cancelled_ontrack)
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

        val goal = testGoalResponseData(goalId = 3211, status = GoalStatus.ACTIVE, trackingStatus = GoalTrackingStatus.AHEAD).toGoal()
        database.goals().insert(goal)

        // Check goal 3211 is added
        val testObserver1 = goals.fetchGoal(goalId = 3211).test()
        testObserver1.awaitValue()
        assertEquals(3211L, testObserver1.value().data?.goalId)

        goals.refreshGoals(status = status, trackingStatus = trackingStatus) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            // Check goal still exists that doesn't match filter
            val testObserver2 = goals.fetchGoal(goalId = 3211).test()
            testObserver2.awaitValue()
            assertEquals(3211L, testObserver2.value().data?.goalId)

            // Check new goals added
            val testObserver3 = goals.fetchGoals(status = status, trackingStatus = trackingStatus).test()
            testObserver3.awaitValue()
            assertEquals(2, testObserver3.value().data?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshGoalsFailsIfLoggedOut() {
        initSetup()

        clearLoggedInPreferences()

        goals.refreshGoals { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (resource.error as DataError).subType)
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

        clearLoggedInPreferences()

        goals.createGoal(
                name = "My test goal",
                description = "The bestest test goal",
                imageUrl = "https://example.com/image.png",
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
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)
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

        clearLoggedInPreferences()

        val goal = testGoalResponseData(goalId = 3211).toGoal()
        goals.updateGoal(goal) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)
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

        clearLoggedInPreferences()

        goals.deleteGoal(3211) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testGoalsLinkToAccounts() {
        // TODO: to be implemented
    }

    // Goal Period Tests

    @Test
    fun testFetchGoalPeriodById() {
        initSetup()

        val data1 = testGoalPeriodResponseData(goalPeriodId = 100)
        val data2 = testGoalPeriodResponseData(goalPeriodId = 101)
        val data3 = testGoalPeriodResponseData(goalPeriodId = 102)
        val data4 = testGoalPeriodResponseData(goalPeriodId = 103)
        val data5 = testGoalPeriodResponseData(goalPeriodId = 104)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        database.goalPeriods().insertAll(*list.map { it.toGoalPeriod() }.toList().toTypedArray())

        val testObserver = goals.fetchGoalPeriod(data3.goalPeriodId).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(data3.goalPeriodId, testObserver.value().data?.goalPeriodId)

        tearDown()
    }

    @Test
    fun testFetchGoalPeriods() {
        initSetup()

        val data1 = testGoalPeriodResponseData(goalPeriodId = 100, goalId = 200, trackingStatus = GoalTrackingStatus.ON_TRACK)
        val data2 = testGoalPeriodResponseData(goalPeriodId = 101, goalId = 200, trackingStatus = GoalTrackingStatus.AHEAD)
        val data3 = testGoalPeriodResponseData(goalPeriodId = 102, goalId = 201, trackingStatus = GoalTrackingStatus.ON_TRACK)
        val data4 = testGoalPeriodResponseData(goalPeriodId = 103, goalId = 200, trackingStatus = GoalTrackingStatus.ON_TRACK)
        val data5 = testGoalPeriodResponseData(goalPeriodId = 104, goalId = 201, trackingStatus = GoalTrackingStatus.ON_TRACK)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        database.goalPeriods().insertAll(*list.map { it.toGoalPeriod() }.toList().toTypedArray())

        val testObserver = goals.fetchGoalPeriods(goalId = 200, trackingStatus = GoalTrackingStatus.ON_TRACK).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchGoalPeriodByIdWithRelation() {
        initSetup()

        database.goals().insert(testGoalResponseData(goalId = 123).toGoal())
        database.goalPeriods().insert(testGoalPeriodResponseData(goalPeriodId = 456, goalId = 123).toGoalPeriod())

        val testObserver = goals.fetchGoalPeriodWithRelation(goalPeriodId = 456).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(456L, testObserver.value().data?.goalPeriod?.goalPeriodId)
        assertEquals(123L, testObserver.value().data?.goal?.goal?.goalId)

        tearDown()
    }

    @Test
    fun testFetchGoalPeriodsWithRelation() {
        initSetup()

        database.goals().insert(testGoalResponseData(goalId = 123).toGoal())
        database.goalPeriods().insert(testGoalPeriodResponseData(goalPeriodId = 456, goalId = 123, trackingStatus = GoalTrackingStatus.ON_TRACK).toGoalPeriod())
        database.goalPeriods().insert(testGoalPeriodResponseData(goalPeriodId = 457, goalId = 123, trackingStatus = GoalTrackingStatus.AHEAD).toGoalPeriod())
        database.goalPeriods().insert(testGoalPeriodResponseData(goalPeriodId = 458, goalId = 123, trackingStatus = GoalTrackingStatus.BEHIND).toGoalPeriod())
        database.goalPeriods().insert(testGoalPeriodResponseData(goalPeriodId = 459, goalId = 123, trackingStatus = GoalTrackingStatus.ON_TRACK).toGoalPeriod())
        database.goalPeriods().insert(testGoalPeriodResponseData(goalPeriodId = 460, goalId = 223, trackingStatus = GoalTrackingStatus.ON_TRACK).toGoalPeriod())

        val testObserver = goals.fetchGoalPeriodsWithRelation(
                goalId = 123,
                trackingStatus = GoalTrackingStatus.ON_TRACK
        ).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)
        assertEquals(123L, testObserver.value().data?.get(0)?.goal?.goal?.goalId)

        tearDown()
    }

    @Test
    fun testRefreshGoalPeriods() {
        initSetup()

        val goalId: Long = 123
        val requestPath = "goals/$goalId/periods"

        val body = readStringFromJson(app, R.raw.goal_periods_valid)
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

        goals.refreshGoalPeriods(goalId = goalId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = goals.fetchGoalPeriods(goalId = goalId).test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)
            assertEquals(3, testObserver.value().data?.size)

            val period = testObserver.value().data?.first()
            assertEquals(7822L, period?.goalPeriodId)
            assertEquals(123L, period?.goalId)
            assertEquals(BigDecimal("111.42"), period?.currentAmount)
            assertEquals("2019-07-25", period?.endDate)
            assertEquals(BigDecimal("173.5"), period?.requiredAmount)
            assertEquals("2019-07-18", period?.startDate)
            assertEquals(BigDecimal("150"), period?.targetAmount)
            assertEquals(GoalTrackingStatus.BEHIND, period?.trackingStatus)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshGoalPeriodsFailsIfLoggedOut() {
        initSetup()

        clearLoggedInPreferences()

        goals.refreshGoalPeriods(goalId = 123) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshGoalPeriodById() {
        initSetup()

        val goalId: Long = 123
        val goalPeriodId: Long = 897
        val requestPath = "goals/$goalId/periods/$goalPeriodId"

        val body = readStringFromJson(app, R.raw.goal_period_id_897)
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

        goals.refreshGoalPeriod(goalId = goalId, goalPeriodId = goalPeriodId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = goals.fetchGoalPeriod(goalPeriodId = goalPeriodId).test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)
            assertEquals(897L, testObserver.value().data?.goalPeriodId)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshGoalPeriodByIdFailsIfLoggedOut() {
        initSetup()

        clearLoggedInPreferences()

        goals.refreshGoalPeriod(goalId = 123, goalPeriodId = 897) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testGoalPeriodsLinkToGoals() {
        initSetup()

        val goalId: Long = 3211
        val requestPath = "goals/$goalId/periods"

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.goal_periods_linked_valid))
                } else if (request?.trimmedPath == GoalsAPI.URL_GOALS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.goals_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        goals.refreshGoals { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)
        }

        goals.refreshGoalPeriods(goalId = goalId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(3)

        val testObserver = goals.fetchGoalPeriodWithRelation(goalPeriodId = 9000).test()

        testObserver.awaitValue()
        val model = testObserver.value().data
        assertNotNull(model)
        assertEquals(9000L, model?.goalPeriod?.goalPeriodId)
        assertEquals(model?.goalPeriod?.goalId, model?.goal?.goal?.goalId)

        tearDown()
    }

    @Test
    fun testLinkingRemoveCachedCascade() {
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

        database.goals().insert(testGoalResponseData(goalId = 123).toGoal())
        database.goalPeriods().insert(testGoalPeriodResponseData(goalPeriodId = 456, goalId = 123).toGoalPeriod())
        database.goalPeriods().insert(testGoalPeriodResponseData(goalPeriodId = 457, goalId = 123).toGoalPeriod())

        goals.fetchGoal(goalId = 123).test().apply {
            awaitValue()

            assertEquals(123L, value().data?.goalId)
        }

        goals.fetchGoalPeriods(goalId = 123).test().apply {
            awaitValue()

            assertEquals(2, value().data?.size)
            assertEquals(456L, value().data?.get(0)?.goalPeriodId)
            assertEquals(457L, value().data?.get(1)?.goalPeriodId)
        }

        goals.refreshGoals { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            goals.fetchGoals().test().apply {
                awaitValue()

                assertNotNull(value().data)
                assertEquals(3, value().data?.size)
            }

            goals.fetchGoal(goalId = 123).test().apply {
                awaitValue()

                assertNull(value().data)
            }

            goals.fetchGoalPeriod(goalPeriodId = 456).test().apply {
                awaitValue()

                assertNull(value().data)
            }

            goals.fetchGoalPeriod(goalPeriodId = 457).test().apply {
                awaitValue()

                assertNull(value().data)
            }
        }

        wait(3)

        tearDown()
    }
}