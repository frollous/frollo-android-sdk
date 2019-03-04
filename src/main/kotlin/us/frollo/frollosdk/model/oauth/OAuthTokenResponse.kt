package us.frollo.frollosdk.model.oauth

import com.google.gson.annotations.SerializedName

internal data class OAuthTokenResponse(
        @SerializedName("access_token") val accessToken: String,
        @SerializedName("created_at") val createdAt: Long,
        @SerializedName("expires_in") val expiresIn: Long,
        @SerializedName("refresh_token") val refreshToken: String?,
        @SerializedName("token_type") val tokenType: String
)