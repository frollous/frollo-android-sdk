package us.frollo.frollosdk.extensions

import us.frollo.frollosdk.model.api.user.TokenResponse
import us.frollo.frollosdk.model.api.user.UserResponse
import us.frollo.frollosdk.model.api.user.UserUpdateRequest
import us.frollo.frollosdk.model.coredata.user.User

internal fun UserResponse.stripTokens() =
        UserResponse(
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
                features = features)

internal fun UserResponse.fetchTokens(): TokenResponse =
        TokenResponse(
                accessToken = accessToken ?: "",
                refreshToken = refreshToken ?: "",
                accessTokenExp = accessTokenExp ?: -1)

internal fun User.updateRequest(): UserUpdateRequest =
        UserUpdateRequest(
                firstName = firstName,
                email = email,
                primaryCurrency = primaryCurrency,
                attribution = attribution,
                lastName = lastName,
                mobileNumber = mobileNumber,
                gender = gender,
                currentAddress = currentAddress,
                householdSize = householdSize,
                householdType = householdType,
                occupation = occupation,
                industry = industry,
                dateOfBirth = dateOfBirth,
                driverLicense = driverLicense)