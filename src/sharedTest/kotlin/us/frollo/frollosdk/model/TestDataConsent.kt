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

import us.frollo.frollosdk.model.api.cdr.CDRConfigurationResponse
import us.frollo.frollosdk.model.api.cdr.ConsentResponse
import us.frollo.frollosdk.model.coredata.cdr.CDRPermission
import us.frollo.frollosdk.model.coredata.cdr.CDRPermissionDetail
import us.frollo.frollosdk.model.coredata.cdr.ConsentCreateForm
import us.frollo.frollosdk.model.coredata.cdr.ConsentStatus
import us.frollo.frollosdk.model.coredata.cdr.ConsentUpdateForm
import us.frollo.frollosdk.model.coredata.cdr.SharingDuration
import us.frollo.frollosdk.testutils.randomNumber
import us.frollo.frollosdk.testutils.randomString

internal fun testConsentResponseData(
    consentId: Long? = null,
    providerId: Long? = null,
    providerAccountId: Long? = null,
    status: ConsentStatus? = null
): ConsentResponse {

    return ConsentResponse(
        consentId = consentId ?: randomNumber().toLong(),
        providerId = providerId ?: randomNumber().toLong(),
        providerAccountId = providerAccountId ?: randomNumber().toLong(),
        status = status ?: ConsentStatus.ACTIVE,
        sharingDuration = randomNumber().toLong(),
        sharingStartedAt = "2020-01-03",
        sharingStoppedAt = "2020-05-03",
        authorisationRequestURL = "",
        confirmationPDFURL = "",
        withdrawalPDFURL = "",
        permissions = testCDRPermissionData(),
        additionalPermissions = null,
        deleteRedundantData = true
    )
}

internal fun testCDRPermissionData(): List<CDRPermission> {
    return listOf(
        CDRPermission(
            permissionId = "account_details",
            title = "Account balance and details",
            description = "We leverage...",
            required = true,
            details = listOf(
                CDRPermissionDetail(
                    detailId = "account_name",
                    description = "Name of account"
                )
            )
        ),
        CDRPermission(
            permissionId = "transaction_details",
            title = "Transaction and details",
            description = "We leverage...",
            required = false,
            details = listOf(
                CDRPermissionDetail(
                    detailId = "transaction_name",
                    description = "Name of transaction"
                )
            )
        )
    )
}

internal fun testConsentCreateFormData(providerId: Long? = null): ConsentCreateForm {
    return ConsentCreateForm(
        providerId = providerId ?: randomNumber().toLong(),
        sharingDuration = randomNumber().toLong(),
        permissions = listOf("account_details"),
        additionalPermissions = null,
        existingConsentId = null
    )
}

internal fun testConsentUpdateFormData(sharingDuration: Long? = null): ConsentUpdateForm {
    return ConsentUpdateForm(
        sharingDuration = sharingDuration ?: randomNumber().toLong()
    )
}

internal fun testCDRConfigurationData(adrId: String? = null): CDRConfigurationResponse {
    return CDRConfigurationResponse(
        adrId = adrId ?: randomString(20),
        adrName = randomString(20),
        supportEmail = randomString(20),
        sharingDurations = listOf(testSharingDurationData(), testSharingDurationData(), testSharingDurationData())
    )
}

internal fun testSharingDurationData(): SharingDuration {
    return SharingDuration(
        duration = randomNumber().toLong(),
        description = randomString(20),
        imageUrl = randomString(20)
    )
}
