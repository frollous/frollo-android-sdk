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
        private const val API_VERSION = "1.17"
    }

    internal val accessToken: String
        get() = "Bearer ${authToken.getAccessToken()}"

    internal val refreshToken: String
        get() = "Bearer ${authToken.getRefreshToken()}"

    /**
     * Creates the User-Agent header value for standard API requests.
     * @return a [String] with actual values to "Bundle Identifier|Software Version|Build|OS Version|API"
     */
    // "us.frollo.frollosdk|v1.0.0|b777|Android8.1.0|API1.11"
    internal val userAgent: String
        get() = "${BuildConfig.APPLICATION_ID}|V${BuildConfig.VERSION_NAME}|B${BuildConfig.VERSION_CODE}|Android${systemInfo.osVersionName}|API$API_VERSION"

    /**
     * Returns the temporary otp token formatted and ready for header authorization for registering the user.
     * @return "Bearer xxx.yyy.zzz"
     */
    internal val otp: String
        get() = "Bearer ${OTP().otp()}"

}