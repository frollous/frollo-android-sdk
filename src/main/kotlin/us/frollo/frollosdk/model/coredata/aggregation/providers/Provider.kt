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

package us.frollo.frollosdk.model.coredata.aggregation.providers

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import us.frollo.frollosdk.model.IAdapterModel

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(tableName = "provider",
        indices = [Index("provider_id")])

/** Data representation Provider */
data class Provider(

    /** Unique ID of the provider */
    @PrimaryKey @ColumnInfo(name = "provider_id") val providerId: Long,

    /** Name of the provider */
    @ColumnInfo(name = "provider_name") val providerName: String,

    /** URL to the small logo image (optional). Empty if no logo image available. */
    @ColumnInfo(name = "small_logo_url") val smallLogoUrl: String,

    /** Small Logo Revision (optional). 0 if no logo image revision available. */
    @ColumnInfo(name = "small_logo_revision") val smallLogoRevision: Int,

    /** Status of the provider */
    @ColumnInfo(name = "provider_status") val providerStatus: ProviderStatus,

    /** Popular provider */
    @ColumnInfo(name = "popular") val popular: Boolean,

    /** Supported container names */
    @ColumnInfo(name = "container_names") val containerNames: List<ProviderContainerName>,

    /** URL of the provider's login page (optional) */
    @ColumnInfo(name = "login_url") val loginUrl: String?,

    /** URL to the large logo image (optional) */
    @ColumnInfo(name = "large_logo_url") val largeLogoUrl: String?,

    /** Large Logo Revision */
    @ColumnInfo(name = "large_logo_revision") val largeLogoRevision: Int?,

    /** Specifies the aggregator with which this Provider get its data from */
    @ColumnInfo(name = "aggregator_type") val aggregatorType: AggregatorType,

    /** The permission groups that are supported by this Provider */
    @ColumnInfo(name = "permissions") val permissions: List<ProviderPermission>?,

    /** Base URL of the provider's website */
    @ColumnInfo(name = "base_url") val baseUrl: String?,

    /** URL to the forgot password page of the provider (optional) */
    @ColumnInfo(name = "forget_password_url") val forgetPasswordUrl: String?,

    /** OAuth site */
    @ColumnInfo(name = "o_auth_site") val oAuthSite: Boolean?,

    /** Authentication Type (optional) */
    @ColumnInfo(name = "auth_type") val authType: ProviderAuthType?,

    /** Type of MFA on the provider (optional) */
    @ColumnInfo(name = "mfa_type") val mfaType: ProviderMFAType?,

    /** Help message to be displayed alongside the provider (optional) */
    @ColumnInfo(name = "help_message") val helpMessage: String?,

    /** Login help message (optional) */
    @ColumnInfo(name = "login_help_message") val loginHelpMessage: String?,

    /** Login Form (optional) */
    @ColumnInfo(name = "login_form") val loginForm: ProviderLoginForm?,

    /** Provider encryption (optional) */
    @Embedded(prefix = "encryption_") val encryption: ProviderEncryption?

) : IAdapterModel