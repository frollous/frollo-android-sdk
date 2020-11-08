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

package us.frollo.frollosdk.model.coredata.cdr

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.JsonObject
import us.frollo.frollosdk.model.IAdapterModel

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(
    tableName = "consent",
    indices = [
        Index("consent_id"),
        Index("provider_account_id"),
        Index("provider_id")
    ]
)

/** Data representation Consent */
data class Consent(

    /** Unique ID for the consent */
    @PrimaryKey @ColumnInfo(name = "consent_id") val consentId: Long,

    /** The provider ID for the consent */
    @ColumnInfo(name = "provider_id") val providerId: Long,

    /** The provider account ID for the consent (Optional) */
    @ColumnInfo(name = "provider_account_id") val providerAccountId: Long?,

    /** The permissions requested for the consent */
    @ColumnInfo(name = "permissions") val permissions: List<CDRPermission>,

    /** Additional permissions (meta-data map of String:Boolean) that can be set (Optional) */
    @ColumnInfo(name = "additional_permissions") val additionalPermissions: JsonObject?,

    /** The authorization URL that should be used to initiate a login with the provider (Optional) */
    @ColumnInfo(name = "authorisation_request_url") val authorisationRequestURL: String?,

    /** URL of the Consent Confirmation PDF (Optional) */
    @ColumnInfo(name = "confirmation_pdf_url") val confirmationPDFURL: String?,

    /** The authorization URL that should be used to initiate a login with the provider (Optional) */
    @ColumnInfo(name = "withdrawal_pdf_url") val withdrawalPDFURL: String?,

    /** Specifies whether the data should be deleted after the consent is done */
    @ColumnInfo(name = "delete_redundant_data") val deleteRedundantData: Boolean,

    /** Start date of the sharing window. This date is the date when the consent officially starts on the Data Holder's end */
    @ColumnInfo(name = "sharing_started_at") val sharingStartedAt: String?,

    /** Stopped sharing at date. The date the consent expired or was withdrawn (Optional) */
    @ColumnInfo(name = "sharing_stopped_at") val sharingStoppedAt: String?,

    /** The duration (in seconds) for the consent */
    @ColumnInfo(name = "sharing_duration") val sharingDuration: Long?,

    /** The new status for the consent */
    @ColumnInfo(name = "status") val status: ConsentStatus

) : IAdapterModel {

    companion object {
        /** Date format for dates associated with Consent */
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    }
}
