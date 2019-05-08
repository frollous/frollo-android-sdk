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

package us.frollo.frollosdk.mapping

import us.frollo.frollosdk.model.api.aggregation.accounts.AccountResponse
import us.frollo.frollosdk.model.api.aggregation.merchants.MerchantResponse
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountResponse
import us.frollo.frollosdk.model.api.aggregation.providers.ProviderResponse
import us.frollo.frollosdk.model.api.aggregation.transactioncategories.TransactionCategoryResponse
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionResponse
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionsSummaryResponse
import us.frollo.frollosdk.model.api.aggregation.tags.TransactionTagResponse
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Account
import us.frollo.frollosdk.model.coredata.aggregation.merchants.Merchant
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccount
import us.frollo.frollosdk.model.coredata.aggregation.providers.Provider
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderContainerName
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategory
import us.frollo.frollosdk.model.coredata.aggregation.transactions.Transaction
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionsSummary
import us.frollo.frollosdk.model.coredata.aggregation.tags.TransactionTag

internal fun ProviderResponse.toProvider(): Provider =
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

internal fun ProviderAccountResponse.toProviderAccount(): ProviderAccount =
        ProviderAccount(
                providerAccountId = providerAccountId,
                providerId = providerId,
                editable = editable,
                refreshStatus = refreshStatus,
                loginForm = loginForm)

internal fun AccountResponse.toAccount(): Account =
        Account(
                accountId = accountId,
                accountName = accountName,
                accountNumber = accountNumber,
                bsb = bsb,
                nickName = nickName,
                providerAccountId = providerAccountId,
                providerName = providerName,
                aggregator = aggregator,
                aggregatorId = aggregatorId,
                holderProfile = holderProfile,
                accountStatus = accountStatus,
                attributes = attributes,
                included = included,
                favourite = favourite,
                hidden = hidden,
                refreshStatus = refreshStatus,
                currentBalance = currentBalance,
                availableBalance = availableBalance,
                availableCash = availableCash,
                availableCredit = availableCredit,
                totalCashLimit = totalCashLimit,
                totalCreditLine = totalCreditLine,
                interestTotal = interestTotal,
                apr = apr,
                interestRate = interestRate,
                amountDue = amountDue,
                minimumAmountDue = minimumAmountDue,
                lastPaymentAmount = lastPaymentAmount,
                lastPaymentDate = lastPaymentDate,
                dueDate = dueDate,
                endDate = endDate,
                balanceDetails = balanceDetails)

internal fun TransactionResponse.toTransaction(): Transaction =
        Transaction(
                transactionId = transactionId,
                accountId = accountId,
                amount = amount,
                baseType = baseType,
                billId = billId,
                billPaymentId = billPaymentId,
                categoryId = categoryId,
                merchant = merchant,
                budgetCategory = budgetCategory,
                description = description,
                included = included,
                memo = memo,
                postDate = postDate,
                status = status,
                transactionDate = transactionDate,
                userTags = userTags)

internal fun TransactionsSummaryResponse.toTransactionsSummary(): TransactionsSummary =
        TransactionsSummary(
                count = count,
                sum = sum)

internal fun TransactionCategoryResponse.toTransactionCategory(): TransactionCategory =
        TransactionCategory(
                transactionCategoryId = transactionCategoryId,
                userDefined = userDefined,
                name = name,
                categoryType = categoryType,
                defaultBudgetCategory = defaultBudgetCategory,
                iconUrl = iconUrl,
                placement = placement)

internal fun MerchantResponse.toMerchant(): Merchant =
        Merchant(
                merchantId = merchantId,
                name = name,
                merchantType = merchantType,
                smallLogoUrl = smallLogoUrl)

internal fun TransactionTagResponse.toTransactionTags(): TransactionTag =
        TransactionTag(name = name, count = count, lastUsedAt = lastUsedAt, createdAt = createdAt)