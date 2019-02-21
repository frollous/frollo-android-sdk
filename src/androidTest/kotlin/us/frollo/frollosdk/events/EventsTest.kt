package us.frollo.frollosdk.events

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
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.EventsAPI
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.testutils.wait

class EventsTest {

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private lateinit var mockServer: MockWebServer
    private lateinit var preferences: Preferences
    private lateinit var keystore: Keystore
    private lateinit var network: NetworkService

    private lateinit var events: Events

    private fun initSetup() {
        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url("/")

        if (!FrolloSDK.isSetup) FrolloSDK.setup(app, SetupParams.Builder().serverUrl(baseUrl.toString()).build()) {}

        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        network = NetworkService(baseUrl.toString(), keystore, preferences)

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        events = Events(network)
    }

    private fun tearDown() {
        mockServer.shutdown()
        preferences.resetAll()
    }

    @Test
    fun testTriggerEvent() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == EventsAPI.URL_EVENT) {
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
        assertEquals(EventsAPI.URL_EVENT, request.path)

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
    fun testEventNotHandled() {
        initSetup()

        events.handleEvent("UNKNOWN_EVENT") { handled, error  ->
            assertNull(error)
            assertFalse(handled)
        }

        tearDown()
    }
}