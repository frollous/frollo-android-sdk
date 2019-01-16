package us.frollo.frollosdk.data.remote.api

import retrofit2.Call
import retrofit2.http.*
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.API_VERSION_PATH
import us.frollo.frollosdk.model.api.device.DeviceUpdateRequest
import us.frollo.frollosdk.model.api.user.TokenResponse

internal interface DeviceAPI {
    companion object {
        const val URL_DEVICE = "$API_VERSION_PATH/device/"
        const val URL_TOKEN_REFRESH = "$API_VERSION_PATH/device/refresh/"
    }

    @PUT(URL_DEVICE)
    fun updateDevice(@Body request: DeviceUpdateRequest): Call<Void>

    @POST(URL_TOKEN_REFRESH)
    fun refreshTokens(): Call<TokenResponse>
}