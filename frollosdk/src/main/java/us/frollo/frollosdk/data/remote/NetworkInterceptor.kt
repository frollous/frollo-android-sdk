package us.frollo.frollosdk.data.remote

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

internal class NetworkInterceptor(private val serviceHelper: NetworkServiceHelper) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()

        // TODO: Add Header Auth for all requests except LOGIN
        addRequestUserAgentHeader(request, builder)

        val req = builder.build()

        return chain.proceed(req)
    }

    private fun addRequestUserAgentHeader(request: Request, builder: Request.Builder) {
        builder.removeHeader(NetworkServiceHelper.HEADER_USER_AGENT) //Hack: Remove default User-Agent when present
                .addHeader(NetworkServiceHelper.HEADER_USER_AGENT, serviceHelper.getUserAgent())
    }
}