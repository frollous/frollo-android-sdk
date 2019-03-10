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

import org.jetbrains.anko.doAsync
import us.frollo.frollosdk.network.NetworkService

internal object Log {

    var network: NetworkService? = null

    var logLevel: LogLevel = LogLevel.ERROR
        set(value) = updateLogLevel(value)

    internal var debugLoggers = mutableListOf<Logger>()
    internal var infoLoggers = mutableListOf<Logger>()
    internal var errorLoggers = mutableListOf<Logger>()

    private fun updateLogLevel(level: LogLevel) {
        val consoleLogger = ConsoleLogger()
        val networkLogger = NetworkLogger(network)

        debugLoggers.clear()
        infoLoggers.clear()
        errorLoggers.clear()

        when(level) {
            LogLevel.DEBUG -> {
                debugLoggers.add(consoleLogger)
                infoLoggers.add(consoleLogger)
                errorLoggers.add(consoleLogger)
                errorLoggers.add(networkLogger)
            }
            LogLevel.INFO -> {
                infoLoggers.add(consoleLogger)
                errorLoggers.add(consoleLogger)
                errorLoggers.add(networkLogger)
            }
            LogLevel.ERROR -> {
                errorLoggers.add(consoleLogger)
                errorLoggers.add(networkLogger)
            }
        }
    }

    internal fun d(tag: String, message: String?) =
            message?.let { debugLog(String.format("%s : %s", tag, it)) }

    internal fun i(tag: String, message: String?) =
            message?.let { infoLog(String.format("%s : %s", tag, it)) }

    internal fun e(tag: String, message: String?) =
            message?.let { errorLog(String.format("%s : %s", tag, it)) }

    private fun debugLog(message: String) {
        debugLoggers.forEach { logger ->
            doAsync {
                logger.writeMessage(message, LogLevel.DEBUG)
            }
        }
    }

    private fun infoLog(message: String) {
        infoLoggers.forEach { logger ->
            doAsync {
                logger.writeMessage(message, LogLevel.INFO)
            }
        }
    }

    private fun errorLog(message: String) {
        errorLoggers.forEach { logger ->
            doAsync {
                logger.writeMessage(message, LogLevel.ERROR)
            }
        }
    }
}