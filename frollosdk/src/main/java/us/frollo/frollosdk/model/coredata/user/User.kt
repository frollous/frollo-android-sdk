package us.frollo.frollosdk.model.coredata.user

data class User(
        val userId: Int,
        val firstName: String,
        val email: String,
        val emailVerified: Boolean,
        val status: UserStatus,
        val primaryCurrency: String,
        val validPassword: Boolean,
        val lastName: String?,
        val gender: Gender?,
        val currentAddress: Address?,
        val previousAddress: Address?,
        val householdSize: Int?,
        val householdType: HouseholdType?,
        val occupation: Occupation?,
        val industry: Industry?,
        val dateOfBirth: String?,
        val driverLicense: String?
)