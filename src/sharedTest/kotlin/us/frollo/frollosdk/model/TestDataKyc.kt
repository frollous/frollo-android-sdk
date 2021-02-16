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

import us.frollo.frollosdk.model.coredata.kyc.DateOfBirth
import us.frollo.frollosdk.model.coredata.kyc.IdentityDocument
import us.frollo.frollosdk.model.coredata.kyc.IdentityDocumentType
import us.frollo.frollosdk.model.coredata.kyc.KycStatus
import us.frollo.frollosdk.model.coredata.kyc.Name
import us.frollo.frollosdk.model.coredata.kyc.UserKyc

internal fun testKycResponseData(): UserKyc {
    return UserKyc(
        addresses = listOf(testAddressData()),
        dateOfBirth = DateOfBirth(
            dateOfBirth = "1991-01-01",
            yearOfBirth = "1991"
        ),
        email = "drsheldon@frollo.us",
        gender = "M",
        mobileNumber = "0421354444",
        name = Name(
            displayName = "Sheldon",
            familyName = "Cooper",
            givenName = "Shelly",
            honourific = "Dr",
            middleName = "K"
        ),
        identityDocuments = listOf(
            IdentityDocument(
                country = "AU",
                idExpiry = "2022-12-12",
                idNumber = "123456",
                idSubType = "certificate",
                idType = IdentityDocumentType.NATIONAL_HEALTH_ID,
                region = "Sydney"
            )
        ),
        status = KycStatus.VERIFIED
    )
}
