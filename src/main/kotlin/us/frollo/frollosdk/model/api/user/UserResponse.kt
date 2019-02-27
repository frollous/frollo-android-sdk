package us.frollo.frollosdk.model.api.user

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.user.*

internal data class UserResponse(
        @SerializedName("id") val userId: Long,
        @SerializedName("first_name") val firstName: String,
        @SerializedName("email") val email: String,
        @SerializedName("email_verified") val emailVerified: Boolean,
        @SerializedName("status") val status: UserStatus,
        @SerializedName("primary_currency") val primaryCurrency: String,
        @SerializedName("valid_password") val validPassword: Boolean,
        @SerializedName("register_complete") val registerComplete: Boolean,
        @SerializedName("registration_date") val registrationDate: String,
        @SerializedName("facebook_id") val facebookId: String?,
        @SerializedName("attribution") val attribution: Attribution?,
        @SerializedName("last_name") val lastName: String?,
        @SerializedName("mobile_number") var mobileNumber: String?,
        @SerializedName("gender") val gender: Gender?,
        @SerializedName("address") val currentAddress: Address?,
        @SerializedName("previous_address") val previousAddress: Address?,
        @SerializedName("household_size") val householdSize: Int?,
        @SerializedName("marital_status") val householdType: HouseholdType?,
        @SerializedName("occupation") val occupation: Occupation?,
        @SerializedName("industry") val industry: Industry?,
        @SerializedName("date_of_birth") val dateOfBirth: String?, // yyyy-MM or yyyy-MM-dd
        @SerializedName("driver_license") val driverLicense: String?,
        @SerializedName("features") val features: List<FeatureFlag>?
)