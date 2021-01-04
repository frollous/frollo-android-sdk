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

package us.frollo.frollosdk.mapping

import us.frollo.frollosdk.model.api.user.UserResponse
import us.frollo.frollosdk.model.coredata.user.User

internal fun UserResponse.toUser() =
    User(
        userId = userId,
        firstName = firstName,
        email = email,
        emailVerified = emailVerified,
        status = status,
        primaryCurrency = primaryCurrency,
        validPassword = validPassword,
        registerSteps = registerSteps,
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
