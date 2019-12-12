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

package us.frollo.frollosdk.budgets

import com.jraska.livedata.test
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.BaseAndroidTest
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.mapping.toBudget
import us.frollo.frollosdk.model.coredata.budgets.BudgetFrequency
import us.frollo.frollosdk.model.coredata.budgets.BudgetStatus
import us.frollo.frollosdk.model.coredata.budgets.BudgetTrackingStatus
import us.frollo.frollosdk.model.testBudgetResponseData
import us.frollo.frollosdk.network.api.BudgetsAPI
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.mapping.toBudgetPeriod
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.model.testBudgetPeriodResponseData
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import us.frollo.frollosdk.testutils.wait
import java.math.BigDecimal

class BudgetsTest : BaseAndroidTest() {

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
    }

    @Test
    fun testFetchBudgets() {
        initSetup()

        val data1 = testBudgetResponseData(100, frequency = BudgetFrequency.MONTHLY, status = BudgetStatus.ACTIVE, trackingStatus = BudgetTrackingStatus.ON_TRACK)
        val data2 = testBudgetResponseData(101, frequency = BudgetFrequency.MONTHLY, status = BudgetStatus.ACTIVE, trackingStatus = BudgetTrackingStatus.ON_TRACK)
        val data3 = testBudgetResponseData(102, frequency = BudgetFrequency.ANNUALLY, status = BudgetStatus.ACTIVE, trackingStatus = BudgetTrackingStatus.ON_TRACK)
        val data4 = testBudgetResponseData(103, frequency = BudgetFrequency.ANNUALLY, status = BudgetStatus.ACTIVE, trackingStatus = BudgetTrackingStatus.ON_TRACK)
        val data5 = testBudgetResponseData(105, frequency = BudgetFrequency.MONTHLY, status = BudgetStatus.ACTIVE, trackingStatus = BudgetTrackingStatus.ON_TRACK)
        val data6 = testBudgetResponseData(106, frequency = BudgetFrequency.MONTHLY, status = BudgetStatus.CANCELLED, trackingStatus = BudgetTrackingStatus.ON_TRACK)
        val data7 = testBudgetResponseData(107, frequency = BudgetFrequency.MONTHLY, status = BudgetStatus.ACTIVE, trackingStatus = BudgetTrackingStatus.AHEAD)
        val data8 = testBudgetResponseData(108, frequency = BudgetFrequency.MONTHLY, status = BudgetStatus.ACTIVE, trackingStatus = BudgetTrackingStatus.ON_TRACK)
        val data9 = testBudgetResponseData(109, frequency = BudgetFrequency.BIANNUALLY, status = BudgetStatus.ACTIVE, trackingStatus = BudgetTrackingStatus.ON_TRACK)

        val list = mutableListOf(data1, data2, data3, data4, data5, data6, data7, data8, data9)
        database.budgets().insertAll(*list.map { it.toBudget() }.toList().toTypedArray())

        val testObserver = budgets.fetchBudgets(
                frequency = BudgetFrequency.MONTHLY,
                status = BudgetStatus.ACTIVE,
                trackingStatus = BudgetTrackingStatus.ON_TRACK).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().data?.isNotEmpty() == true)
        assertEquals(6, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchBudgetsById() {
        initSetup()

        val data1 = testBudgetResponseData(100, frequency = BudgetFrequency.MONTHLY, status = BudgetStatus.ACTIVE, trackingStatus = BudgetTrackingStatus.ON_TRACK)
        val data2 = testBudgetResponseData(101, frequency = BudgetFrequency.MONTHLY, status = BudgetStatus.ACTIVE, trackingStatus = BudgetTrackingStatus.ON_TRACK)
        val data3 = testBudgetResponseData(102, frequency = BudgetFrequency.ANNUALLY, status = BudgetStatus.ACTIVE, trackingStatus = BudgetTrackingStatus.ON_TRACK)
        val data4 = testBudgetResponseData(103, frequency = BudgetFrequency.ANNUALLY, status = BudgetStatus.ACTIVE, trackingStatus = BudgetTrackingStatus.ON_TRACK)
        val data5 = testBudgetResponseData(105, frequency = BudgetFrequency.MONTHLY, status = BudgetStatus.ACTIVE, trackingStatus = BudgetTrackingStatus.ON_TRACK)
        val data6 = testBudgetResponseData(106, frequency = BudgetFrequency.MONTHLY, status = BudgetStatus.CANCELLED, trackingStatus = BudgetTrackingStatus.ON_TRACK)
        val data7 = testBudgetResponseData(107, frequency = BudgetFrequency.MONTHLY, status = BudgetStatus.ACTIVE, trackingStatus = BudgetTrackingStatus.AHEAD)
        val data8 = testBudgetResponseData(108, frequency = BudgetFrequency.MONTHLY, status = BudgetStatus.ACTIVE, trackingStatus = BudgetTrackingStatus.ON_TRACK)
        val data9 = testBudgetResponseData(109, frequency = BudgetFrequency.BIANNUALLY, status = BudgetStatus.ACTIVE, trackingStatus = BudgetTrackingStatus.ON_TRACK)

        val list = mutableListOf(data1, data2, data3, data4, data5, data6, data7, data8, data9)
        database.budgets().insertAll(*list.map { it.toBudget() }.toList().toTypedArray())

        val testObserver = budgets.fetchBudget(100).test()

        testObserver.awaitValue()
        assertEquals(100L, testObserver.value().data?.budgetId)
        assertEquals(BudgetFrequency.MONTHLY, testObserver.value().data?.frequency)

        tearDown()
    }

    @Test
    fun testRefreshBudgetById() {
        initSetup()

        val budgetId: Long = 6

        val requestPath = "budgets/$budgetId"

        val body = readStringFromJson(app, R.raw.refresh_budget)
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

        budgets.refreshBudget(budgetId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = budgets.fetchBudget(budgetId).test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)
            assertEquals(budgetId, testObserver.value().data?.budgetId)
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

        budgets.refreshBudget(3211) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testCreateBudget() {
        initSetup()

        val body = readStringFromJson(app, R.raw.refresh_budget)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == BudgetsAPI.URL_BUDGETS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        budgets.createBudgetCategoryBudget(BudgetFrequency.MONTHLY, BigDecimal(1000), BudgetCategory.LIFESTYLE, null,
                "https://helpx.adobe.com/content/dam/help/en/stock/how-to/visual-reverse-image-search/jcr_content/main-pars/image/visual-reverse-image-search-v2_intro.jpg") { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = budgets.fetchBudget(6).test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)
            assertEquals(6L, testObserver.value().data?.budgetId)
            assertEquals(BudgetFrequency.MONTHLY, testObserver.value().data?.frequency)
        }

        val request = mockServer.takeRequest()
        assertEquals(BudgetsAPI.URL_BUDGETS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testCreateBudgetFailsIfLoggedOut() {
        initSetup()

        clearLoggedInPreferences()

        budgets.createBudgetCategoryBudget(BudgetFrequency.MONTHLY, BigDecimal(1000), BudgetCategory.LIFESTYLE, null,
                "https://helpx.adobe.com/content/dam/help/en/stock/how-to/visual-reverse-image-search/jcr_content/main-pars/image/visual-reverse-image-search-v2_intro.jpg") { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshBudgetsFailsIfLoggedOut() {
        initSetup()

        clearLoggedInPreferences()

        budgets.refreshBudgets { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    // TODO this needs to be redone
    @Test
    fun testRefreshBudgets() {
        initSetup()

        val body = readStringFromJson(app, R.raw.budget_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath?.contains(BudgetsAPI.URL_BUDGETS) == true) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        budgets.refreshBudgets { resource ->
            val testObserver = budgets.fetchBudgets(current = true).test()
            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)
            assertEquals(1, testObserver.value().data?.size)
        }

        mockServer.takeRequest()
        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateBudget() {
        initSetup()

        val budgetId: Long = 6

        val requestPath = "budgets/$budgetId"

        val body = readStringFromJson(app, R.raw.refresh_budget)
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

        val budget = testBudgetResponseData(budgetId).toBudget()

        database.budgets().insert(budget)

        budgets.updateBudget(budget) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = budgets.fetchBudget(budgetId).test()

            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(budgetId, models?.budgetId)
            assertEquals(BudgetTrackingStatus.BEHIND, models?.trackingStatus)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateBudgetFailsIfLoggedOut() {
        initSetup()

        clearLoggedInPreferences()

        val budget = testBudgetResponseData(6).toBudget()
        budgets.updateBudget(budget) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testDeleteBudget() {
        initSetup()

        val budgetId: Long = 6

        val requestPath = "budgets/$budgetId"

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        database.budgets().insert(testBudgetResponseData(budgetId).toBudget())

        var testObserver = budgets.fetchBudget(budgetId).test()

        testObserver.awaitValue()
        val model = testObserver.value().data
        assertNotNull(model)
        assertEquals(budgetId, model?.budgetId)

        budgets.deleteBudget(budgetId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            testObserver = budgets.fetchBudget(budgetId).test()

            testObserver.awaitValue()
            assertNull(testObserver.value().data)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testDeleteBudgetFailsIfLoggedOut() {
        initSetup()

        clearLoggedInPreferences()

        budgets.deleteBudget(6) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    // Budget Period Tests

    @Test
    fun testFetchBudgetPeriodById() {
        initSetup()

        val data1 = testBudgetPeriodResponseData(budgetPeriodId = 100)
        val data2 = testBudgetPeriodResponseData(budgetPeriodId = 101)
        val data3 = testBudgetPeriodResponseData(budgetPeriodId = 102)
        val data4 = testBudgetPeriodResponseData(budgetPeriodId = 103)
        val data5 = testBudgetPeriodResponseData(budgetPeriodId = 104)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        database.budgetPeriods().insertAll(*list.map { it.toBudgetPeriod() }.toList().toTypedArray())

        val testObserver = budgets.fetchBudgetPeriod(data3.budgetPeriodId).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(data3.budgetPeriodId, testObserver.value().data?.budgetPeriodId)

        tearDown()
    }

    @Test
    fun testFetchBudgetPeriods() {
        initSetup()

        val data1 = testBudgetPeriodResponseData(budgetPeriodId = 100, budgetId = 200, trackingStatus = BudgetTrackingStatus.ON_TRACK)
        val data2 = testBudgetPeriodResponseData(budgetPeriodId = 101, budgetId = 200, trackingStatus = BudgetTrackingStatus.AHEAD)
        val data3 = testBudgetPeriodResponseData(budgetPeriodId = 102, budgetId = 201, trackingStatus = BudgetTrackingStatus.ON_TRACK)
        val data4 = testBudgetPeriodResponseData(budgetPeriodId = 103, budgetId = 200, trackingStatus = BudgetTrackingStatus.ON_TRACK)
        val data5 = testBudgetPeriodResponseData(budgetPeriodId = 104, budgetId = 201, trackingStatus = BudgetTrackingStatus.ON_TRACK)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        database.budgetPeriods().insertAll(*list.map { it.toBudgetPeriod() }.toList().toTypedArray())

        val testObserver = budgets.fetchBudgetPeriods(budgetId = 200, trackingStatus = BudgetTrackingStatus.ON_TRACK).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchBudgetByIdWithRelation() {
        initSetup()

        database.budgets().insert(testBudgetResponseData(budgetId = 6).toBudget())
        database.budgetPeriods().insert(testBudgetPeriodResponseData(budgetPeriodId = 456, budgetId = 6).toBudgetPeriod())

        val testObserver = budgets.fetchBudgetWithRelation(6).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(456L, testObserver.value().data?.periods?.get(0)?.budgetPeriodId)
        assertEquals(4L, testObserver.value().data?.budget?.budgetId)

        tearDown()
    }

    @Test
    fun testRefreshBudgetPeriods() {
        initSetup()

        val budgetId: Long = 6
        val requestPath = "budgets/$budgetId/periods"

        val body = readStringFromJson(app, R.raw.refresh_budget_periods)
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

        budgets.refreshBudgetPeriods(budgetId = budgetId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = budgets.fetchBudgetPeriods(budgetId = budgetId).test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)
            assertEquals(15, testObserver.value().data?.size)

            val period = testObserver.value().data?.first()
            assertEquals(85L, period?.budgetPeriodId)
            assertEquals(4L, period?.budgetId)
            assertEquals(BigDecimal("111.42"), period?.currentAmount)
            assertEquals("2019-11-22", period?.endDate)
            assertEquals(BigDecimal("173.5"), period?.requiredAmount)
            assertEquals("2019-11-21", period?.startDate)
            assertEquals(BigDecimal("15.62"), period?.targetAmount)
            assertEquals(BudgetTrackingStatus.BEHIND, period?.trackingStatus)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshBudgetPeriodsFailsIfLoggedOut() {
        initSetup()

        clearLoggedInPreferences()

        budgets.refreshBudgetPeriods(budgetId = 6) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshBudgetPeriodById() {
        initSetup()

        val budgetId: Long = 6
        val periodId: Long = 85
        val requestPath = "budgets/$budgetId/periods/$periodId"

        val body = readStringFromJson(app, R.raw.refresh_budget_period)
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

        budgets.refreshBudgetPeriod(budgetId = budgetId, periodId = periodId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = budgets.fetchBudgetPeriod(periodId).test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)
            assertEquals(85L, testObserver.value().data?.budgetPeriodId)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshBudgetPeriodByIdFailsIfLoggedOut() {
        initSetup()

        clearLoggedInPreferences()

        budgets.refreshBudgetPeriod(budgetId = 6, periodId = 85) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testBudgetPeriodsLinkToBudgets() {
        initSetup()

        val budgetId: Long = 6
        val requestPath = "budgets/$budgetId/periods"

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.refresh_budget_periods))
                } else if (request?.trimmedPath == BudgetsAPI.URL_BUDGETS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.refresh_budget))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        budgets.refreshBudgets { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)
        }

        budgets.refreshBudgetPeriods(budgetId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(3)

        val testObserver = budgets.fetchBudgetWithRelation(9000).test()

        testObserver.awaitValue()
        val model = testObserver.value().data
        assertNotNull(model)
        assertEquals(9000L, model?.goalPeriod?.goalPeriodId)
        assertEquals(model?.goalPeriod?.goalId, model?.goal?.goal?.goalId)

        tearDown()
    }
}