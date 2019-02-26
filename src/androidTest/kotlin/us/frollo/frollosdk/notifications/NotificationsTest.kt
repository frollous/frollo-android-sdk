package us.frollo.frollosdk.notifications

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.jraska.livedata.test
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.authentication.AuthToken
import us.frollo.frollosdk.authentication.Authentication
import us.frollo.frollosdk.authentication.OAuth
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.core.testSDKConfig
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.network.NetworkHelper
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.DeviceAPI
import us.frollo.frollosdk.events.Events
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.messages.Messages
import us.frollo.frollosdk.model.testEventNotificationBundle
import us.frollo.frollosdk.model.testMessageNotificationBundle
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.wait

class NotificationsTest {

    @get:Rule val testRule = InstantTaskExecutorRule()
    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private lateinit var mockServer: MockWebServer
    private lateinit var preferences: Preferences
    private lateinit var keystore: Keystore
    private lateinit var database: SDKDatabase
    private lateinit var network: NetworkService

    private lateinit var authentication: Authentication
    private lateinit var events: Events
    private lateinit var messages: Messages
    private lateinit var notifications: Notifications

    private fun initSetup() {
        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url("/")

        val config = testSDKConfig(serverUrl = baseUrl.toString())
        if (!FrolloSDK.isSetup) FrolloSDK.setup(app, config) {}

        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        database = SDKDatabase.getInstance(app)
        val oAuth = OAuth(config = config)
        network = NetworkService(oAuth = oAuth, keystore = keystore, pref = preferences)

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        messages = Messages(network, database)
        events = Events(network)
        authentication = Authentication(oAuth, DeviceInfo(app), network, database, preferences)

        notifications = Notifications(authentication, events, messages)
    }

    private fun tearDown() {
        mockServer.shutdown()
        preferences.resetAll()
        database.clearAllTables()
    }

    @Test
    fun testRegisterPushNotificationToken() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == DeviceAPI.URL_DEVICE) {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val tokenString = "740f4707bebcf74f9b7c25d48e3358945f6aa01da5ddb387462c7eaf61bb78ad"

        notifications.registerPushNotificationToken(tokenString)

        val request = mockServer.takeRequest()
        assertEquals(DeviceAPI.URL_DEVICE, request.path)

        tearDown()
    }

    @Test
    fun testHandlePushNotificationEvent() {
        initSetup()

        notifications.handlePushNotification(testEventNotificationBundle())

        // TODO: How to test this?

        tearDown()
    }

    @Test
    fun testHandlePushNotificationMessage() {
        initSetup()

        val body = readStringFromJson(app, R.raw.message_id_12345)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == "${NetworkHelper.API_VERSION_PATH}/messages/12345") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        notifications.handlePushNotification(testMessageNotificationBundle())

        wait(3)

        val request = mockServer.takeRequest()
        assertEquals("${NetworkHelper.API_VERSION_PATH}/messages/12345", request.path)

        val testObserver = messages.fetchMessage(12345L).test()
        testObserver.awaitValue()
        val model = testObserver.value().data
        assertNotNull(model)
        assertEquals(12345L, model?.messageId)

        tearDown()
    }
}