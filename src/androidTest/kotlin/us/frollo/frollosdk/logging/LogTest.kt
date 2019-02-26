package us.frollo.frollosdk.logging

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.*
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.authentication.AuthToken
import us.frollo.frollosdk.authentication.OAuth
import us.frollo.frollosdk.core.testSDKConfig
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.DeviceAPI
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.testutils.wait

class LogTest {
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

        Log.network = network
    }

    private fun tearDown() {
        mockServer.shutdown()
        preferences.resetAll()
        Log.debugLoggers.clear()
        Log.infoLoggers.clear()
        Log.errorLoggers.clear()
    }

    @Test
    fun testDebugLogLevel() {
        initSetup()

        Log.logLevel = LogLevel.DEBUG

        assertTrue(Log.debugLoggers.size > 0)
        val typesOfDebugLogger = typesOfLoggerIn(Log.debugLoggers)
        assertTrue(typesOfDebugLogger.console)
        assertFalse(typesOfDebugLogger.network)

        assertTrue(Log.infoLoggers.size > 0)
        val typesOfInfoLogger = typesOfLoggerIn(Log.infoLoggers)
        assertTrue(typesOfInfoLogger.console)
        assertFalse(typesOfInfoLogger.network)

        assertTrue(Log.errorLoggers.size > 0)
        val typesOfErrorLogger = typesOfLoggerIn(Log.errorLoggers)
        assertTrue(typesOfErrorLogger.console)
        assertTrue(typesOfErrorLogger.network)

        tearDown()
    }

    @Test
    fun testInfoLogLevel() {
        initSetup()

        Log.logLevel = LogLevel.INFO

        assertTrue(Log.debugLoggers.size == 0)

        assertTrue(Log.infoLoggers.size > 0)
        val typesOfInfoLogger = typesOfLoggerIn(Log.infoLoggers)
        assertTrue(typesOfInfoLogger.console)
        assertFalse(typesOfInfoLogger.network)

        assertTrue(Log.errorLoggers.size > 0)
        val typesOfErrorLogger = typesOfLoggerIn(Log.errorLoggers)
        assertTrue(typesOfErrorLogger.console)
        assertTrue(typesOfErrorLogger.network)

        tearDown()
    }

    @Test
    fun testErrorLogLevel() {
        initSetup()

        Log.logLevel = LogLevel.ERROR

        assertTrue(Log.debugLoggers.size == 0)

        assertTrue(Log.infoLoggers.size == 0)

        assertTrue(Log.errorLoggers.size > 0)
        val typesOfErrorLogger = typesOfLoggerIn(Log.errorLoggers)
        assertTrue(typesOfErrorLogger.console)
        assertTrue(typesOfErrorLogger.network)

        tearDown()
    }

    @Test
    fun testDebugMessage() {
        initSetup()
        Log.logLevel = LogLevel.DEBUG
        Log.d("Tag", "Test Message")
        tearDown()
    }

    @Test
    fun testInfoMessage() {
        initSetup()
        Log.logLevel = LogLevel.INFO
        Log.i("Tag", "Test Message")
        tearDown()
    }

    @Test
    fun testErrorMessage() {
        initSetup()
        Log.logLevel = LogLevel.ERROR

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == DeviceAPI.URL_LOG) {
                    return MockResponse()
                            .setResponseCode(201)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        Log.e("Tag", "Test Message 1")
        Log.e("Tag", "Test Message 2")
        Log.e("Tag", "Test Message 3")

        wait(3)

        assertEquals(3, mockServer.requestCount)
        val request = mockServer.takeRequest()
        assertEquals(DeviceAPI.URL_LOG, request.path)

        tearDown()
    }

    private fun typesOfLoggerIn(loggers: List<Logger>): TypesOfLogger {
        var consoleLoggerFound = false
        var networkLoggerFound = false

        loggers.forEach { logger ->
            when(logger) {
                is ConsoleLogger -> consoleLoggerFound = true
                is NetworkLogger -> networkLoggerFound = true
            }
        }

        return TypesOfLogger(consoleLoggerFound, networkLoggerFound)
    }

    private inner class TypesOfLogger(val console: Boolean, val network: Boolean)
}