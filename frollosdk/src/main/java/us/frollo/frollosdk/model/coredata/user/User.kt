package us.frollo.frollosdk.model.coredata.user

data class User(
        val userId: Long, //TODO: review if we should allow to modify this
        var firstName: String?,
        var email: String,
        val emailVerified: Boolean, //TODO: review if we should allow to modify this
        val status: UserStatus, //TODO: review if we should allow to modify this
        val primaryCurrency: String, //TODO: review if we should allow to modify this
        var validPassword: Boolean, //TODO: review if we should allow to modify this
        val registrationDate: String, //TODO: review if we should allow to modify this
        val facebookId: String?, //TODO: review if we should allow to modify this
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
        val features: List<FeatureFlag>?) //TODO: review if we should allow to modify this