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
import okhttp3.CertificatePinner
import us.frollo.frollosdk.BuildConfig
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.authentication.AuthToken
import us.frollo.frollosdk.authentication.Authentication
import us.frollo.frollosdk.authentication.AuthenticationTokenCallback
import us.frollo.frollosdk.authentication.AuthenticationType.OAuth2
import us.frollo.frollosdk.authentication.OAuth2Helper
import us.frollo.frollosdk.preferences.Preferences

class NetworkService internal constructor(
    internal val oAuth2Helper: OAuth2Helper,
    keystore: Keystore,
    pref: Preferences
) : IApiProvider, AuthenticationTokenCallback {

    companion object {
        private const val TAG = "NetworkService"
        private const val PINNING_PATTERN = "*.frollo.us"
    }

    internal val authToken = AuthToken(keystore, pref)
    private val helper = NetworkHelper(authToken)
    private val serverInterceptor = NetworkInterceptor(this, helper)
    private val tokenInterceptor = TokenInterceptor(helper)
    private val apiRetrofit = createRetrofit(oAuth2Helper.config.serverUrl)
    private val authRetrofit: Retrofit?
        get() {
            return if (oAuth2Helper.config.authenticationType is OAuth2)
                createRetrofit(oAuth2Helper.oAuth2.tokenUrl)
            else null
        }
    private var revokeTokenRetrofit: Retrofit? = null
    internal var authentication: Authentication? = null
    internal var invalidTokenRetries: Int = 0

    init {
        if (oAuth2Helper.config.authenticationType is OAuth2) {
            oAuth2Helper.oAuth2.revokeTokenURL?.let { revokeTokenUrl ->
                revokeTokenRetrofit = createRetrofit(revokeTokenUrl)
            }
        }
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .enableComplexMapKeySerialization()
                .create()

        val httpClientBuilder = OkHttpClient.Builder()
                .addInterceptor(
                        if (oAuth2Helper.config.authenticationType is OAuth2 &&
                                (baseUrl == oAuth2Helper.oAuth2.tokenUrl || baseUrl == oAuth2Helper.oAuth2.revokeTokenURL))
                            tokenInterceptor
                        else
                            serverInterceptor)
                .authenticator(
                        if (oAuth2Helper.config.authenticationType is OAuth2 &&
                                (baseUrl == oAuth2Helper.oAuth2.tokenUrl || baseUrl == oAuth2Helper.oAuth2.revokeTokenURL))
                            TokenAuthenticator(this)
                        else
                            NetworkAuthenticator(this, helper))

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
    override fun <T> createAuth(service: Class<T>): T? = authRetrofit?.create(service)
    override fun <T> createRevoke(service: Class<T>): T? = revokeTokenRetrofit?.create(service)

    override fun saveAccessTokens(accessToken: String, expiry: Long) {
        authToken.saveAccessToken(accessToken)
        authToken.saveTokenExpiry(expiry)
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