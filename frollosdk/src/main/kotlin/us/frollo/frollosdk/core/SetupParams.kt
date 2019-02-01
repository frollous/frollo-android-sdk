package us.frollo.frollosdk.core

import us.frollo.frollosdk.logging.LogLevel

/**
 * Parameters to configure SDK setup.
 */
data class SetupParams(
        /**
         * Base URL of the Frollo API this SDK should point to
         */
        val serverUrl: String,
        /**
         * Level of logging for debug and error messages
         */
        val logLevel: LogLevel) {

    /**
     * Builder class to create an instance of [SetupParams]
     */
    class Builder {
        private var serverUrl = ""
        private var logLevel = LogLevel.ERROR

        /**
         * Sets the server URL.
         */
        fun serverUrl(serverUrl: String) = apply { this.serverUrl = serverUrl }
        /**
         * Sets the log level for logging.
         */
        fun logLevel(logLevel: LogLevel) = apply { this.logLevel = logLevel }

        /**
         * Creates an instance of [SetupParams]
         */
        fun build() = SetupParams(serverUrl, logLevel)
    }
}