package us.frollo.frollosdk.model.api.events

import com.google.gson.annotations.SerializedName

internal data class EventCreateRequest(
        @SerializedName("event") val event: String,
        @SerializedName("delay_minutes") val delayMinutes: Long)