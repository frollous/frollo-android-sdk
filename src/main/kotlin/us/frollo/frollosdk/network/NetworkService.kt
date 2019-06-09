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

import android.os.Build
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import us.frollo.frollosdk.base.LiveDataCallAdapterFactory
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.logging.Log
import okhttp3.CertificatePinner
import us.frollo.frollosdk.BuildConfig
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.authentication.AuthToken
import us.frollo.frollosdk.authentication.OAuth
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.error.OAuth2Error
import us.frollo.frollosdk.extensions.handleOAuth2Failure
import us.frollo.frollosdk.model.oauth.OAuthTokenResponse
import us.frollo.frollosdk.network.api.TokenAPI
import us.frollo.frollosdk.preferences.Preferences

class NetworkService internal constructor(
    internal val oAuth: OAuth,
    keystore: Keystore,
    pref: Preferences
) : IApiProvider {

    companion object {
        private const val TAG = "NetworkService"
        private const val PINNING_PATTERN = "*.frollo.us"
    }

    internal val authToken = AuthToken(keystore, pref)
    private val helper = NetworkHelper(authToken)
    private val serverInterceptor = NetworkInterceptor(this, helper)
    private val tokenInterceptor = TokenInterceptor(helper)
    private var apiRetrofit = createRetrofit(oAuth.config.serverUrl)
    private var authRetrofit = createRetrofit(oAuth.config.tokenUrl)
    private var revokeTokenRetrofit: Retrofit? = null

    internal var invalidTokenRetries: Int = 0

    init {
        oAuth.config.revokeTokenURL?.let { revokeTokenUrl ->
            revokeTokenRetrofit = createRetrofit(revokeTokenUrl)
        }
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .enableComplexMapKeySerialization()
                .create()

        val httpClientBuilder = OkHttpClient.Builder()
                .addInterceptor(
                        if (baseUrl == oAuth.config.tokenUrl || baseUrl == oAuth.config.revokeTokenURL)
                            tokenInterceptor
                        else
                            serverInterceptor)
                .authenticator(
                        if (baseUrl == oAuth.config.tokenUrl || baseUrl == oAuth.config.revokeTokenURL)
                            TokenAuthenticator(this)
                        else
                            NetworkAuthenticator(this))

        if (!BuildConfig.DEBUG && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            val certPinner = CertificatePinner.Builder()
                    .add(PINNING_PATTERN, PublicKey.ACTIVE)
                    .add(PINNING_PATTERN, PublicKey.BACKUP)
                    .build()
            httpClientBuilder.certificatePinner(certPinner)
        }
        val httpClient = httpClientBuilder.build()

        val builder = Retrofit.Builder()
                .client(httpClient)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(LiveDataCallAdapterFactory)

        return builder.build()
    }

    override fun <T> create(service: Class<T>): T = apiRetrofit.create(service)
    override fun <T> createAuth(service: Class<T>): T = authRetrofit.create(service)
    override fun <T> createRevoke(service: Class<T>): T? = revokeTokenRetrofit?.create(service)

    /**
     * Refreshes the authentication token
     * @return The new authentication token to be used
     */
    internal fun refreshTokens(completion: OnFrolloSDKCompletionListener<Result>? = null): String? {
        val request = oAuth.getRefreshTokensRequest(authToken.getRefreshToken())
        val tokenEndpoint = createAuth(TokenAPI::class.java)

        val response = tokenEndpoint.refreshTokens(request).execute()
        val apiResponse = ApiResponse(response)

        if (apiResponse.isSuccessful) {
            apiResponse.body?.let { handleTokens(it) }

            completion?.invoke(Result.success())

            return apiResponse.body?.accessToken
        } else {
            val error = OAuth2Error(response = apiResponse.errorMessage)

            Log.e("$TAG#refreshTokens", error.localizedMessage)

            handleOAuth2Failure(error)

            completion?.invoke(Result.error(error))

            return null
        }
    }

    internal fun hasTokens(): Boolean =
            authToken.getAccessToken() != null && authToken.getRefreshToken() != null

    internal fun handleTokens(tokenResponse: OAuthTokenResponse) {
        authToken.saveTokens(tokenResponse)
    }

    internal fun authenticateRequest(request: Request): Request {
        return serverInterceptor.authenticateRequest(request)
    }

    internal fun reset() {
        invalidTokenRetries = 0
        authToken.clearTokens()
    }

    internal fun triggerForcedLogout() {
        reset()
        if (FrolloSDK.isSetup) FrolloSDK.forcedLogout()
    }
}