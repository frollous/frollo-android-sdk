package us.frollo.frollosdk.network

import android.os.Build
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import us.frollo.frollosdk.auth.AuthToken
import us.frollo.frollosdk.base.LiveDataCallAdapterFactory
import us.frollo.frollosdk.network.api.DeviceAPI
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.model.api.user.TokenResponse
import us.frollo.frollosdk.preferences.Preferences
import okhttp3.CertificatePinner
import us.frollo.frollosdk.BuildConfig

class NetworkService internal constructor(internal val serverUrl: String, keystore: Keystore, pref: Preferences) : IApiProvider {

    companion object {
        private const val TAG = "NetworkService"
    }

    private val authToken = AuthToken(keystore, pref)
    private val helper = NetworkHelper(authToken)
    private val interceptor = NetworkInterceptor(this, helper)
    private var retrofit = createRetrofit()

    private fun createRetrofit(): Retrofit {
        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .enableComplexMapKeySerialization()
                .create()

        val httpClientBuilder = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .authenticator(NetworkAuthenticator(this))

        if (!BuildConfig.DEBUG && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            val certPinner = CertificatePinner.Builder()
                    .add("*.frollo.us", "sha256/XysGYqMH3Ml0kZoh6zTTaTzR4wYBGgUWfvbxgh4V4QA=")
                    .add("*.frollo.us", "sha256/UgMkdW5Xlo5dOndGZIdWLSrMu7DD3gwmnyqSOg+gz3I=")
                    .build()
            httpClientBuilder.certificatePinner(certPinner)
        }
        val httpClient = httpClientBuilder.build()

        val builder = Retrofit.Builder()
                .client(httpClient)
                .baseUrl(serverUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(LiveDataCallAdapterFactory)

        return builder.build()
    }

    override fun <T> create(service: Class<T>): T = retrofit.create(service)

    /**
     * Refreshes the authentication token
     * @return The new authentication token to be used
     */
    internal fun refreshTokens(): String? {
        val tokenEndpoint = create(DeviceAPI::class.java)
        val response = tokenEndpoint.refreshTokens().execute()
        return if (response.isSuccessful) {
            response.body()?.let { handleTokens(it) }
            response.body()?.accessToken
        } else {
            Log.e("$TAG#refreshTokens", "Refreshing token failed due to authorisation error.")
            null
        }
    }

    internal fun hasTokens() : Boolean =
            authToken.getAccessToken() != null && authToken.getRefreshToken() != null

    internal fun handleTokens(tokenResponse: TokenResponse) {
        authToken.saveTokens(tokenResponse)
    }

    internal fun authenticateRequest(request: Request): Request {
        return interceptor.authenticateRequest(request)
    }

    internal fun reset() {
        authToken.clearTokens()
    }
}