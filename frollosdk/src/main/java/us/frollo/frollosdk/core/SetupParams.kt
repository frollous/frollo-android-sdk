package us.frollo.frollosdk.core

import android.util.Log

data class SetupParams(val serverUrl: String, val logLevel: Int) {

    class Builder {
        private var serverUrl = ""
        private var logLevel = Log.ERROR

        fun serverUrl(serverUrl: String) = apply { this.serverUrl = serverUrl }
        fun logLevel(logLevel: Int) = apply { this.logLevel = logLevel }

        fun build() = SetupParams(serverUrl, logLevel)
    }
}