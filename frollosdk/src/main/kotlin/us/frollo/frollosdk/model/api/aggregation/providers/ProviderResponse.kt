package us.frollo.frollosdk.model.api.aggregation.providers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.aggregation.providers.*

/**
 * Declaring the column info allows for the renaming of variables without implementing a
 * database migration, as the column name would not change.
 */
@Entity(tableName = "provider",
        indices = [Index("provider_id")])
internal data class ProviderResponse(
        @PrimaryKey
        @ColumnInfo(name = "provider_id") @SerializedName("id") val providerId: Long,
        @ColumnInfo(name = "provider_name") @SerializedName("name") val providerName: String,
        @ColumnInfo(name = "small_logo_url") @SerializedName("small_logo_url") val smallLogoUrl: String,
        @ColumnInfo(name = "small_logo_revision") @SerializedName("small_logo_revision") val smallLogoRevision: Int,
        @ColumnInfo(name = "provider_status") @SerializedName("status") val providerStatus: ProviderStatus,
        @ColumnInfo(name = "popular") @SerializedName("popular") val popular: Boolean,
        @ColumnInfo(name = "container_names") @SerializedName("container_names") val containerNames: List<String>,
        @ColumnInfo(name = "login_url") @SerializedName("login_url") val loginUrl: String?,

        @ColumnInfo(name = "large_logo_url") @SerializedName("large_logo_url") val largeLogoUrl: String?,
        @ColumnInfo(name = "large_logo_revision") @SerializedName("large_logo_revision") val largeLogoRevision: Int?,
        @ColumnInfo(name = "base_url") @SerializedName("base_url") val baseUrl: String?,
        @ColumnInfo(name = "forget_password_url") @SerializedName("forget_password_url") val forgetPasswordUrl: String?,
        @ColumnInfo(name = "o_auth_site") @SerializedName("o_auth_site") val oAuthSite: Boolean?,
        @ColumnInfo(name = "auth_type") @SerializedName("auth_type") val authType: ProviderAuthType?,
        @ColumnInfo(name = "mfa_type") @SerializedName("mfa_type") val mfaType: ProviderMFAType?,
        @ColumnInfo(name = "help_message") @SerializedName("help_message") val helpMessage: String?,
        @ColumnInfo(name = "login_help_message") @SerializedName("login_help_message") val loginHelpMessage: String?,
        @ColumnInfo(name = "login_form") @SerializedName("login_form") val loginForm: ProviderLoginForm?,
        @ColumnInfo(name = "encryption") @SerializedName("encryption") val encryption: ProviderEncryption?
)