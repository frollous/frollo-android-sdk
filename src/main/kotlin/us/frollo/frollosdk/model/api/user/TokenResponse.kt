package us.frollo.frollosdk.model.api.user

import com.google.gson.annotations.SerializedName

internal data class TokenResponse(
        @SerializedName("refresh_token") val refreshToken: String,
        @SerializedName("access_token") val accessToken: String,
        @SerializedName("access_token_exp") val accessTokenExp: Long
)