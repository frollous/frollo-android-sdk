package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountResponse
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshAdditionalStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshSubStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.RefreshStatus
import us.frollo.frollosdk.testutils.randomNumber

internal fun testProviderAccountResponseData(providerAccountId: Long? = null, providerId: Long? = null) : ProviderAccountResponse {

    val refreshStatus = RefreshStatus(
            status = AccountRefreshStatus.NEEDS_ACTION,
            subStatus = AccountRefreshSubStatus.INPUT_REQUIRED,
            additionalStatus = AccountRefreshAdditionalStatus.MFA_NEEDED,
            lastRefreshed = "2019-01-01",
            nextRefresh = "2019-01-01")

    return ProviderAccountResponse(
            providerAccountId = providerAccountId ?: randomNumber().toLong(),
            providerId = providerId ?: randomNumber().toLong(),
            editable = true,
            refreshStatus = refreshStatus,
            loginForm = null)
}