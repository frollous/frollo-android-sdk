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

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Test

import org.junit.Assert.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.authentication.OAuth
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.ACTION.ACTION_REFRESH_TRANSACTIONS
import us.frollo.frollosdk.core.ARGUMENT
import us.frollo.frollosdk.core.testSDKConfig
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.EventsAPI
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.model.testTransactionUpdatedNotificationPayload
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.testutils.trimmedPath
import us.frollo.frollosdk.testutils.wait

class EventsTest {

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private lateinit var mockServer: MockWebServer
    private lateinit var preferences: Preferences
    private lateinit var keystore: Keystore
    private lateinit var network: NetworkService

    private lateinit var events: Events

    private var notifyFlag = false
    private var transactionIds: LongArray? = null

    private fun initSetup() {
        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url("/")

        val config = testSDKConfig(serverUrl = baseUrl.toString())
        if (!FrolloSDK.isSetup) FrolloSDK.setup(app, config) {}

        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        val oAuth = OAuth(config = config)
        network = NetworkService(oAuth = oAuth, keystore = keystore, pref = preferences)

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        events = Events(network)
    }

    private fun tearDown() {
        mockServer.shutdown()
        preferences.resetAll()
        notifyFlag = false
    }

    @Test
    fun testTriggerEvent() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
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
        }

        val request = mockServer.takeRequest()
        assertEquals(EventsAPI.URL_EVENT, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testEventHandled() {
        initSetup()

        events.handleEvent("TEST_EVENT") { handled, error  ->
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

        events.handleEvent("T_UPDATED", notificationPayload = payload) { handled, error  ->
            assertNull(error)
            assertTrue(handled)
        }

        wait(2)

        assertTrue(notifyFlag)
        assertTrue(transactionIds?.toList()?.containsAll(listOf(45123L, 986L, 7000072L)) == true)

        lbm.unregisterReceiver(receiver)

        tearDown()
    }

    @Test
    fun testEventNotHandled() {
        initSetup()

        events.handleEvent("UNKNOWN_EVENT") { handled, error  ->
            assertNull(error)
            assertFalse(handled)
        }

        tearDown()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            notifyFlag = true
            transactionIds = intent.getBundleExtra(ARGUMENT.ARG_DATA).getLongArray(ARGUMENT.ARG_TRANSACTION_IDS)
        }
    }
}