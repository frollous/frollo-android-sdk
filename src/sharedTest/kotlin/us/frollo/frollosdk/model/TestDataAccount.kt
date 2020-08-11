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

import us.frollo.frollosdk.model.api.aggregation.accounts.AccountResponse
import us.frollo.frollosdk.model.api.aggregation.accounts.AccountUpdateRequest
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountAttributes
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountClassification
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountFeature
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountFeatureDetail
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountGroup
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountStatus
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountSubType
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountType
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Balance
import us.frollo.frollosdk.model.coredata.aggregation.accounts.BalanceDetails
import us.frollo.frollosdk.model.coredata.aggregation.accounts.BalanceTier
import us.frollo.frollosdk.model.coredata.aggregation.accounts.HolderProfile
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshAdditionalStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshSubStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.RefreshStatus
import us.frollo.frollosdk.testutils.randomNumber
import us.frollo.frollosdk.testutils.randomString
import us.frollo.frollosdk.testutils.randomUUID
import java.math.BigDecimal
import kotlin.random.Random

internal fun testAccountResponseData(accountId: Long? = null, providerAccountId: Long? = null, accountType: AccountType? = null, accountRefreshStatus: AccountRefreshStatus? = null, included: Boolean? = null): AccountResponse {

    val balanceDetails = BalanceDetails(
        currentDescription = randomUUID(),
        tiers = listOf(BalanceTier(description = randomUUID(), min = randomNumber(), max = randomNumber()))
    )

    val holderProfile = HolderProfile(name = "Jacob Frollo")

    val refreshStatus = RefreshStatus(
        status = accountRefreshStatus ?: AccountRefreshStatus.NEEDS_ACTION,
        subStatus = AccountRefreshSubStatus.INPUT_REQUIRED,
        additionalStatus = AccountRefreshAdditionalStatus.MFA_NEEDED,
        lastRefreshed = "2019-01-01",
        nextRefresh = "2019-01-01"
    )

    val attributes = AccountAttributes(
        accountType = accountType ?: AccountType.values()[Random.nextInt(AccountType.values().size)],
        classification = AccountClassification.values()[Random.nextInt(AccountClassification.values().size)],
        accountSubType = AccountSubType.values()[Random.nextInt(AccountSubType.values().size)],
        group = AccountGroup.values()[Random.nextInt(AccountGroup.values().size)]
    )

    return AccountResponse(
        accountId = accountId ?: randomNumber().toLong(),
        providerAccountId = providerAccountId ?: randomNumber().toLong(),
        refreshStatus = refreshStatus,
        attributes = attributes,
        accountName = randomUUID(),
        accountStatus = AccountStatus.ACTIVE,
        favourite = true,
        hidden = false,
        included = included ?: true,
        providerName = "Detailed Test Provider",
        amountDue = Balance(amount = randomNumber().toBigDecimal(), currency = "AUD"),
        apr = BigDecimal("18.53"),
        availableBalance = Balance(amount = randomNumber().toBigDecimal(), currency = "AUD"),
        availableCash = Balance(amount = randomNumber().toBigDecimal(), currency = "AUD"),
        availableCredit = Balance(amount = randomNumber().toBigDecimal(), currency = "AUD"),
        balanceDetails = balanceDetails,
        currentBalance = Balance(amount = randomNumber().toBigDecimal(), currency = "AUD"),
        dueDate = "2019-01-01",
        holderProfile = holderProfile,
        interestRate = BigDecimal("3.05"),
        lastPaymentAmount = Balance(amount = randomNumber().toBigDecimal(), currency = "AUD"),
        lastPaymentDate = "2019-01-01",
        minimumAmountDue = Balance(amount = randomNumber().toBigDecimal(), currency = "AUD"),
        nickName = "Friendly Name",
        totalCashLimit = Balance(amount = randomNumber().toBigDecimal(), currency = "AUD"),
        totalCreditLine = Balance(amount = randomNumber().toBigDecimal(), currency = "AUD"),
        accountNumber = randomUUID(),
        aggregator = randomUUID(),
        bsb = randomUUID(),
        interestTotal = Balance(amount = randomNumber().toBigDecimal(), currency = "AUD"),
        endDate = randomUUID(),
        goalIds = null,
        externalId = randomString(8),
        features = testAccountFeaturesData(),
        productsAvailable = false,
        cdrProduct = null
    )
}

internal fun testAccountFeaturesData(): List<AccountFeature> {
    return listOf(
        AccountFeature(
            featureId = "payments",
            name = "Payments",
            imageUrl = "https://image.png",
            details = testAccountFeatureDetailsData()
        ),
        AccountFeature(
            featureId = "transfers",
            name = "Transfers",
            imageUrl = null,
            details = listOf(
                AccountFeatureDetail(
                    detailId = "internal_transfer",
                    name = "Transfer",
                    imageUrl = null
                )
            )
        ),
        AccountFeature(
            featureId = "statements",
            name = "Statements",
            imageUrl = null,
            details = null
        )
    )
}

internal fun testAccountFeatureDetailsData(): List<AccountFeatureDetail> {
    return listOf(
        AccountFeatureDetail(
            detailId = "bpay",
            name = "BPAY",
            imageUrl = "https://image-detail.png"
        ),
        AccountFeatureDetail(
            detailId = "npp",
            name = "PayID",
            imageUrl = null
        )
    )
}

internal fun testUpdateRequestData(hidden: Boolean = false, included: Boolean = true): AccountUpdateRequest {
    return AccountUpdateRequest(
        hidden = hidden,
        included = included,
        favourite = true,
        accountSubType = AccountSubType.BANK_ACCOUNT,
        nickName = "Friendly Name"
    )
}
