package us.frollo.frollosdk.model.api.device

import com.google.gson.annotations.SerializedName

data class DeviceUpdateRequest(
        @SerializedName("device_name") var deviceName: String?,
        @SerializedName("notification_token") var notificationToken: String?,
        @SerializedName("timezone") var timezone: String?
)