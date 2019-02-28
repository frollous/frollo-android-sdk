package us.frollo.frollosdk.logging

import android.app.Application
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
import us.frollo.frollosdk.core.testSDKConfig
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.DeviceAPI
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.testutils.trimmedPath

class NetworkLoggerTest {

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private lateinit var mockServer: MockWebServer
    private lateinit var preferences: Preferences
    private lateinit var keystore: Keystore
    private lateinit var network: NetworkService

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
    }

    private fun tearDown() {
        mockServer.shutdown()
        preferences.resetAll()
    }

    @Test
    fun testLogging() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == DeviceAPI.URL_LOG) {
                    return MockResponse()
                            .setResponseCode(201)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val logger = NetworkLogger(network)
        logger.writeMessage("Test Message", LogLevel.ERROR)

        val request = mockServer.takeRequest()
        assertEquals(DeviceAPI.URL_LOG, request.trimmedPath)

        tearDown()
    }
}