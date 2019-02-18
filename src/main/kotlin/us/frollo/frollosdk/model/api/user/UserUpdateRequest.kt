package us.frollo.frollosdk.model.api.user

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.user.*

internal data class UserUpdateRequest(
        @SerializedName("first_name") val firstName: String? = null,
        @SerializedName("email") val email: String? = null,
        @SerializedName("primary_currency") val primaryCurrency: String? = null,
        @SerializedName("attribution") val attribution: Attribution? = null,
        @SerializedName("last_name") val lastName: String? = null,
        @SerializedName("mobile_number") val mobileNumber: String? = null,
        @SerializedName("gender") val gender: Gender? = null,
        @SerializedName("address") val currentAddress: Address? = null,
        @SerializedName("household_size") val householdSize: Int? = null,
        @SerializedName("marital_status") val householdType: HouseholdType? = null,
        @SerializedName("occupation") val occupation: Occupation? = null,
        @SerializedName("industry") val industry: Industry? = null,
        @SerializedName("date_of_birth") val dateOfBirth: String? = null, // yyyy-MM or yyyy-MM-dd
        @SerializedName("driver_license") val driverLicense: String? = null)