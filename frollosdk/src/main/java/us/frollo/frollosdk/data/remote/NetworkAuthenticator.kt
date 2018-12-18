package us.frollo.frollosdk.data.remote

import okhttp3.*
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.HEADER_AUTHORIZATION
import us.frollo.frollosdk.error.APIError
import us.frollo.frollosdk.error.APIErrorType

/**
 * Responds to an authentication challenge from the web server.
 * Implementation may either attempt to satisfy the challenge by
 * returning a request that includes an authorization header,
 * or they may refuse the challenge by returning null.
 *
 * In this case the unauthenticated response will be returned to the caller that triggered it.
 * Implementations should check if the initial request already included an attempt to authenticate.
 * If so it is likely that further attempts will not be useful and the authenticator should give up.
 * When authentication is requested by an origin server, the response code is 401 and the
 * implementation should respond with a new request that sets the "Authorization" header.
 */
internal class NetworkAuthenticator(private val network: NetworkService) : Authenticator {

    override fun authenticate(route: Route, response: Response): Request? {
        // TODO: Review this
        /*if (response.request().header(HEADER_AUTHORIZATION) != null) {
            return null // Give up, we've already failed to authenticate.
        }*/

        var newRequest: Request? = null
        response.body()?.string()?.let { body ->
            val apiError = APIError(response.code(), body)
            when (apiError.type) {
                APIErrorType.INVALID_ACCESS_TOKEN -> {
                    val newToken = network.refreshTokens()
                    newRequest = response.request().newBuilder()
                            .header(HEADER_AUTHORIZATION, "Bearer $newToken")
                            .build()
                }
                APIErrorType.INVALID_REFRESH_TOKEN, APIErrorType.SUSPENDED_DEVICE, APIErrorType.SUSPENDED_USER, APIErrorType.OTHER_AUTHORISATION -> {
                    FrolloSDK.forcedLogout()
                }
                else -> {
                    // Do nothing
                }
            }

        }
        return newRequest
    }
}