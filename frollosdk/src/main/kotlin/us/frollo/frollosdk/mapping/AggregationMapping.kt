package us.frollo.frollosdk.mapping

import us.frollo.frollosdk.model.api.aggregation.providers.ProviderResponse
import us.frollo.frollosdk.model.coredata.aggregation.providers.Provider
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderContainerName

internal fun ProviderResponse.toProvider(): Provider? =
        Provider(
                providerId = providerId,
                providerName = providerName,
                smallLogoUrl = smallLogoUrl,
                smallLogoRevision = smallLogoRevision,
                providerStatus = providerStatus,
                popular = popular,
                containerNames = containerNames.map { ProviderContainerName.valueOf(it.toUpperCase()) }.toList(),
                loginUrl = loginUrl,
                largeLogoUrl = largeLogoUrl,
                largeLogoRevision = largeLogoRevision,
                baseUrl = baseUrl,
                forgetPasswordUrl = forgetPasswordUrl,
                oAuthSite = oAuthSite,
                authType = authType,
                mfaType = mfaType,
                helpMessage = helpMessage,
                loginHelpMessage = loginHelpMessage,
                loginForm = loginForm,
                encryption = encryption)