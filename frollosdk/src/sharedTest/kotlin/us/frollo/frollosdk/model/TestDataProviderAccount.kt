package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountResponse
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshAdditionalStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshSubStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.RefreshStatus
import us.frollo.frollosdk.testutils.randomNumber
import java.util.*

internal fun testProviderAccountResponseData(providerAccountId: Long? = null, providerId: Long? = null) : ProviderAccountResponse {

    val refreshStatus = RefreshStatus(
            status = AccountRefreshStatus.NEEDS_ACTION,
            subStatus = AccountRefreshSubStatus.INPUT_REQUIRED,
            additionalStatus = AccountRefreshAdditionalStatus.MFA_NEEDED,
            lastRefreshed = Date(),
            nextRefresh = Date())

    return ProviderAccountResponse(
            providerAccountId = providerAccountId ?: randomNumber().toLong(),
            providerId = providerId ?: randomNumber().toLong(),
            editable = true,
            refreshStatus = refreshStatus,
            loginForm = null)
}