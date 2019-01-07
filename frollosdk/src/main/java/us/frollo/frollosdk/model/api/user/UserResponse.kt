package us.frollo.frollosdk.model.api.user

import androidx.room.*
import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.user.*

/**
 * Declaring the column info allows for the renaming of variables without implementing a
 * database migration, as the column name would not change.
 */
@Entity(tableName = "user",
        indices = [Index("user_id")])
internal data class UserResponse(
        @PrimaryKey
        @ColumnInfo(name = "user_id") @SerializedName("id") val userId: Int,
        @ColumnInfo(name = "first_name") @SerializedName("first_name") val firstName: String,
        @ColumnInfo(name = "email") @SerializedName("email") val email: String,
        @ColumnInfo(name = "email_verified") @SerializedName("email_verified") val emailVerified: Boolean,
        @ColumnInfo(name = "status") @SerializedName("status") val status: UserStatus,
        @ColumnInfo(name = "primary_currency") @SerializedName("primary_currency") val primaryCurrency: String,
        @ColumnInfo(name = "valid_password") @SerializedName("valid_password") val validPassword: Boolean,
        @ColumnInfo(name = "register_complete") @SerializedName("register_complete") val registerComplete: Boolean,
        @ColumnInfo(name = "registration_date") @SerializedName("registration_date") val registrationDate: String,

        @ColumnInfo(name = "facebook_id") @SerializedName("facebook_id") val facebookId: String?,
        @ColumnInfo(name = "attribution") @SerializedName("attribution") val attribution: Attribution?,
        @ColumnInfo(name = "last_name") @SerializedName("last_name") val lastName: String?,
        @ColumnInfo(name = "mobile_number") @SerializedName("mobile_number") var mobileNumber: String?,
        @ColumnInfo(name = "gender") @SerializedName("gender") val gender: Gender?,
        @Embedded(prefix = "c_address_") @SerializedName("address") val currentAddress: Address?,
        @Embedded(prefix = "p_address_") @SerializedName("previous_address") val previousAddress: Address?,
        @ColumnInfo(name = "household_size") @SerializedName("household_size") val householdSize: Int?,
        @ColumnInfo(name = "marital_status") @SerializedName("marital_status") val householdType: HouseholdType?,
        @ColumnInfo(name = "occupation") @SerializedName("occupation") val occupation: Occupation?,
        @ColumnInfo(name = "industry") @SerializedName("industry") val industry: Industry?,
        @ColumnInfo(name = "date_of_birth") @SerializedName("date_of_birth") val dateOfBirth: String?,
        @ColumnInfo(name = "driver_license") @SerializedName("driver_license") val driverLicense: String?,
        @ColumnInfo(name = "features") @SerializedName("features") val features: List<FeatureFlag>?,

        @ColumnInfo(name = "refresh_token") @SerializedName("refresh_token") val refreshToken: String? = null,
        @ColumnInfo(name = "access_token") @SerializedName("access_token") val accessToken: String? = null,
        @ColumnInfo(name = "access_token_exp") @SerializedName("access_token_exp") val accessTokenExp: Long? = null
)