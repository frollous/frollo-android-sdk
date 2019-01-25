package us.frollo.frollosdk.data.remote

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import us.frollo.frollosdk.auth.AuthToken
import us.frollo.frollosdk.base.LiveDataCallAdapterFactory
import us.frollo.frollosdk.data.remote.api.DeviceAPI
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.model.api.user.TokenResponse
import us.frollo.frollosdk.preferences.Preferences

class NetworkService(internal val serverUrl: String, keystore: Keystore, pref: Preferences) : IApiProvider {

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

        val httpClient = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .authenticator(NetworkAuthenticator(this))
                .build()

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