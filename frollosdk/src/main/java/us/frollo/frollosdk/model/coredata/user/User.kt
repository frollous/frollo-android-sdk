package us.frollo.frollosdk.model.coredata.user

data class User(
        val userId: Long,
        var firstName: String?,
        var email: String,
        val emailVerified: Boolean,
        val status: UserStatus,
        val primaryCurrency: String,
        val validPassword: Boolean,
        val registrationDate: String,
        val facebookId: String?,
        var attribution: Attribution?,
        var lastName: String?,
        var mobileNumber: String?,
        var gender: Gender?,
        var currentAddress: Address?,
        var previousAddress: Address?,
        var householdSize: Int?,
        var householdType: HouseholdType?,
        var occupation: Occupation?,
        var industry: Industry?,
        var dateOfBirth: String?,
        var driverLicense: String?,
        val features: List<FeatureFlag>?)