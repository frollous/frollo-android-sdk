package us.frollo.frollosdk.model.api.user

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.auth.AuthType

internal data class UserLoginRequest(
        @SerializedName("auth_type") val authType: AuthType,
        @SerializedName("device_id") val deviceId: String,
        @SerializedName("device_name") val deviceName: String,
        @SerializedName("device_type") val deviceType: String,
        @SerializedName("email") val email: String?,
        @SerializedName("password") val password: String?,
        @SerializedName("user_id") val userId: String? = null,
        @SerializedName("user_token") val userToken: String? = null) {

        fun valid() = when(authType) {
                AuthType.EMAIL -> email != null && password != null
                AuthType.FACEBOOK -> email != null && userId != null && userToken != null
                AuthType.VOLT -> userId != null && userToken != null
        }
}