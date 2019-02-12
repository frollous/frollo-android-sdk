package us.frollo.frollosdk.model.coredata.aggregation.providers

import us.frollo.frollosdk.model.IAdapterModel

data class Provider(
        val providerId: Long,
        val providerName: String,
        val smallLogoUrl: String,
        val smallLogoRevision: Int,
        val providerStatus: ProviderStatus,
        val popular: Boolean,
        val containerNames: List<ProviderContainerName>,
        val loginUrl: String?,
        val largeLogoUrl: String?,
        val largeLogoRevision: Int?,
        val baseUrl: String?,
        val forgetPasswordUrl: String?,
        val oAuthSite: Boolean?,
        val authType: ProviderAuthType?,
        val mfaType: ProviderMFAType?,
        val helpMessage: String?,
        val loginHelpMessage: String?,
        val loginForm: ProviderLoginForm?,
        val encryption: ProviderEncryption?
): IAdapterModel