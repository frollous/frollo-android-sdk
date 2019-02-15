package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionResponse
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Balance
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionBaseType
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionDescription
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionStatus
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.testutils.randomBoolean
import us.frollo.frollosdk.testutils.randomNumber
import us.frollo.frollosdk.testutils.randomUUID
import kotlin.random.Random

internal fun testTransactionResponseData(transactionId: Long? = null, accountId: Long? = null) : TransactionResponse {
    return TransactionResponse(
            transactionId = transactionId ?: randomNumber().toLong(),
            accountId = accountId ?: randomNumber().toLong(),
            amount = Balance(amount = randomNumber().toBigDecimal(), currency = "AUD"),
            baseType = TransactionBaseType.values()[Random.nextInt(TransactionBaseType.values().size)],
            billId = randomNumber().toLong(),
            billPaymentId = randomNumber().toLong(),
            categoryId = randomNumber().toLong(),
            merchantId = randomNumber().toLong(),
            budgetCategory = BudgetCategory.values()[Random.nextInt(BudgetCategory.values().size)],
            description = TransactionDescription(original = randomUUID(), user = null, simple = null),
            included = randomBoolean(),
            memo = randomUUID(),
            postDate = "2019-01-01",
            status = TransactionStatus.values()[Random.nextInt(TransactionStatus.values().size)],
            transactionDate = "2019-01-01")
}