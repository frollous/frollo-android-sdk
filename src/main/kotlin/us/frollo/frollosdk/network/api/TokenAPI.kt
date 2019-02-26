package us.frollo.frollosdk.network.api

import retrofit2.Call
import retrofit2.http.*
import us.frollo.frollosdk.model.oauth.OAuthTokenRequest
import us.frollo.frollosdk.model.oauth.OAuthTokenResponse

internal interface TokenAPI {
    @POST
    fun refreshTokens(@Body request: OAuthTokenRequest): Call<OAuthTokenResponse>
}