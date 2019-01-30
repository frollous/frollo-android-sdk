package us.frollo.frollosdk.model.api.user

import com.google.gson.annotations.SerializedName

internal data class UserResetPasswordRequest(
        @SerializedName("email") val email: String)