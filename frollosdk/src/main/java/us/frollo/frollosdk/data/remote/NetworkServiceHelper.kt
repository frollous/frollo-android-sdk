package us.frollo.frollosdk.data.remote

import us.frollo.frollosdk.core.SystemInfo

/**
 * This class wraps the rules for interacting with the Frollo API such as the headers used, API version and so on
 */
internal class NetworkServiceHelper(private val systemInfo: SystemInfo) {
    companion object {
        const val HEADER_USER_AGENT = "User-Agent"
        private const val API_VERSION = "1.16"
    }

    /**
     * Creates the User-Agent header value for standard API requests.
     * @return a [String] with actual values to "Bundle Identifier|Software Version|Build|OS Version|API"
     */
    //"us.frollo.frollo.sandbox|v1.1|b777|Android8.1.0|API1.11"
    fun getUserAgent(): String =
            "${systemInfo.packageName}|V${systemInfo.appVersion}|B${systemInfo.appCode}|Android${systemInfo.osVersionName}|API$API_VERSION"
}