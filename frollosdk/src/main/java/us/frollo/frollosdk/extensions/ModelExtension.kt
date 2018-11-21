package us.frollo.frollosdk.extensions

import us.frollo.frollosdk.model.api.user.TokenResponse
import us.frollo.frollosdk.model.api.user.UserResponse

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
                gender = gender,
                currentAddress = currentAddress,
                previousAddress = previousAddress,
                householdSize = householdSize,
                householdType = householdType,
                occupation = occupation,
                industry = industry,
                dateOfBirth = dateOfBirth,
                driverLicense = driverLicense,
                features = features
        )

internal fun UserResponse.fetchTokens(): TokenResponse =
        TokenResponse(
                accessToken = accessToken ?: "",
                refreshToken = refreshToken ?: "",
                accessTokenExp = accessTokenExp ?: -1
        )