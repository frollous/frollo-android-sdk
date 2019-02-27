package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.oauth.OAuthGrantType
import us.frollo.frollosdk.model.oauth.OAuthTokenRequest
import us.frollo.frollosdk.testutils.*

internal fun testOAuthTokenRequestData(
        grantType: OAuthGrantType,
        password: String? = null,
        legacyToken: String? = null,
        refreshToken: String? = null,
        authorizationCode: String? = null) : OAuthTokenRequest {

    return OAuthTokenRequest(
            grantType = grantType,
            clientId = randomString(32),
            domain = "api.example.com",
            username = "user@example.com",
            password = password,
            legacyToken = legacyToken,
            refreshToken = refreshToken,
            code = authorizationCode
    )
}