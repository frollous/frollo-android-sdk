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

package us.frollo.frollosdk.logging

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.BaseAndroidTest
import us.frollo.frollosdk.network.api.DeviceAPI
import us.frollo.frollosdk.testutils.trimmedPath
import us.frollo.frollosdk.testutils.wait

class LogTest : BaseAndroidTest() {
    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        Log.network = network
    }

    override fun tearDown() {
        super.tearDown()

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

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == DeviceAPI.URL_LOG) {
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
        assertEquals(DeviceAPI.URL_LOG, request.trimmedPath)

        tearDown()
    }

    private fun typesOfLoggerIn(loggers: List<Logger>): TypesOfLogger {
        var consoleLoggerFound = false
        var networkLoggerFound = false

        loggers.forEach { logger ->
            when (logger) {
                is ConsoleLogger -> consoleLoggerFound = true
                is NetworkLogger -> networkLoggerFound = true
            }
        }

        return TypesOfLogger(consoleLoggerFound, networkLoggerFound)
    }

    private inner class TypesOfLogger(val console: Boolean, val network: Boolean)
}