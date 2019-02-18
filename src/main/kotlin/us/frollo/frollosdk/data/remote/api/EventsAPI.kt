package us.frollo.frollosdk.data.remote.api

import retrofit2.Call
import retrofit2.http.*
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.API_VERSION_PATH
import us.frollo.frollosdk.model.api.events.EventCreateRequest

internal interface EventsAPI {
    companion object {
        const val URL_EVENT = "$API_VERSION_PATH/events/"
    }

    @POST(URL_EVENT)
    fun createEvent(@Body request: EventCreateRequest): Call<Void>
}