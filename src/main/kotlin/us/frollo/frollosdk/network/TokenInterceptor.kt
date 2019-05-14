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

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import us.frollo.frollosdk.logging.Log
import java.io.IOException

internal class TokenInterceptor(private val helper: NetworkHelper) : Interceptor {

    companion object {
        private const val TAG = "TokenInterceptor"

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

        return response
    }

    private fun adaptRequest(originalRequest: Request): Request {
        val builder = originalRequest.newBuilder()
        helper.addAdditionalHeaders(builder)
        return builder.build()
    }
}