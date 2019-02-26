package us.frollo.frollosdk.model.api.device

import com.google.gson.annotations.SerializedName

internal data class DeviceUpdateRequest(
        @SerializedName("device_id") val deviceId: String,
        @SerializedName("device_type") val deviceType: String,
        @SerializedName("device_name") var deviceName: String,
        @SerializedName("notification_token") var notificationToken: String?,
        @SerializedName("timezone") var timezone: String?,
        @SerializedName("compliant") var compliant: Boolean?
)