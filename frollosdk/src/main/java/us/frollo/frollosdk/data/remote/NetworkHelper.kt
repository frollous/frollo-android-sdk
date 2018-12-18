package us.frollo.frollosdk.data.remote

import us.frollo.frollosdk.BuildConfig
import us.frollo.frollosdk.auth.AuthToken
import us.frollo.frollosdk.auth.otp.OTP
import us.frollo.frollosdk.core.SystemInfo

/**
 * This class wraps the rules for interacting with the Frollo API such as the headers used, API version and so on
 */
internal class NetworkHelper(private val systemInfo: SystemInfo, private val authToken: AuthToken) {

    companion object {
        internal const val HEADER_AUTHORIZATION = "Authorization"
        internal const val HEADER_USER_AGENT = "User-Agent"
        internal const val HEADER_BUNDLE_ID = "X-Bundle-Id"
        internal const val HEADER_SOFTWARE_VERSION = "X-Software-Version"
        internal const val HEADER_DEVICE_VERSION = "X-Device-Version"
        internal const val HEADER_API_VERSION = "X-Api-Version"
        internal const val HEADER_BACKGROUND = "X-Background"
        internal const val API_VERSION = "2.0"
    }

    internal val accessToken: String
        get() = "Bearer ${authToken.getAccessToken()}"

    internal val refreshToken: String
        get() = "Bearer ${authToken.getRefreshToken()}"

    internal val bundleId: String
        get() = BuildConfig.APPLICATION_ID

    internal val softwareVersion: String
        get() = "V${BuildConfig.VERSION_NAME}-B${BuildConfig.VERSION_CODE}"

    internal val deviceVersion: String
        get() = "Android${systemInfo.osVersionName}"

    //TODO: Check with Edd if we have to send "SDK" instead of "V" for version string
    // "us.frollo.frollosdk|V1.0.0|B777|Android8.1.0|API2.0"
    internal val userAgent: String
        get() = "${BuildConfig.APPLICATION_ID}|V${BuildConfig.VERSION_NAME}|B${BuildConfig.VERSION_CODE}|Android${systemInfo.osVersionName}|API$API_VERSION"

    /**
     * Returns the temporary otp token formatted and ready for header authorization for registering the user.
     * @return "Bearer xxx.yyy.zzz"
     */
    internal val otp: String
        get() = "Bearer ${OTP().otp()}"

}