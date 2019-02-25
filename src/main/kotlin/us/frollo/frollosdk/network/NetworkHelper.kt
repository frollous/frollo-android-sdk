package us.frollo.frollosdk.network

import android.os.Build
import us.frollo.frollosdk.BuildConfig
import us.frollo.frollosdk.authentication.AuthToken
import us.frollo.frollosdk.authentication.otp.OTP

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
        internal const val API_VERSION = "2.0"
        internal const val API_VERSION_PATH = "/api/v2"
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

    /**
     * Returns the temporary otp token formatted and ready for header authorization for registering the user.
     * @return "Bearer xxx.yyy.zzz"
     */
    internal val otp: String
        get() = "Bearer ${OTP.generateOTP(BuildConfig.APPLICATION_ID)}"
}