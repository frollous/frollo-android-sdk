/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.api.user.UserRegisterRequest
import us.frollo.frollosdk.model.api.user.UserResetPasswordRequest
import us.frollo.frollosdk.model.api.user.UserResponse
import us.frollo.frollosdk.model.coredata.user.Address
import us.frollo.frollosdk.model.coredata.user.Attribution
import us.frollo.frollosdk.model.coredata.user.FeatureFlag
import us.frollo.frollosdk.model.coredata.user.Gender
import us.frollo.frollosdk.model.coredata.user.HouseholdType
import us.frollo.frollosdk.model.coredata.user.Industry
import us.frollo.frollosdk.model.coredata.user.Occupation
import us.frollo.frollosdk.model.coredata.user.UserStatus
import us.frollo.frollosdk.testutils.randomNumber
import us.frollo.frollosdk.testutils.randomString
import us.frollo.frollosdk.testutils.randomUUID
import us.frollo.frollosdk.testutils.today

internal fun testUserResponseData(userId: Long? = null): UserResponse {
    val name = randomUUID()

    return UserResponse(
                userId = userId ?: randomNumber().toLong(),
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
                features = listOf(FeatureFlag(feature = "aggregation", enabled = true)))
}

internal fun UserResponse.testModifyUserResponseData(firstName: String): UserResponse {
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
            features = features)
}

internal fun testValidRegisterData(): UserRegisterRequest {
    val name = randomUUID()
    return UserRegisterRequest(
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