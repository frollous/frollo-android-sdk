package us.frollo.frollosdk.data.remote.api

import retrofit2.Call
import retrofit2.http.*
import us.frollo.frollosdk.data.remote.NetworkHelper
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.API_VERSION_PATH
import us.frollo.frollosdk.model.api.user.TokenResponse

internal interface TokenAPI {
    companion object {
        const val URL_TOKEN_REFRESH = "$API_VERSION_PATH/device/refresh/"
    }

    @POST(URL_TOKEN_REFRESH)
    fun refreshTokens(): Call<TokenResponse>
}