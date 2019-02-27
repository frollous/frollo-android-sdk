package us.frollo.frollosdk.network.api

import retrofit2.Call
import retrofit2.http.*
import us.frollo.frollosdk.model.oauth.OAuthTokenRequest
import us.frollo.frollosdk.model.oauth.OAuthTokenResponse

internal interface TokenAPI {
    companion object {
        const val URL_TOKEN = "/"
    }

    @POST(URL_TOKEN)
    fun refreshTokens(@Body request: OAuthTokenRequest): Call<OAuthTokenResponse>
}