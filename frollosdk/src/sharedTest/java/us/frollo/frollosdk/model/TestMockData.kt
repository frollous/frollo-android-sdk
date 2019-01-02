package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.api.user.UserResponse
import us.frollo.frollosdk.model.coredata.user.*
import us.frollo.frollosdk.testutils.*

internal fun testDataUserResponse() : UserResponse {
    val name = randomUUID()

    return UserResponse(
                userId = randomNumber(),
                firstName = name,
                email = "$name@frollo.us",
                emailVerified = true,
                status = UserStatus.ACTIVE,
                primaryCurrency = "AUD",
                validPassword = true,
                registerComplete = true,
                registrationDate = today("yyyy-MM"),
                facebookId = randomNumber().toString(),
                attribution = Attribution(adGroup = randomString(8), campaign = randomString(8), creative = randomString(8), network = randomString(8)),
                lastName = randomUUID(),
                gender = Gender.MALE,
                currentAddress = Address(lineOne = "41 McLaren Street", lineTwo = "Frollo Level 1", suburb = "North Sydney", postcode = "2060"),
                previousAddress = Address(lineOne = "Bay 9 Middlemiss St", lineTwo = "Frollo Unit 13", suburb = "Lavender Bay", postcode = "2060"),
                householdSize = 1,
                householdType = HouseholdType.SINGLE,
                occupation = Occupation.COMMUNITY_AND_PERSONAL_SERVICE_WORKERS,
                industry = Industry.ELECTRICITY_GAS_WATER_AND_WASTE_SERVICES,
                dateOfBirth = "1990-01",
                driverLicense = "12345678",
                features = listOf(FeatureFlag(feature = "aggregation", enabled = true)),
                refreshToken = "AValidRefreshTokenFromHost",
                accessToken = "AValidAccessTokenFromHost",
                accessTokenExp = 1721259268)
}