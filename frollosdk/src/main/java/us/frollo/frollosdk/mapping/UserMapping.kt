package us.frollo.frollosdk.mapping

import us.frollo.frollosdk.model.api.user.UserResponse
import us.frollo.frollosdk.model.coredata.user.*

internal fun UserResponse.toUser() =
        User(
                userId = userId,
                firstName = firstName,
                email = email,
                emailVerified = emailVerified,
                status = status,
                primaryCurrency = primaryCurrency,
                validPassword = validPassword,
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
                features = features
        )