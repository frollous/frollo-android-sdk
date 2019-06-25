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

package us.frollo.frollosdk.network

import android.os.Build
import okhttp3.Request
import us.frollo.frollosdk.BuildConfig
import us.frollo.frollosdk.authentication.AuthToken

/**
 * This class wraps the rules for interacting with the Frollo API such as the headers used, API version and so on
 */
internal class NetworkHelper(private val authToken: AuthToken) {

    companion object {
        internal const val HEADER_AUTHORIZATION = "Authorization"
        internal const val HEADER_USER_AGENT = "User-Agent"
        internal const val HEADER_BUNDLE_ID = "X-Bundle-Id"
        internal const val HEADER_SOFTWARE_VERSION = "X-Software-Version"
        internal const val HEADER_DEVICE_VERSION = "X-Device-Version"
        internal const val HEADER_API_VERSION = "X-Api-Version"
        internal const val HEADER_BACKGROUND = "X-Background"
        internal const val API_VERSION = "2.4"
    }

    internal val authAccessToken: String?
        get() {
            val token = authToken.getAccessToken()
            return if (token == null) null else "Bearer $token"
        }

    internal val authRefreshToken: String?
        get() {
            val token = authToken.getRefreshToken()
            return if (token == null) null else "Bearer $token"
        }

    internal val accessTokenExpiry: Long
        get() = authToken.getAccessTokenExpiry()

    internal val bundleId: String
        get() = BuildConfig.APPLICATION_ID

    internal val softwareVersion: String
        get() = "V${BuildConfig.VERSION_NAME}-B${BuildConfig.VERSION_CODE}"

    internal val deviceVersion: String
        get() = "Android${Build.VERSION.RELEASE}"

    // "us.frollo.frollosdk|SDK1.0.0|B777|Android8.1.0|API2.0"
    internal val userAgent: String
        get() = "${BuildConfig.APPLICATION_ID}|SDK${BuildConfig.VERSION_NAME}|B${BuildConfig.VERSION_CODE}|Android${Build.VERSION.RELEASE}|API$API_VERSION"

    internal fun addAdditionalHeaders(builder: Request.Builder) {
        builder.removeHeader(NetworkHelper.HEADER_API_VERSION).addHeader(NetworkHelper.HEADER_API_VERSION, NetworkHelper.API_VERSION)
        builder.removeHeader(NetworkHelper.HEADER_BUNDLE_ID).addHeader(NetworkHelper.HEADER_BUNDLE_ID, bundleId)
        builder.removeHeader(NetworkHelper.HEADER_DEVICE_VERSION).addHeader(NetworkHelper.HEADER_DEVICE_VERSION, deviceVersion)
        builder.removeHeader(NetworkHelper.HEADER_SOFTWARE_VERSION).addHeader(NetworkHelper.HEADER_SOFTWARE_VERSION, softwareVersion)
        builder.removeHeader(NetworkHelper.HEADER_USER_AGENT).addHeader(NetworkHelper.HEADER_USER_AGENT, userAgent)
    }
}