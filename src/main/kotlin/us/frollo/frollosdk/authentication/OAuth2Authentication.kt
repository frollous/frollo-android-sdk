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

package us.frollo.frollosdk.authentication

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.ACTION
import us.frollo.frollosdk.core.ARGUMENT
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.error.OAuth2Error
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.handleOAuth2Failure
import us.frollo.frollosdk.extensions.notify
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.model.oauth.OAuth2Scope
import us.frollo.frollosdk.model.oauth.OAuthGrantType
import us.frollo.frollosdk.model.oauth.OAuthTokenRequest
import us.frollo.frollosdk.model.oauth.OAuthTokenResponse
import us.frollo.frollosdk.network.ApiResponse
import us.frollo.frollosdk.network.ErrorResponseType
import us.frollo.frollosdk.network.api.TokenAPI
import us.frollo.frollosdk.preferences.Preferences

/**
 * Manages authentication, login, registration, logout and the user profile.
 */
class OAuth2Authentication(
    internal val oAuth2Helper: OAuth2Helper,
    private val pref: Preferences
) : AccessTokenProvider, AuthenticationCallback {

    companion object {
        private const val TAG = "OAuth2Authentication"

        /** Request Code for the Authorization Intent for Web based login */
        const val RC_AUTH = 100
    }

    /**
     * Indicates if the user is currently authorised with Frollo
     */
    var loggedIn: Boolean
        get() = pref.loggedIn
        private set(value) { pref.loggedIn = value }

    internal var tokenAPI: TokenAPI? = null
    internal var revokeTokenAPI: TokenAPI? = null
    internal var authToken: AuthToken? = null

    private var codeVerifier: String? = null
    private var isRefreshingToken = false

    /**
     * Access token to use for authorization
     */
    override val accessToken: AccessToken?
        get() = authToken?.getAccessToken()?.let { AccessToken(token = it, expiry = authToken?.getAccessTokenExpiry()) } ?: run { null }

    /**
     * Time in seconds the access token should pre-emptively be refreshed before it expires
     */
    override val preemptiveRefreshTime: Long = 180 // 3 minutes

    /**
     * Login a user via WebView
     *
     * Initiate the authorization code login flow using a WebView
     *
     * @param activity Activity from which the ChromeTabs/Browser should be launched
     * @param scopes OpenID Connect OAuth2 scopes to be sent. See [OAuth2Scope].
     * @param additionalParameters Pass additional query parameters to the authorization endpoint (Optional)
     * @param completedIntent PendingIntent of an Activity to which the completed response from the ChromeTabs/Browser is delivered
     * @param cancelledIntent PendingIntent of an Activity to which the cancelled response from the ChromeTabs/Browser is delivered
     * @param toolBarColor Color of the CustomTabs toolbar using getColor() method
     *
     * NOTE: When using this method you need to call [handleWebLoginResponse]
     * in the onCreate() of the pending intent activity
     */
    @Throws(DataError::class)
    fun loginUserUsingWeb(
        activity: Activity,
        scopes: List<String>,
        additionalParameters: Map<String, String>? = null,
        completedIntent: PendingIntent,
        cancelledIntent: PendingIntent,
        toolBarColor: Int? = null
    ) {

        if (!oAuth2Helper.config.validForAuthorizationCodeFlow()) {
            throw DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)
        }

        val authRequest = oAuth2Helper.getAuthorizationRequest(scopes, additionalParameters)

        codeVerifier = authRequest.codeVerifier

        val authService = AuthorizationService(activity)
        val authIntent = oAuth2Helper.getCustomTabsIntent(authService, authRequest, toolBarColor)

        authService.performAuthorizationRequest(authRequest, completedIntent, cancelledIntent, authIntent)
    }

    /**
     * Login a user via WebView
     *
     * Initiate the authorization code login flow using a WebView
     *
     * @param activity Activity from which the ChromeTabs/Browser should be launched
     * @param scopes OpenID Connect OAuth2 scopes to be sent. See [OAuth2Scope].
     * @param additionalParameters Pass additional query parameters to the authorization endpoint (Optional)
     * @param toolBarColor Color of the CustomTabs toolbar using getColor() method
     *
     * NOTE: When using this method you need to call [handleWebLoginResponse]
     * in the onActivityResult() of the activity from which you call this method
     */
    @Throws(DataError::class)
    fun loginUserUsingWeb(
        activity: Activity,
        scopes: List<String>,
        additionalParameters: Map<String, String>? = null,
        toolBarColor: Int? = null
    ) {

        if (!oAuth2Helper.config.validForAuthorizationCodeFlow()) {
            throw DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)
        }

        val authRequest = oAuth2Helper.getAuthorizationRequest(scopes, additionalParameters)

        codeVerifier = authRequest.codeVerifier

        val authService = AuthorizationService(activity)
        val tabsIntent = oAuth2Helper.getCustomTabsIntent(authService, authRequest, toolBarColor)
        val authIntent = authService.getAuthorizationRequestIntent(authRequest, tabsIntent)

        activity.startActivityForResult(authIntent, RC_AUTH)
    }

    /**
     * Process the authorization response to continue WebView login flow
     *
     * @param authIntent Response intent received from WebView in onActivityResult or in onCreate of the pending intent Activity
     * @param scopes OpenID Connect OAuth2 scopes to be sent. See [OAuth2Scope].
     * @param completion: Completion handler with any error that occurred
     */
    fun handleWebLoginResponse(authIntent: Intent?, scopes: List<String>, completion: OnFrolloSDKCompletionListener<Result>) {
        authIntent?.let {
            val response = AuthorizationResponse.fromIntent(authIntent)
            val exception = AuthorizationException.fromIntent(authIntent)

            val authorizationCode = response?.authorizationCode

            if (authorizationCode != null) {
                exchangeAuthorizationCode(code = authorizationCode, codeVerifier = codeVerifier, scopes = scopes, completion = completion)
            } else {
                completion.invoke(Result.error(OAuth2Error(exception = exception)))
            }
        } ?: run {
            completion.invoke(Result.error(DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)))
        }
    }

    /**
     * Login a user using email and password
     *
     * @param email Email address of the user
     * @param password Password for the user
     * @param scopes OpenID Connect OAuth2 scopes to be sent. See [OAuth2Scope].
     * @param grantType Optional OAuth Grant Type for the Authentication. By Default it is [OAuthGrantType.PASSWORD]
     * @param completion: Completion handler with any error that occurred
     */
    fun loginUser(
        email: String,
        password: String,
        scopes: List<String>,
        grantType: OAuthGrantType = OAuthGrantType.PASSWORD,
        completion: OnFrolloSDKCompletionListener<Result>
    ) {
        if (loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.ALREADY_LOGGED_IN)
            Log.e("$TAG#loginUser", error.localizedDescription)
            completion.invoke(Result.error(error))
            return
        }

        if (!oAuth2Helper.config.validForROPC() &&
            grantType in listOf(OAuthGrantType.PASSWORD, OAuthGrantType.REALM_PASSWORD)
        ) {
            completion.invoke(Result.error(DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)))
            return
        }

        val request = oAuth2Helper.getLoginRequest(
            username = email,
            password = password,
            scopes = scopes,
            grantType = grantType
        )
        if (!request.valid) {
            completion.invoke(Result.error(DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)))
            return
        }

        // Authorize the user
        authorizeUser(request, completion, "loginUser")
    }

    /**
     * Exchange an authorization code and code verifier for a token
     *
     * @param code Authorization code
     * @param codeVerifier Authorization code verifier for PKCE (Optional)
     * @param scopes OpenID Connect OAuth2 scopes to be sent. See [OAuth2Scope].
     * @param completion Completion handler with any error that occurred
     */
    fun exchangeAuthorizationCode(code: String, codeVerifier: String? = null, scopes: List<String>, completion: OnFrolloSDKCompletionListener<Result>) {
        if (loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.ALREADY_LOGGED_IN)
            Log.e("$TAG#exchangeAuthorizationCode", error.localizedDescription)
            completion.invoke(Result.error(error))
            return
        }

        val request = oAuth2Helper.getExchangeAuthorizationCodeRequest(code = code, codeVerifier = codeVerifier, scopes = scopes)
        if (!request.valid) {
            completion.invoke(Result.error(DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)))
            return
        }

        // Authorize the user
        authorizeUser(request, completion, "exchangeAuthorizationCode")
    }

    /**
     * Exchange a legacy refresh token for a new valid refresh access token pair.
     *
     * @param legacyToken Legacy refresh token to be exchanged
     * @param completion Completion handler with any error that occurred
     */
    fun exchangeLegacyToken(legacyToken: String, completion: OnFrolloSDKCompletionListener<Result>) {
        val scopes = listOf(OAuth2Scope.OFFLINE_ACCESS, OAuth2Scope.EMAIL, OAuth2Scope.OPENID)
        val request = oAuth2Helper.getExchangeTokenRequest(legacyToken = legacyToken, scopes = scopes)
        if (!request.valid) {
            completion.invoke(Result.error(DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)))
            return
        }

        // Authorize the user
        authorizeUser(request, completion, "exchangeLegacyToken")
    }

    private fun authorizeUser(request: OAuthTokenRequest, completion: OnFrolloSDKCompletionListener<Result>, tagRef: String) {
        tokenAPI?.refreshTokens(request)?.enqueue(ErrorResponseType.OAUTH2) { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#$tagRef.refreshTokens", resource.error?.localizedDescription)

                    completion.invoke(Result.error(resource.error))
                }

                Resource.Status.SUCCESS -> {
                    resource.data?.let { response ->
                        handleTokens(response)

                        setLoggedIn()

                        updateDevice()

                        completion.invoke(Result.success())
                    } ?: run {
                        completion.invoke(Result.error(DataError(DataErrorType.AUTHENTICATION, DataErrorSubType.MISSING_ACCESS_TOKEN)))
                    }
                }
            }
        }
    }

    /**
     * Refresh Access Token
     *
     * Forces a refresh of the access token using the refresh token if a 401 was encountered. For advanced usage only in combination with web request authentication.
     *
     * @param completion Completion handler with any error that occurred (Optional)
     */
    fun refreshTokens(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (isRefreshingToken)
            return

        isRefreshingToken = true

        val refreshToken = authToken?.getRefreshToken()

        if (refreshToken == null) {
            val error = DataError(DataErrorType.AUTHENTICATION, DataErrorSubType.MISSING_REFRESH_TOKEN)
            completion?.invoke(Result.error(error))
            Log.e("$TAG#refreshTokens", error.localizedMessage)

            forcedReset()

            return
        }

        val request = oAuth2Helper.getRefreshTokensRequest(refreshToken)

        val response = tokenAPI?.refreshTokens(request)?.execute()
        val apiResponse = ApiResponse(response)

        if (apiResponse.isSuccessful) {
            apiResponse.body?.let {
                handleTokens(it)
            }

            completion?.invoke(Result.success())
        } else {
            val error = OAuth2Error(response = apiResponse.errorMessage)

            Log.e("$TAG#refreshTokens", error.localizedMessage)

            handleOAuth2Failure(error)

            completion?.invoke(Result.error(error))
        }

        isRefreshingToken = false
    }

    /**
     * Logout and reset the SDK
     *
     * Logout the user by revoking the refresh token if possible followed by local cleanup by calling reset
     *
     * @param completion Completion handler with option error if something goes wrong (optional)
     *
     * See also [FrolloSDK.reset]
     */
    fun logout(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        // Revoke the refresh token if possible
        authToken?.getRefreshToken()?.let { refreshToken ->
            val request = oAuth2Helper.getTokenRevokeRequest(refreshToken)
            revokeTokenAPI?.revokeToken(request)?.enqueue { resource ->
                if (resource.status == Resource.Status.ERROR) {
                    Log.d("$TAG#logout", resource.error?.localizedDescription)
                }
            }
        }

        forcedReset(completion)
    }

    override fun accessTokenExpired() {
        refreshTokens(null)
    }

    override fun tokenInvalidated() {
        forcedReset()
    }

    private fun handleTokens(tokenResponse: OAuthTokenResponse) {
        // With OAuth2 you get refresh token only the very first time and it is for lifetime.
        // Subsequent token refresh responses does not contain refresh token.
        // Hence hold on to the old refresh token if the response does not has a refresh token.
        tokenResponse.refreshToken?.let {
            authToken?.saveRefreshToken(it)
        }

        val createdAt = if (tokenResponse.createdAt > 0L)
            LocalDateTime.ofEpochSecond(tokenResponse.createdAt, 0, ZoneOffset.UTC)
        else
            LocalDateTime.now(ZoneOffset.UTC)
        val tokenExpiry = createdAt.plusSeconds(tokenResponse.expiresIn).toEpochSecond(ZoneOffset.UTC)

        authToken?.saveAccessToken(tokenResponse.accessToken)
        authToken?.saveTokenExpiry(tokenExpiry)
    }

    private fun setLoggedIn() {
        if (!loggedIn) {
            loggedIn = true

            notify(
                action = ACTION.ACTION_AUTHENTICATION_CHANGED,
                extrasKey = ARGUMENT.ARG_AUTHENTICATION_STATUS,
                extrasData = AuthenticationStatus.AUTHENTICATED
            )
        }
    }

    private fun updateDevice() {
        if (FrolloSDK.isSetup) FrolloSDK.userManagement.updateDevice()
    }

    private fun clearTokens() {
        authToken?.clearTokens()
    }

    /**
     * Reset the authentication state. Resets the user to a logged out state and clears any tokens cached
     */
    fun reset() {
        loggedIn = false

        isRefreshingToken = false

        clearTokens()
    }

    private fun forcedReset(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        reset()

        if (FrolloSDK.isSetup) FrolloSDK.reset(completion)
    }
}
