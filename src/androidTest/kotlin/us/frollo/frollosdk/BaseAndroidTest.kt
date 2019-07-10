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

package us.frollo.frollosdk

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.threetenabp.AndroidThreeTen
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import us.frollo.frollosdk.aggregation.Aggregation
import us.frollo.frollosdk.authentication.Authentication
import us.frollo.frollosdk.authentication.OAuth2Authentication
import us.frollo.frollosdk.authentication.OAuth2Helper
import us.frollo.frollosdk.bills.Bills
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.core.testSDKConfig
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.events.Events
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.messages.Messages
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.TokenAPI
import us.frollo.frollosdk.notifications.Notifications
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.reports.Reports
import us.frollo.frollosdk.surveys.Surveys
import us.frollo.frollosdk.user.UserManagement

abstract class BaseAndroidTest {

    companion object {
        const val TOKEN_URL = "token/"
        const val REVOKE_TOKEN_URL = "revoke/"
    }

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    lateinit var mockServer: MockWebServer
    lateinit var mockTokenServer: MockWebServer
    lateinit var mockRevokeTokenServer: MockWebServer
    lateinit var network: NetworkService
    lateinit var preferences: Preferences
    lateinit var keystore: Keystore
    lateinit var database: SDKDatabase
    lateinit var authentication: Authentication
    lateinit var aggregation: Aggregation
    lateinit var userManagement: UserManagement
    lateinit var bills: Bills
    lateinit var events: Events
    lateinit var messages: Messages
    lateinit var notifications: Notifications
    lateinit var reports: Reports
    lateinit var surveys: Surveys

    val scopes = listOf("offline_access", "openid", "email")

    protected open fun initSetup() {
        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url("/")

        mockTokenServer = MockWebServer()
        mockTokenServer.start()
        val baseTokenUrl = mockTokenServer.url("/$TOKEN_URL")

        mockRevokeTokenServer = MockWebServer()
        mockRevokeTokenServer.start()
        val baseRevokeTokenUrl = mockRevokeTokenServer.url("/$REVOKE_TOKEN_URL")

        val config = testSDKConfig(serverUrl = baseUrl.toString(), tokenUrl = baseTokenUrl.toString(), revokeTokenURL = baseRevokeTokenUrl.toString())
        if (!FrolloSDK.isSetup) FrolloSDK.setup(app, config) {}

        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        database = SDKDatabase.getInstance(app)
        val oAuth = OAuth2Helper(config = config)
        network = NetworkService(oAuth2Helper = oAuth, keystore = keystore, pref = preferences)

        authentication = OAuth2Authentication(oAuth, preferences, authenticationCallback = FrolloSDK, tokenCallback = network).apply {
                tokenAPI = network.createAuth(TokenAPI::class.java)
                revokeTokenAPI = network.createRevoke(TokenAPI::class.java)
                authToken = network.authToken
            }
        network.authentication = authentication
        userManagement = UserManagement(DeviceInfo(app), network, database, preferences, authentication, authenticationCallback = FrolloSDK)
        aggregation = Aggregation(network, database, LocalBroadcastManager.getInstance(app), authentication)
        bills = Bills(network, database, aggregation, authentication)
        events = Events(network, authentication)
        messages = Messages(network, database, authentication)
        notifications = Notifications(userManagement, events, messages)
        reports = Reports(network, database, aggregation, authentication)
        surveys = Surveys(network, authentication)

        AndroidThreeTen.init(app)
    }

    protected open fun tearDown() {
        mockServer.shutdown()
        mockTokenServer.shutdown()
        mockRevokeTokenServer.shutdown()
        network.reset()
        authentication.reset()
        preferences.resetAll()
        database.clearAllTables()
    }
}