package us.frollo.frollosdk.model.api.user

import com.google.gson.annotations.SerializedName

internal data class UserChangePasswordRequest(
        @SerializedName("current_password") val currentPassword: String?,
        @SerializedName("new_password") val newPassword: String
) {
        fun valid() = newPassword.length >= 8
}