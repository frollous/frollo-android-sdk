package us.frollo.frollosdk.model.api.user

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.user.Address

internal data class UserRegisterRequest(
        @SerializedName("device_id") val deviceId: String,
        @SerializedName("device_name") val deviceName: String,
        @SerializedName("device_type") val deviceType: String,

        @SerializedName("email") val email: String,
        @SerializedName("first_name") val firstName: String,
        @SerializedName("password") val password: String,

        @SerializedName("address") var currentAddress: Address? = null,
        @SerializedName("date_of_birth") var dateOfBirth: String? = null, // yyyy-MM or yyyy-MM-dd
        @SerializedName("last_name") val lastName: String? = null,
        @SerializedName("mobile_number") val mobileNumber: String? = null)