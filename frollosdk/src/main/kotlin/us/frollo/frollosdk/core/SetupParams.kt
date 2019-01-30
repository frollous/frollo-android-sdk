package us.frollo.frollosdk.core

import us.frollo.frollosdk.logging.LogLevel

data class SetupParams(val serverUrl: String, val logLevel: LogLevel) {

    class Builder {
        private var serverUrl = ""
        private var logLevel = LogLevel.ERROR

        fun serverUrl(serverUrl: String) = apply { this.serverUrl = serverUrl }
        fun logLevel(logLevel: LogLevel) = apply { this.logLevel = logLevel }

        fun build() = SetupParams(serverUrl, logLevel)
    }
}