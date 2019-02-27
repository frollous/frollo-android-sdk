package us.frollo.frollosdk.network

import okhttp3.Interceptor
import okhttp3.Response
import us.frollo.frollosdk.logging.Log
import java.io.IOException

internal class TokenInterceptor : Interceptor {

    companion object {
        private const val TAG = "TokenInterceptor"

        private const val MAX_RATE_LIMIT_COUNT = 10
    }

    private var rateLimitCount = 0

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        var response = chain.proceed(request)

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
}