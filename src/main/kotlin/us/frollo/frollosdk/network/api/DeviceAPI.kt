package us.frollo.frollosdk.network.api

import retrofit2.Call
import retrofit2.http.*
import us.frollo.frollosdk.model.api.device.DeviceUpdateRequest
import us.frollo.frollosdk.model.api.device.LogRequest

internal interface DeviceAPI {
    companion object {
        const val URL_DEVICE = "device"
        const val URL_LOG = "device/log"
    }

    @PUT(URL_DEVICE)
    fun updateDevice(@Body request: DeviceUpdateRequest): Call<Void>

    @POST(URL_LOG)
    fun createLog(@Body request: LogRequest): Call<Void>
}