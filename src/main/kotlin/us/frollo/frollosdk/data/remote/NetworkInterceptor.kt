package us.frollo.frollosdk.data.remote

import android.net.Uri
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_API_VERSION
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_AUTHORIZATION
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_BUNDLE_ID
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_DEVICE_VERSION
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_SOFTWARE_VERSION
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_USER_AGENT
import us.frollo.frollosdk.data.remote.api.DeviceAPI.Companion.URL_TOKEN_REFRESH
import us.frollo.frollosdk.data.remote.api.UserAPI.Companion.URL_LOGIN
import us.frollo.frollosdk.data.remote.api.UserAPI.Companion.URL_REGISTER
import us.frollo.frollosdk.data.remote.api.UserAPI.Companion.URL_PASSWORD_RESET
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.extensions.toJson
import us.frollo.frollosdk.logging.Log
import java.io.IOException

internal class NetworkInterceptor(private val network: NetworkService, private val helper: NetworkHelper) : Interceptor {

    companion object {
        private const val TAG = "NetworkInterceptor"

        private const val MAX_RATE_LIMIT_COUNT = 10
        private const val TIME_INTERVAL_5_MINUTES = 300L //seconds
    }

    private var rateLimitCount = 0

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()

        try {
            if (request?.url()?.host() == Uri.parse(network.serverUrl).host) { // Precautionary check to not append headers for any external requests
                addAuthorizationHeader(request, builder)
                addAdditionalHeaders(builder)
            }
        } catch (error: DataError) {
            if (error.type == DataErrorType.AUTHENTICATION && error.subType == DataErrorSubType.MISSING_REFRESH_TOKEN)
                FrolloSDK.forcedLogout()
            throw IOException(error.toJson())
        }

        val req = builder.build()

        var response = chain.proceed(req)

        //TODO: Review 429 Rate Limiting
        if (!response.isSuccessful && response.code() == 429) {
            Log.e("$TAG#intercept", "Error Response 429: Too many requests. Backoff!")

            // wait & retry
            try {
                rateLimitCount = Math.min(rateLimitCount + 1, MAX_RATE_LIMIT_COUNT)
                val sleepTime = (rateLimitCount * 3) * 1000L
                Thread.sleep(sleepTime)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            response = chain.proceed(chain.request())
        }

        return response
    }

    private fun addAuthorizationHeader(request: Request, builder: Request.Builder) {
        val url = request.url().toString()
        if (request.headers().get(HEADER_AUTHORIZATION) == null) {
            if (url.contains(URL_REGISTER) || url.contains(URL_PASSWORD_RESET)) {
                // OTP auth header for register & password reset
                appendOTP(builder)
            } else if (url.contains(URL_TOKEN_REFRESH)) {
                appendRefreshToken(builder)
            } else if (!url.contains(URL_LOGIN)) { // No auth header for login
                validateAndAppendAccessToken(builder)
            }
        }
    }

    private fun addAdditionalHeaders(builder: Request.Builder) {
        builder.removeHeader(HEADER_API_VERSION).addHeader(HEADER_API_VERSION, NetworkHelper.API_VERSION)
        builder.removeHeader(HEADER_BUNDLE_ID).addHeader(HEADER_BUNDLE_ID, helper.bundleId)
        builder.removeHeader(HEADER_DEVICE_VERSION).addHeader(HEADER_DEVICE_VERSION, helper.deviceVersion)
        builder.removeHeader(HEADER_SOFTWARE_VERSION).addHeader(HEADER_SOFTWARE_VERSION, helper.softwareVersion)
        builder.removeHeader(HEADER_USER_AGENT).addHeader(HEADER_USER_AGENT, helper.userAgent)
    }

    private fun appendOTP(builder: Request.Builder) {
        builder.addHeader(HEADER_AUTHORIZATION, helper.otp)
    }

    @Throws(DataError::class)
    private fun appendRefreshToken(builder: Request.Builder) {
        helper.authRefreshToken?.let {
            builder.addHeader(HEADER_AUTHORIZATION, it)
        } ?: run {
            throw DataError(DataErrorType.AUTHENTICATION, DataErrorSubType.MISSING_REFRESH_TOKEN)
        }
    }

    fun authenticateRequest(request: Request): Request {
        val builder = request.newBuilder()
        validateAndAppendAccessToken(builder)
        return builder.build()
    }

    @Throws(DataError::class)
    private fun validateAndAppendAccessToken(builder: Request.Builder) {
        if (helper.authRefreshToken == null) {
            Log.e("$TAG#validateAndAppendAccessToken", "No valid refresh token when trying to refresh access token.")
            throw DataError(DataErrorType.AUTHENTICATION, DataErrorSubType.MISSING_REFRESH_TOKEN)
        }

        if (!validAccessToken())
            network.refreshTokens()

        helper.authAccessToken?.let {
            builder.addHeader(HEADER_AUTHORIZATION, it)
        } ?: run {
            throw DataError(DataErrorType.AUTHENTICATION, DataErrorSubType.MISSING_ACCESS_TOKEN)
        }
    }

    private fun validAccessToken(): Boolean {
        if (helper.accessTokenExpiry == -1L)
            return false

        val expiryDate = LocalDateTime.ofEpochSecond(helper.accessTokenExpiry, 0, ZoneOffset.UTC)
        val adjustedExpiryDate = expiryDate.plusSeconds(-TIME_INTERVAL_5_MINUTES)
        val nowDate = LocalDateTime.now(ZoneOffset.UTC)

        return nowDate.isBefore(adjustedExpiryDate)
    }
}