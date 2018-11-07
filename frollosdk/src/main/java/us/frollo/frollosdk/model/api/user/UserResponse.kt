package us.frollo.frollosdk.model.api.user

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.user.*

data class UserResponse(
        @SerializedName("id") val id: Int,
        @SerializedName("first_name") var firstName: String,
        @SerializedName("email") var email: String,
        @SerializedName("email_verified") val emailVerified: Boolean,
        @SerializedName("status") var status: UserStatus,
        @SerializedName("primary_currency") val primaryCurrency: String,
        @SerializedName("valid_password") val valid_password: Boolean,
        @SerializedName("register_complete") val register_complete: Boolean,

        @SerializedName("facebook_id") val facebookId: String?,
        @SerializedName("attribution") val attribution: Attribution?,
        @SerializedName("last_name") var lastName: String?,
        @SerializedName("gender") var gender: Gender?,
        @SerializedName("address") var currentAddress: Address?,
        @SerializedName("previous_address") var previousAddress: Address?,
        @SerializedName("household_size") var householdSize: Int?,
        @SerializedName("marital_status") var householdType: HouseholdType?,
        @SerializedName("occupation") var occupation: Occupation?,
        @SerializedName("industry") var industry: Industry?,
        @SerializedName("date_of_birth") var dateOfBirth: String?,
        @SerializedName("driver_license") var driverLicense: String?,
        @SerializedName("features") var features: List<FeatureFlag>?,

        @SerializedName("refresh_token") var refreshToken: String? = null,
        @SerializedName("access_token") var accessToken: String? = null,
        @SerializedName("access_token_exp") var accessTokenExp: Long? = null
)