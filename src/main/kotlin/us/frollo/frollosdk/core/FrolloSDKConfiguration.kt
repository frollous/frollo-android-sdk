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

package us.frollo.frollosdk.core

import android.net.Uri
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
     * OAuth2 Redirection URL. URL to redirect to after the authorization flow is complete. This should be a deep or universal link to the host app
     */
    val redirectUrl: String,
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
    val logLevel: LogLevel = LogLevel.ERROR
) {

    internal fun validForROPC() = clientId.isNotBlank() && tokenUrl.isNotBlank() && serverUrl.isNotBlank()

    internal fun validForAuthorizationCodeFlow() = clientId.isNotBlank() && tokenUrl.isNotBlank() &&
            redirectUrl.isNotBlank() && authorizationUrl.isNotBlank() && serverUrl.isNotBlank()

    /**
     * OAuth2 Redirection URL. URL to redirect to after the authorization flow is complete. This should be a deep or universal link to the host app
     */
    val redirectUri: Uri = Uri.parse(redirectUrl)
    /**
     * URL of the OAuth2 authorization endpoint for web based login
     */
    val authorizationUri: Uri = Uri.parse(authorizationUrl)
    /**
     * URL of the OAuth2 token endpoint for getting tokens and native login
     */
    val tokenUri: Uri = Uri.parse(tokenUrl)
}