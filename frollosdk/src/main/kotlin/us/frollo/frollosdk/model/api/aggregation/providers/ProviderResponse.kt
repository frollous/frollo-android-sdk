package us.frollo.frollosdk.model.api.aggregation.providers

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.aggregation.providers.*

internal data class ProviderResponse(
        @SerializedName("id") val providerId: Long,
        @SerializedName("name") val providerName: String,
        @SerializedName("small_logo_url") val smallLogoUrl: String,
        @SerializedName("small_logo_revision") val smallLogoRevision: Int,
        @SerializedName("status") val providerStatus: ProviderStatus,
        @SerializedName("popular") val popular: Boolean,
        @SerializedName("container_names") val containerNames: List<String>,
        @SerializedName("login_url") val loginUrl: String?,

        @SerializedName("large_logo_url") val largeLogoUrl: String?,
        @SerializedName("large_logo_revision") val largeLogoRevision: Int?,
        @SerializedName("base_url") val baseUrl: String?,
        @SerializedName("forget_password_url") val forgetPasswordUrl: String?,
        @SerializedName("o_auth_site") val oAuthSite: Boolean?,
        @SerializedName("auth_type") val authType: ProviderAuthType?,
        @SerializedName("mfa_type") val mfaType: ProviderMFAType?,
        @SerializedName("help_message") val helpMessage: String?,
        @SerializedName("login_help_message") val loginHelpMessage: String?,
        @SerializedName("login_form") val loginForm: ProviderLoginForm?,
        @SerializedName("encryption") val encryption: ProviderEncryption?
)