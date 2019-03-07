package us.frollo.frollosdk.model.coredata.aggregation.providers

import androidx.room.*
import us.frollo.frollosdk.model.IAdapterModel

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(tableName = "provider",
        indices = [Index("provider_id")])

/** Data representation Provider */
data class Provider(

        /** Unique ID of the provider */
        @PrimaryKey
        @ColumnInfo(name = "provider_id") val providerId: Long,

        /** Name of the provider */
        @ColumnInfo(name = "provider_name") val providerName: String,

        /** URL to the small logo image (optional) */
        @ColumnInfo(name = "small_logo_url") val smallLogoUrl: String,

        /** Small Logo Revision */
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

): IAdapterModel