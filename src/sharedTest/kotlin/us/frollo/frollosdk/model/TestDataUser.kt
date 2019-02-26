package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.api.user.UserRegisterRequest
import us.frollo.frollosdk.model.api.user.UserResetPasswordRequest
import us.frollo.frollosdk.model.api.user.UserResponse
import us.frollo.frollosdk.model.coredata.user.*
import us.frollo.frollosdk.testutils.*

internal fun testUserResponseData() : UserResponse {
    val name = randomUUID()

    return UserResponse(
                userId = randomNumber().toLong(),
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
                mobileNumber = "0411111111",
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

internal fun UserResponse.testModifyUserResponseData(firstName: String) : UserResponse {
    return UserResponse(
            userId = userId,
            firstName = firstName,
            email = email,
            emailVerified = emailVerified,
            status = status,
            primaryCurrency = primaryCurrency,
            validPassword = validPassword,
            registerComplete = registerComplete,
            registrationDate = registrationDate,
            facebookId = facebookId,
            attribution = attribution,
            lastName = lastName,
            mobileNumber = mobileNumber,
            gender = gender,
            currentAddress = currentAddress,
            previousAddress = previousAddress,
            householdSize = householdSize,
            householdType = householdType,
            occupation = occupation,
            industry = industry,
            dateOfBirth = dateOfBirth,
            driverLicense = driverLicense,
            features = features,
            refreshToken = refreshToken,
            accessToken = accessToken,
            accessTokenExp = accessTokenExp)
}

internal fun testEmailLoginData() : UserLoginRequest {
    val name = randomUUID()

    return UserLoginRequest(
            authType = AuthType.EMAIL,
            deviceId = randomString(8),
            deviceName = randomString(8),
            deviceType = "Android",
            email = "$name@frollo.us",
            password = randomString(8))
}

internal fun testFacebookLoginData() : UserLoginRequest {
    val name = randomUUID()

    return UserLoginRequest(
            authType = AuthType.FACEBOOK,
            deviceId = randomString(8),
            deviceName = randomString(8),
            deviceType = "Android",
            email = "$name@frollo.us",
            userId = randomNumber().toString(),
            userToken = randomString(8))
}

internal fun testVoltLoginData() : UserLoginRequest {
    return UserLoginRequest(
            authType = AuthType.VOLT,
            deviceId = randomString(8),
            deviceName = randomString(8),
            deviceType = "Android",
            userId = randomNumber().toString(),
            userToken = randomString(8))
}

internal fun testInvalidLoginData() : UserLoginRequest {
    return UserLoginRequest(
            authType = AuthType.FACEBOOK,
            deviceId = randomString(8),
            deviceName = randomString(8),
            deviceType = "Android",
            userId = randomNumber().toString())
}

internal fun testValidRegisterData() : UserRegisterRequest {
    val name = randomUUID()
    return UserRegisterRequest(
            deviceId = randomString(8),
            deviceName = randomString(8),
            deviceType = "Android",
            firstName = name,
            lastName = randomUUID(),
            mobileNumber = "0411111111",
            currentAddress = Address(postcode = "2060"),
            dateOfBirth = "1990-01",
            email = "$name@frollo.us",
            password = randomString(8)
    )
}

internal fun testResetPasswordData() =
        UserResetPasswordRequest(email = "${randomUUID()}@frollo.us")