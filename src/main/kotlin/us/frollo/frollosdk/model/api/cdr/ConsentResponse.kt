/*
 * Copyright 2020 Frollo
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

package us.frollo.frollosdk.model.api.cdr

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.cdr.CDRPermission
import us.frollo.frollosdk.model.coredata.cdr.ConsentStatus

internal data class ConsentResponse(

    /** The ID of the consent */
    @SerializedName("id") val consentId: Long,

    /** The provider ID for the consent */
    @SerializedName("provider_id") val providerId: Long,

    /** The provider account ID for the consent (Optional) */
    @SerializedName("provider_account_id") val providerAccountId: Long?,

    /** The permissions requested for the consent */
    @SerializedName("permissions") val permissions: List<CDRPermission>,

    /** Additional permissions (meta-data map of String:Boolean) that can be set (Optional) */
    @SerializedName("additional_permissions") val additionalPermissions: JsonObject?,

    /** The authorization URL that should be used to initiate a login with the provider (Optional) */
    @SerializedName("authorisation_request_url") val authorisationRequestURL: String?,

    /** URL of the Consent Confirmation PDF (Optional) */
    @SerializedName("confirmation_pdf_url") val confirmationPDFURL: String?,

    /** The authorization URL that should be used to initiate a login with the provider (Optional) */
    @SerializedName("withdrawal_pdf_url") val withdrawalPDFURL: String?,

    /** Specifies whether the data should be deleted after the consent is done */
    @SerializedName("delete_redundant_data") val deleteRedundantData: Boolean,

    /** Start date of the sharing window. This date is the date when the consent officially starts on the Data Holder's end (Optional) */
    @SerializedName("sharing_started_at") val sharingStartedAt: String?, // yyyy-MM-dd

    /** Stopped sharing at date. The date the consent expired or was withdrawn (Optional) */
    @SerializedName("sharing_stopped_at") val sharingStoppedAt: String?, // yyyy-MM-dd

    /** The duration (in seconds) for the consent */
    @SerializedName("sharing_duration") val sharingDuration: Long?,

    /** The new status for the consent */
    @SerializedName("status") val status: ConsentStatus
)
