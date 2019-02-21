package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.api.aggregation.transactioncategories.TransactionCategoryResponse
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategoryType
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.testutils.randomBoolean
import us.frollo.frollosdk.testutils.randomNumber
import us.frollo.frollosdk.testutils.randomUUID
import kotlin.random.Random

internal fun testTransactionCategoryResponseData(transactionCategoryId: Long? = null) : TransactionCategoryResponse {
    return TransactionCategoryResponse(
            transactionCategoryId = transactionCategoryId ?: randomNumber().toLong(),
            categoryType = TransactionCategoryType.values()[Random.nextInt(TransactionCategoryType.values().size)],
            defaultBudgetCategory = BudgetCategory.values()[Random.nextInt(BudgetCategory.values().size)],
            name = randomUUID(),
            userDefined = randomBoolean(),
            placement = 1,
            iconUrl = "https://example.com/category.png")
}