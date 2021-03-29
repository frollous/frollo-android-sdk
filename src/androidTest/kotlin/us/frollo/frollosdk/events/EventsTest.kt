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

package us.frollo.frollosdk.events

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.BaseAndroidTest
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.ACTION.ACTION_BUDGET_CURRENT_PERIOD_READY
import us.frollo.frollosdk.core.ACTION.ACTION_ONBOARDING_STEP_COMPLETED
import us.frollo.frollosdk.core.ACTION.ACTION_REFRESH_TRANSACTIONS
import us.frollo.frollosdk.core.ARGUMENT
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.model.testOnboardingStepNotificationPayload
import us.frollo.frollosdk.model.testTransactionUpdatedNotificationPayload
import us.frollo.frollosdk.network.api.EventsAPI
import us.frollo.frollosdk.testutils.trimmedPath
import us.frollo.frollosdk.testutils.wait
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class EventsTest : BaseAndroidTest() {

    private var notifyFlag = false
    private var transactionIds: LongArray? = null
    private var onboardingStep: String? = null

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
    }

    override fun tearDown() {
        super.tearDown()

        transactionIds = null
        notifyFlag = false
        onboardingStep = null
    }

    @Test
    fun testTriggerEvent() {
        initSetup()

        val signal = CountDownLatch(1)

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == EventsAPI.URL_EVENT) {
                    return MockResponse()
                        .setResponseCode(201)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        events.triggerEvent("TEST_EVENT", 15) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(EventsAPI.URL_EVENT, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testTriggerEventFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        events.triggerEvent("TEST_EVENT", 15) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            Assert.assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testEventHandled() {
        initSetup()

        events.handleEvent("TEST_EVENT") { handled, error ->
            assertNull(error)
            assertTrue(handled)
        }

        tearDown()
    }

    @Test
    fun testHandleTransactionsUpdatedEvent() {
        initSetup()

        val lbm = LocalBroadcastManager.getInstance(app)
        lbm.registerReceiver(receiver, IntentFilter(ACTION_REFRESH_TRANSACTIONS))

        val payload = testTransactionUpdatedNotificationPayload()

        events.handleEvent("T_UPDATED", notificationPayload = payload) { handled, error ->
            assertNull(error)
            assertTrue(handled)
        }

        wait(3)

        assertTrue(notifyFlag)
        assertTrue(transactionIds?.toList()?.containsAll(listOf(45123L, 986L, 7000072L)) == true)

        lbm.unregisterReceiver(receiver)

        tearDown()
    }

    @Test
    fun testHandleBudgetCurrentPeriodReadyEvent() {
        initSetup()

        val lbm = LocalBroadcastManager.getInstance(app)
        lbm.registerReceiver(receiver, IntentFilter(ACTION_BUDGET_CURRENT_PERIOD_READY))

        events.handleEvent("B_CURRENT_PERIOD_READY") { handled, error ->
            assertNull(error)
            assertTrue(handled)
        }

        wait(3)

        assertTrue(notifyFlag)

        lbm.unregisterReceiver(receiver)

        tearDown()
    }

    @Test
    fun testHandleOnboardingStepCompletedEvent() {
        initSetup()

        val lbm = LocalBroadcastManager.getInstance(app)
        lbm.registerReceiver(receiver, IntentFilter(ACTION_ONBOARDING_STEP_COMPLETED))

        val payload = testOnboardingStepNotificationPayload()

        events.handleEvent("ONBOARDING_STEP_COMPLETED", notificationPayload = payload) { handled, error ->
            assertNull(error)
            assertTrue(handled)
        }

        wait(3)

        assertTrue(notifyFlag)
        assertEquals("account_opening", onboardingStep)

        lbm.unregisterReceiver(receiver)

        tearDown()
    }

    @Test
    fun testEventNotHandled() {
        initSetup()

        events.handleEvent("UNKNOWN_EVENT") { handled, error ->
            assertNull(error)
            assertFalse(handled)
        }

        tearDown()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            notifyFlag = true
            transactionIds = intent.getLongArrayExtra(ARGUMENT.ARG_TRANSACTION_IDS)
            onboardingStep = intent.getStringExtra(ARGUMENT.ARG_ONBOARDING_STEP_NAME)
        }
    }
}
