package us.frollo.frollosdk.data.remote

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_API_VERSION
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_AUTHORIZATION
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_BUNDLE_ID
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_DEVICE_VERSION
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_SOFTWARE_VERSION
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_USER_AGENT
import us.frollo.frollosdk.data.remote.api.TokenAPI.Companion.URL_TOKEN_REFRESH
import us.frollo.frollosdk.data.remote.api.UserAPI.Companion.URL_LOGIN
import us.frollo.frollosdk.data.remote.api.UserAPI.Companion.URL_REGISTER
import us.frollo.frollosdk.data.remote.api.UserAPI.Companion.URL_USER_RESET
import java.io.IOException

internal class NetworkInterceptor(private val helper: NetworkHelper) : Interceptor {

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

        val response = chain.proceed(req)

        if (!response.isSuccessful)
            handleFailure(response)

        return response
    }

    private fun addRequestAuthorizationHeader(request: Request, builder: Request.Builder) {
        val url = request.url().toString()
        if (request.headers().get(HEADER_AUTHORIZATION) == null) {
            if (url.contains(URL_REGISTER) || url.contains(URL_USER_RESET)) {
                builder.addHeader(HEADER_AUTHORIZATION, helper.otp)
            } else if (url.contains(URL_TOKEN_REFRESH)) {
                builder.addHeader(HEADER_AUTHORIZATION, helper.refreshToken)
            } else {
                //TODO: Validate and then append accessToken
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

    private fun handleFailure(response: Response) {
        when (response.code()) {
            429 -> {
                // TODO: Handle rate limit
            }
        }
    }
}