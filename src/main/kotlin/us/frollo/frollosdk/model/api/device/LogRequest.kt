package us.frollo.frollosdk.model.api.device

import com.google.gson.annotations.SerializedName

internal data class LogRequest(
        @SerializedName("device_id") val deviceId: String,
        @SerializedName("device_type") val deviceType: String,
        @SerializedName("device_name") var deviceName: String,
        @SerializedName("details") var details: String? = null,
        @SerializedName("message") var message: String,
        @SerializedName("score") var score: Int
)