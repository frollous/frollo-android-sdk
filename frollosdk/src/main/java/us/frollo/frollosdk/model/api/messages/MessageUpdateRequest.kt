package us.frollo.frollosdk.model.api.messages

import com.google.gson.annotations.SerializedName

internal data class MessageUpdateRequest(
        @SerializedName("read") val read: Boolean,
        @SerializedName("interacted") val interacted: Boolean)