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

import us.frollo.frollosdk.model.api.aggregation.transactioncategories.TransactionCategoryResponse
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategoryType
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.testutils.randomBoolean
import us.frollo.frollosdk.testutils.randomNumber
import us.frollo.frollosdk.testutils.randomUUID
import kotlin.random.Random

internal fun testTransactionCategoryResponseData(transactionCategoryId: Long? = null): TransactionCategoryResponse {
    return TransactionCategoryResponse(
        transactionCategoryId = transactionCategoryId ?: randomNumber().toLong(),
        categoryType = TransactionCategoryType.values()[Random.nextInt(TransactionCategoryType.values().size)],
        defaultBudgetCategory = BudgetCategory.values()[Random.nextInt(BudgetCategory.values().size)],
        name = randomUUID(),
        userDefined = randomBoolean(),
        placement = 1,
        iconUrl = "https://example.com/category.png"
    )
}
