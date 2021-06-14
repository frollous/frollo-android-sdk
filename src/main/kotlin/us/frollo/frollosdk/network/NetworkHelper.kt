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
import us.frollo.frollosdk.core.AppInfo

/**
 * This class wraps the rules for interacting with the Frollo API such as the headers used, API version and so on
 */
internal class NetworkHelper(private val appInfo: AppInfo) {

    companion object {
        internal const val HEADER_AUTHORIZATION = "Authorization"
        internal const val HEADER_USER_AGENT = "User-Agent"
        internal const val HEADER_BUNDLE_ID = "X-Bundle-Id"
        internal const val HEADER_SOFTWARE_VERSION = "X-Software-Version"
        internal const val HEADER_DEVICE_VERSION = "X-Device-Version"
        internal const val HEADER_API_VERSION = "X-Api-Version"
        internal const val HEADER_BACKGROUND = "X-Background"
        internal const val HEADER_OTP = "X-User-Otp"
        internal const val API_VERSION = "2.14"
    }

    internal val bundleId: String
        get() = BuildConfig.LIBRARY_PACKAGE_NAME

    internal val softwareVersion: String
        get() = "SDK${BuildConfig.SDK_VERSION_NAME}-B${BuildConfig.SDK_VERSION_CODE}|APP${appInfo.versionNumber}-B${appInfo.versionCode}"

    internal val deviceVersion: String
        get() = "Android${Build.VERSION.RELEASE}"

    // "us.frollo.frollosdk|SDK1.0.0|B777|Android8.1.0|API2.0"
    @Deprecated("Not needed anymore by the Host on v2 APIs")
    internal val userAgent: String
        get() = "${BuildConfig.LIBRARY_PACKAGE_NAME}|SDK${BuildConfig.SDK_VERSION_NAME}|B${BuildConfig.SDK_VERSION_CODE}|Android${Build.VERSION.RELEASE}|API$API_VERSION"

    internal fun addAdditionalHeaders(builder: Request.Builder) {
        builder.removeHeader(HEADER_API_VERSION).addHeader(HEADER_API_VERSION, API_VERSION)
        builder.removeHeader(HEADER_BUNDLE_ID).addHeader(HEADER_BUNDLE_ID, bundleId)
        builder.removeHeader(HEADER_DEVICE_VERSION).addHeader(HEADER_DEVICE_VERSION, deviceVersion)
        builder.removeHeader(HEADER_SOFTWARE_VERSION).addHeader(HEADER_SOFTWARE_VERSION, softwareVersion)
        builder.removeHeader(HEADER_USER_AGENT).addHeader(HEADER_USER_AGENT, userAgent)
    }
}
