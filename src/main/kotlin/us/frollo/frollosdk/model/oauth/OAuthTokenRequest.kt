package us.frollo.frollosdk.model.oauth

import com.google.gson.annotations.SerializedName

internal data class OAuthTokenRequest(
        @SerializedName("grant_type") val grantType: OAuthGrantType,
        @SerializedName("client_id") val clientId: String,
        @SerializedName("domain") val domain: String,
        @SerializedName("code") val code: String? = null,
        @SerializedName("state") val state: String? = null,
        @SerializedName("refresh_token") val refreshToken: String? = null,
        @SerializedName("frollo_legacy_token") val legacyToken: String? = null,
        @SerializedName("username") val username: String? = null,
        @SerializedName("password") val password: String? = null
) {
    val valid: Boolean
        get() = when (grantType) {
                    OAuthGrantType.AUTHORIZATION_CODE -> code != null
                    OAuthGrantType.PASSWORD -> (password != null && username != null) || legacyToken != null
                    OAuthGrantType.REFRESH_TOKEN -> refreshToken != null
                }
}