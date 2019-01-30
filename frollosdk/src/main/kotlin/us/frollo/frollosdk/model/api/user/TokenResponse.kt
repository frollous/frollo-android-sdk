package us.frollo.frollosdk.model.api.user

import com.google.gson.annotations.SerializedName

internal data class TokenResponse(
        @SerializedName("refresh_token") val refreshToken: String, //aaaaaaaaaa.bbbbbbbbbbb.cccccccccccc
        @SerializedName("access_token") val accessToken: String, //dddddddddd.eeeeeeeeee.ffffffffffff
        @SerializedName("access_token_exp") val accessTokenExp: Long //1475644695
)