package us.frollo.frollosdk.model.coredata.user

/**
 * Data representation of User
 */
data class User(
        /** Unique ID of the user */
        val userId: Long,
        /** First name of the user */
        var firstName: String?,
        /** Email address of the user */
        var email: String,
        /** User verified their email address */
        val emailVerified: Boolean,
        /** Status of the user's account */
        val status: UserStatus,
        /** Primary currency of the user */
        val primaryCurrency: String,
        /** User has a valid password */
        val validPassword: Boolean,
        /** Date user registered */
        val registrationDate: String,
        /** Facebook ID associated with the user (optional) */
        val facebookId: String?,
        /** Attribution of the user */
        var attribution: Attribution?,
        /** Last name of the user (optional) */
        var lastName: String?,
        /** Mobile phone number of the user (optional) */
        var mobileNumber: String?,
        /** Gender of the user (optional) */
        var gender: Gender?,
        /** Current address of the user */
        var currentAddress: Address?,
        /** Previous address of the user */
        var previousAddress: Address?,
        /** Number of people in the household (optional) */
        var householdSize: Int?,
        /** Household type of the user (optional) */
        var householdType: HouseholdType?,
        /** Occupation of the user (optional) */
        var occupation: Occupation?,
        /** Industry the user works in (optional) */
        var industry: Industry?,
        /** Date of birth of the user (optional) */
        var dateOfBirth: String?,
        /** Drivers license of the user */
        var driverLicense: String?,
        /** A list of [FeatureFlag] decoded from a json array stored in the database. (Optional) */
        val features: List<FeatureFlag>?)