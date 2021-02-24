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

import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionResponse
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionsSummaryResponse
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Balance
import us.frollo.frollosdk.model.coredata.aggregation.merchants.MerchantDetails
import us.frollo.frollosdk.model.coredata.aggregation.tags.TransactionTag
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.CategoryDetails
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionBaseType
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionDescription
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionStatus
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.testutils.randomNumber
import us.frollo.frollosdk.testutils.randomString
import us.frollo.frollosdk.testutils.randomUUID
import java.math.BigDecimal
import java.util.Date

internal fun testTransactionResponseData(
    transactionId: Long? = null,
    accountId: Long? = null,
    categoryId: Long? = null,
    merchantId: Long? = null,
    transactionDate: String? = null,
    included: Boolean? = null,
    userTags: List<String>? = null,
    amount: BigDecimal? = null,
    description: TransactionDescription? = null,
    budgetCategory: BudgetCategory? = null,
    baseType: TransactionBaseType? = null,
    status: TransactionStatus? = null
): TransactionResponse {
    return TransactionResponse(
        transactionId = transactionId ?: randomNumber(10000..20000).toLong(),
        accountId = accountId ?: randomNumber(10000..20000).toLong(),
        amount = if (amount != null) Balance(amount, "AUD") else Balance(amount = BigDecimal(1111), currency = "AUD"),
        baseType = baseType ?: TransactionBaseType.UNKNOWN,
        billId = randomNumber().toLong(),
        billPaymentId = randomNumber().toLong(),
        category = testCategoryDetails(categoryId),
        merchant = testMerchantDetails(merchantId),
        budgetCategory = budgetCategory ?: BudgetCategory.ONE_OFF,
        description = description ?: TransactionDescription(original = randomUUID(), user = null, simple = null),
        included = included ?: false,
        memo = randomUUID(),
        postDate = "2019-01-01",
        status = status ?: TransactionStatus.SCHEDULED,
        transactionDate = transactionDate ?: "2019-01-01",
        userTags = userTags,
        externalId = randomString(8),
        goalId = randomNumber().toLong()
    )
}

internal fun testMerchantDetails(merchantId: Long? = null): MerchantDetails =
    MerchantDetails(
        id = merchantId ?: randomNumber(10000..20000).toLong(),
        name = randomUUID(),
        phone = randomUUID(),
        website = randomUUID(),
        location = null
    )

internal fun testTransactionsSummaryResponseData(count: Long? = null, sum: BigDecimal? = null): TransactionsSummaryResponse =
    TransactionsSummaryResponse(
        count = count ?: randomNumber().toLong(),
        sum = sum ?: randomNumber().toBigDecimal()
    )

internal fun testTransactionTagData(name: String? = null, createdAt: String? = null, lastUsedAt: String? = null): TransactionTag =
    TransactionTag(
        name = name ?: randomString(8),
        createdAt = createdAt ?: Date().toString(),
        lastUsedAt = lastUsedAt ?: Date().toString(),
        count = randomNumber().toLong()
    )

internal fun testCategoryDetails(categoryId: Long? = null): CategoryDetails =
    CategoryDetails(
        id = categoryId ?: randomNumber(10000..20000).toLong(),
        name = randomUUID(),
        imageUrl = randomUUID()
    )
