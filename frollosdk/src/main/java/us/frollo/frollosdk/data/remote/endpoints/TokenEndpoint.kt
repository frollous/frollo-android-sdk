package us.frollo.frollosdk.data.remote.endpoints

import retrofit2.Call
import retrofit2.http.*
import us.frollo.frollosdk.model.api.user.TokenResponse

internal interface TokenEndpoint {
    companion object {
        const val URL_TOKEN_REFRESH = "/api/v1/device/refresh"
    }

    @POST(URL_TOKEN_REFRESH)
    fun refreshTokens(): Call<TokenResponse>
}