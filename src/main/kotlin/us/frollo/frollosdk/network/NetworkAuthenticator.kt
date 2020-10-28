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

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import us.frollo.frollosdk.error.APIError
import us.frollo.frollosdk.error.APIErrorType
import us.frollo.frollosdk.extensions.clonedBodyString
import us.frollo.frollosdk.network.NetworkHelper.Companion.HEADER_AUTHORIZATION

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

    override fun authenticate(route: Route?, response: Response?): Request? {

        var newRequest: Request? = null

        response?.clonedBodyString?.let { body ->
            val apiError = APIError(response.code(), body)

            when (apiError.type) {
                APIErrorType.INVALID_ACCESS_TOKEN -> {
                    if (network.invalidTokenRetries < 5) {
                        val accessToken = network.accessTokenProvider?.accessToken?.token

                        // Check if the request is using updated access token.
                        // If yes run refresh token flow
                        // Otherwise just run it once again with updated access token.
                        val authorization = response.request().header(HEADER_AUTHORIZATION)
                        if (accessToken != null && authorization == "Bearer $accessToken") {
                            network.authenticationCallback?.accessTokenExpired()
                        }

                        val newAccessToken = network.accessTokenProvider?.accessToken?.token
                        if (newAccessToken != null) {
                            newRequest = response.request().newBuilder()
                                .header(HEADER_AUTHORIZATION, "Bearer $newAccessToken")
                                .build()
                        }

                        // NOTE: No need for force logout in this block if newToken == null
                        // as the failures of authentication.refreshTokens() is handled
                        // in OAuth2Authentication or NetworkExtensions
                    }

                    network.invalidTokenRetries++
                }

                APIErrorType.INVALID_REFRESH_TOKEN,
                APIErrorType.SUSPENDED_DEVICE,
                APIErrorType.SUSPENDED_USER,
                APIErrorType.ACCOUNT_LOCKED,
                APIErrorType.OTHER_AUTHORISATION -> {
                    network.tokenInvalidated()
                }
                APIErrorType.SECURITY_CODE_REQUIRED,
                APIErrorType.INVALID_SECURITY_CODE -> {
                    return newRequest // For OTP errors, just return the error and do not force logout
                }
                else -> {
                    // Any other 401
                    network.tokenInvalidated()
                }
            }
        }

        return newRequest
    }
}
