package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.api.aggregation.accounts.AccountResponse
import us.frollo.frollosdk.model.api.aggregation.accounts.AccountUpdateRequest
import us.frollo.frollosdk.model.coredata.aggregation.accounts.*
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshAdditionalStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshSubStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.RefreshStatus
import us.frollo.frollosdk.testutils.randomNumber
import us.frollo.frollosdk.testutils.randomUUID
import java.math.BigDecimal
import kotlin.random.Random

internal fun testAccountResponseData(accountId: Long? = null, providerAccountId: Long? = null) : AccountResponse {

    val balanceDetails = BalanceDetails(
            currentDescription = randomUUID(),
            tiers = listOf(BalanceTier(description = randomUUID(), min = randomNumber(), max = randomNumber())))

    val holderProfile = HolderProfile(name = "Jacob Frollo")

    val refreshStatus = RefreshStatus(
            status = AccountRefreshStatus.NEEDS_ACTION,
            subStatus = AccountRefreshSubStatus.INPUT_REQUIRED,
            additionalStatus = AccountRefreshAdditionalStatus.MFA_NEEDED,
            lastRefreshed = "2019-01-01",
            nextRefresh = "2019-01-01")

    val attributes = AccountAttributes(
            accountType = AccountType.values()[Random.nextInt(AccountType.values().size)],
            classification = AccountClassification.values()[Random.nextInt(AccountClassification.values().size)],
            accountSubType = AccountSubType.values()[Random.nextInt(AccountSubType.values().size)],
            group = AccountGroup.values()[Random.nextInt(AccountGroup.values().size)])

    return AccountResponse(
            accountId = accountId ?: randomNumber().toLong(),
            providerAccountId = providerAccountId ?: randomNumber().toLong(),
            refreshStatus = refreshStatus,
            attributes = attributes,
            accountName = randomUUID(),
            accountStatus = AccountStatus.ACTIVE,
            favourite = true,
            hidden = false,
            included = true,
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
            aggregatorId = randomNumber().toLong(),
            bsb = randomUUID(),
            interestTotal = Balance(amount = randomNumber().toBigDecimal(), currency = "AUD"),
            endDate = randomUUID())
}

internal fun testUpdateRequestData(hidden: Boolean = false, included: Boolean = true): AccountUpdateRequest {
    return AccountUpdateRequest(
            hidden = hidden,
            included = included,
            favourite = true,
            accountSubType = AccountSubType.BANK_ACCOUNT,
            nickName = "Friendly Name")
}