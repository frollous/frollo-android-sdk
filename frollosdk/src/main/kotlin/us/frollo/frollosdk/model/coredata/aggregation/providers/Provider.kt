package us.frollo.frollosdk.model.coredata.aggregation.providers

import androidx.room.*
import us.frollo.frollosdk.model.IAdapterModel

/**
 * Declaring the column info allows for the renaming of variables without implementing a
 * database migration, as the column name would not change.
 */
@Entity(tableName = "provider",
        indices = [Index("provider_id")])
data class Provider(
        @PrimaryKey
        @ColumnInfo(name = "provider_id") val providerId: Long,
        @ColumnInfo(name = "provider_name") val providerName: String,
        @ColumnInfo(name = "small_logo_url") val smallLogoUrl: String,
        @ColumnInfo(name = "small_logo_revision") val smallLogoRevision: Int,
        @ColumnInfo(name = "provider_status") val providerStatus: ProviderStatus,
        @ColumnInfo(name = "popular") val popular: Boolean,
        @ColumnInfo(name = "container_names") val containerNames: List<ProviderContainerName>,
        @ColumnInfo(name = "login_url") val loginUrl: String?,

        @ColumnInfo(name = "large_logo_url") val largeLogoUrl: String?,
        @ColumnInfo(name = "large_logo_revision") val largeLogoRevision: Int?,
        @ColumnInfo(name = "base_url") val baseUrl: String?,
        @ColumnInfo(name = "forget_password_url") val forgetPasswordUrl: String?,
        @ColumnInfo(name = "o_auth_site") val oAuthSite: Boolean?,
        @ColumnInfo(name = "auth_type") val authType: ProviderAuthType?,
        @ColumnInfo(name = "mfa_type") val mfaType: ProviderMFAType?,
        @ColumnInfo(name = "help_message") val helpMessage: String?,
        @ColumnInfo(name = "login_help_message") val loginHelpMessage: String?,
        @ColumnInfo(name = "login_form") val loginForm: ProviderLoginForm?,
        @Embedded(prefix = "encryption_") val encryption: ProviderEncryption?
): IAdapterModel