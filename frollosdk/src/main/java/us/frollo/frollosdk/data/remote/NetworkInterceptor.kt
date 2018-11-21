package us.frollo.frollosdk.data.remote

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_AUTHORIZATION
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_USER_AGENT
import us.frollo.frollosdk.data.remote.endpoints.TokenEndpoint.Companion.URL_TOKEN_REFRESH
import us.frollo.frollosdk.data.remote.endpoints.UserEndpoint.Companion.URL_LOGIN
import us.frollo.frollosdk.data.remote.endpoints.UserEndpoint.Companion.URL_REGISTER
import us.frollo.frollosdk.data.remote.endpoints.UserEndpoint.Companion.URL_USER_RESET
import us.frollo.frollosdk.error.APIError
import us.frollo.frollosdk.error.APIErrorType.*
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
        addRequestUserAgentHeader(builder)

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

    private fun addRequestUserAgentHeader(builder: Request.Builder) {
        builder.removeHeader(HEADER_USER_AGENT) //Hack: Remove default User-Agent when present
                .addHeader(HEADER_USER_AGENT, helper.userAgent)
    }

    private fun handleFailure(response: Response) {
        when (response.code()) {
            401 -> {
                response.body()?.string()?.let { body ->
                    val apiError = APIError(response.code(), body)
                    when (apiError.type) {
                        INVALID_REFRESH_TOKEN, SUSPENDED_DEVICE, SUSPENDED_USER, OTHER_AUTHORISATION -> {
                            FrolloSDK.forcedLogout()
                        }
                        else -> {
                            // Do nothing
                        }
                    }
                }
            }
            429 -> {
                // TODO: Handle rate limit
            }
        }
    }
}