package us.frollo.frollosdk.data.remote

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.auth.AuthToken
import us.frollo.frollosdk.base.LiveDataCallAdapterFactory
import us.frollo.frollosdk.data.remote.endpoints.TokenEndpoint
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.model.api.user.TokenResponse
import us.frollo.frollosdk.preferences.Preferences
import java.util.concurrent.TimeUnit

class NetworkService(keystore: Keystore, pref: Preferences) : IApiProvider {

    private val authToken = AuthToken(keystore, pref)
    private val helper = NetworkHelper(authToken)
    private val interceptor = NetworkInterceptor(helper)
    private val authenticator = NetworkAuthenticator(this)

    private var retrofit: Retrofit

    init {
        retrofit = createRetrofit()
    }

    private fun createRetrofit(): Retrofit {
        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .enableComplexMapKeySerialization()
                .create()

        val httpClient = OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .addInterceptor(interceptor)
                .authenticator(authenticator)
                .build()

        val builder = Retrofit.Builder()
                .client(httpClient)
                .baseUrl(FrolloSDK.serverUrl)
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
        val tokenEndpoint = create(TokenEndpoint::class.java)
        val response = tokenEndpoint.refreshTokens().execute()
        return if (response.isSuccessful) {
            response.body()?.let { handleTokens(it) }
            response.body()?.accessToken
        } else {
            null
        }
    }

    internal fun handleTokens(tokenResponse: TokenResponse) {
        authToken.saveTokens(tokenResponse)
    }

    internal fun reset() {
        authToken.clearTokens()
    }
}