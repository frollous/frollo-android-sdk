package us.frollo.frollosdk.data.remote

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_API_VERSION
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_AUTHORIZATION
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_BUNDLE_ID
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_DEVICE_VERSION
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_SOFTWARE_VERSION
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_USER_AGENT
import us.frollo.frollosdk.data.remote.api.TokenAPI.Companion.URL_TOKEN_REFRESH
import us.frollo.frollosdk.data.remote.api.UserAPI.Companion.URL_LOGIN
import us.frollo.frollosdk.data.remote.api.UserAPI.Companion.URL_REGISTER
import us.frollo.frollosdk.data.remote.api.UserAPI.Companion.URL_PASSWORD_RESET
import java.io.IOException

internal class NetworkInterceptor(private val helper: NetworkHelper) : Interceptor {

    companion object {
        private const val MAX_RATE_LIMIT_COUNT = 10
    }

    private var rateLimitCount = 0

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()

        // Add Header Auth for all requests except LOGIN
        if (!request.url().toString().contains(URL_LOGIN)) {
            addRequestAuthorizationHeader(request, builder)
        }
        addAdditionalHeaders(builder)

        val req = builder.build()

        var response = chain.proceed(req)

        //TODO: Review 429 Rate Limiting
        if (!response.isSuccessful && response.code() == 429) {
            Timber.d("Error Response 429: Too many requests. Backoff!")

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

    private fun addRequestAuthorizationHeader(request: Request, builder: Request.Builder) {
        val url = request.url().toString()
        if (request.headers().get(HEADER_AUTHORIZATION) == null) {
            if (url.contains(URL_REGISTER) || url.contains(URL_PASSWORD_RESET)) {
                builder.addHeader(HEADER_AUTHORIZATION, helper.otp)
            } else if (url.contains(URL_TOKEN_REFRESH)) {
                builder.addHeader(HEADER_AUTHORIZATION, helper.refreshToken)
            } else {
                //TODO: Validate and then append accessToken (refer iOS)
                builder.addHeader(HEADER_AUTHORIZATION, helper.accessToken)
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
}