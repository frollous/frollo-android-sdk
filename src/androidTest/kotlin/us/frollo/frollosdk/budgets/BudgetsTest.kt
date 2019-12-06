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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.BaseAndroidTest
import us.frollo.frollosdk.mapping.toBudget
import us.frollo.frollosdk.model.coredata.budgets.BudgetFrequency
import us.frollo.frollosdk.model.coredata.budgets.BudgetStatus
import us.frollo.frollosdk.model.coredata.budgets.BudgetTrackingStatus
import us.frollo.frollosdk.model.testBudgetResponseData
import us.frollo.frollosdk.network.api.BudgetsAPI
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import us.frollo.frollosdk.testutils.wait

class BudgetsTest : BaseAndroidTest() {

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
    }

    @Test
    fun testFetchGoals() {
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
        assertEquals(4, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testRefreshBudgetsFailsIfLoggedOut() {
        initSetup()

        clearLoggedInPreferences()

        budgets.refreshBudgets { result ->
            assertEquals("API error", result.exceptionOrNull()?.localizedMessage)
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
}