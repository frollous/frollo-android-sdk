package us.frollo.frollosdk.core

import us.frollo.frollosdk.logging.LogLevel

/**
 * Configuration of the SDK and additional optional preferences.
 *
 * Generates a valid OAuth2 SDK configuration for setting up the SDK. This configuration uses manually specified URLs the authorization and token endpoints the SDK should use.
 * Optional preferences can be set before running FrolloSDK.setup()
 */
data class FrolloSDKConfiguration(
        /**
         * OAuth2 Client identifier. The unique identifier of the application implementing the SDK
         */
        val clientId: String,
        /**
         * OAuth2 Redirection URI. URI to redirect to after the authorization flow is complete. This should be a deep or universal link to the host app
         */
        val redirectUri: String,
        /**
         * URL of the OAuth2 authorization endpoint for web based login
         */
        val authorizationUrl: String,
        /**
         * URL of the OAuth2 token endpoint for getting tokens and native login
         */
        val tokenUrl: String,
        /**
         * Base URL of the Frollo API this SDK should point to
         */
        val serverUrl: String,
        /**
         * Level of logging for debug and error messages. Default is [LogLevel.ERROR]
         */
        val logLevel: LogLevel = LogLevel.ERROR)