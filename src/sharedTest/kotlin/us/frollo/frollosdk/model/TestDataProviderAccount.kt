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