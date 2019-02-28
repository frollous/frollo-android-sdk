package us.frollo.frollosdk.authentication

import android.net.Uri
import us.frollo.frollosdk.core.FrolloSDKConfiguration
import us.frollo.frollosdk.model.oauth.OAuthGrantType
import us.frollo.frollosdk.model.oauth.OAuthTokenRequest

class OAuth(val config: FrolloSDKConfiguration) {

    private val domain: String
        get() = Uri.parse(config.serverUrl).host ?: ""

    internal fun getRefreshTokensRequest(refreshToken: String?) =
            OAuthTokenRequest(
                grantType = OAuthGrantType.REFRESH_TOKEN,
                clientId = config.clientId,
                domain = domain,
                refreshToken = refreshToken)

    internal fun getLoginRequest(username: String, password: String) =
            OAuthTokenRequest(
                    grantType = OAuthGrantType.PASSWORD,
                    clientId = config.clientId,
                    domain = domain,
                    username = username,
                    password = password)

    internal fun getRegisterRequest(username: String, password: String) =
            OAuthTokenRequest(
                    grantType = OAuthGrantType.PASSWORD,
                    clientId = config.clientId,
                    domain = domain,
                    username = username,
                    password = password)

    internal fun getExchangeAuthorizationCodeRequest(code: String, codeVerifier: String? = null) =
            OAuthTokenRequest(
                    grantType = OAuthGrantType.AUTHORIZATION_CODE,
                    clientId = config.clientId,
                    domain = domain,
                    code = code,
                    codeVerifier = codeVerifier)

    internal fun getExchangeTokenRequest(legacyToken: String) =
            OAuthTokenRequest(
                    grantType = OAuthGrantType.PASSWORD,
                    clientId = config.clientId,
                    domain = domain,
                    legacyToken = legacyToken)
}