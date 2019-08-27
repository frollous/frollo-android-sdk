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

import android.net.Uri
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.network.NetworkHelper.Companion.HEADER_AUTHORIZATION
import us.frollo.frollosdk.network.api.UserAPI.Companion.URL_REGISTER
import us.frollo.frollosdk.network.api.UserAPI.Companion.URL_PASSWORD_RESET
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.extensions.toJson
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.network.api.UserAPI.Companion.URL_MIGRATE_USER
import java.io.IOException

internal class NetworkInterceptor(private val network: NetworkService, private val helper: NetworkHelper) : Interceptor {

    companion object {
        private const val TAG = "NetworkInterceptor"

        private const val MAX_RATE_LIMIT_COUNT = 10
    }

    private var rateLimitCount = 0

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = adaptRequest(chain.request())

        var response = chain.proceed(request)

        // TODO: Review 429 Rate Limiting
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

        if (response.isSuccessful)
            network.invalidTokenRetries = 0

        return response
    }

    internal fun adaptRequest(originalRequest: Request): Request {
        val builder = originalRequest.newBuilder()

        try {
            if (originalRequest.url().host() == Uri.parse(network.oAuth2Helper.config.serverUrl).host) { // Precautionary check to not append headers for any external requests
                addAuthorizationHeader(originalRequest, builder)
                helper.addAdditionalHeaders(builder)
            }
        } catch (error: DataError) {
            if (error.type == DataErrorType.AUTHENTICATION && error.subType == DataErrorSubType.MISSING_REFRESH_TOKEN)
                network.tokenInvalidated()
            throw IOException(error.toJson())
        }

        return builder.build()
    }

    private fun addAuthorizationHeader(request: Request, builder: Request.Builder) {
        val url = request.url().toString()
        if (request.headers().get(HEADER_AUTHORIZATION) == null &&
                url.contains(URL_MIGRATE_USER)) {

            appendRefreshToken(builder)
        } else if (request.headers().get(HEADER_AUTHORIZATION) == null &&
                !url.contains(URL_REGISTER) &&
                !url.contains(URL_PASSWORD_RESET)) {

            validateAndAppendAccessToken(builder)
        }
    }

    fun authenticateRequest(request: Request): Request {
        val builder = request.newBuilder()
        validateAndAppendAccessToken(builder)
        return builder.build()
    }

    @Throws(DataError::class)
    private fun validateAndAppendAccessToken(builder: Request.Builder) {
        if (!validAccessToken())
            network.authenticationCallback?.accessTokenExpired()

        network.accessTokenProvider?.accessToken?.token?.let {
            builder.addHeader(HEADER_AUTHORIZATION, "Bearer $it")
        } ?: run {
            throw DataError(DataErrorType.AUTHENTICATION, DataErrorSubType.MISSING_ACCESS_TOKEN)
        }
    }

    private fun validAccessToken(): Boolean {
        // Check we have an access token
        if (network.accessTokenProvider?.accessToken == null)
            return false

        // Check if we have an expiry date otherwise assume it's still good
        val expiry = network.accessTokenProvider?.accessToken?.expiry
        if (expiry == null || expiry < 0L)
            return true

        val preemptiveRefreshTime = if (network.oAuth2Helper.config.preemptiveRefreshTime < 0L) 0
        else network.oAuth2Helper.config.preemptiveRefreshTime
        val expiryDate = LocalDateTime.ofEpochSecond(expiry, 0, ZoneOffset.UTC)
        val adjustedExpiryDate = expiryDate.plusSeconds(-preemptiveRefreshTime)
        val nowDate = LocalDateTime.now(ZoneOffset.UTC)

        return nowDate.isBefore(adjustedExpiryDate)
    }

    @Throws(DataError::class)
    private fun appendRefreshToken(builder: Request.Builder) {
        val refreshToken = network.authToken.getRefreshToken()
        refreshToken?.let {
            builder.addHeader(HEADER_AUTHORIZATION, "Bearer $it")
        } ?: run {
            Log.e("$TAG#validateAndAppendRefreshToken", "No valid refresh token when trying to migrate user to Auth0.")
            network.tokenInvalidated()
            throw DataError(DataErrorType.AUTHENTICATION, DataErrorSubType.MISSING_REFRESH_TOKEN)
        }
    }
}