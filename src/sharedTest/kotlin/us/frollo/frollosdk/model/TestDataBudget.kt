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

import com.google.gson.JsonObject
import us.frollo.frollosdk.model.api.budgets.BudgetPeriodResponse
import us.frollo.frollosdk.model.api.budgets.BudgetResponse
import us.frollo.frollosdk.model.coredata.budgets.BudgetType
import us.frollo.frollosdk.model.coredata.budgets.BudgetFrequency
import us.frollo.frollosdk.model.coredata.budgets.BudgetStatus
import us.frollo.frollosdk.model.coredata.budgets.BudgetTrackingStatus
import us.frollo.frollosdk.testutils.randomElement
import us.frollo.frollosdk.testutils.randomNumber
import us.frollo.frollosdk.testutils.randomString
import java.math.BigDecimal

internal fun testBudgetResponseData(
    budgetId: Long? = null,
    isCurrent: Boolean = true,
    frequency: BudgetFrequency? = null,
    status: BudgetStatus? = null,
    trackingStatus: BudgetTrackingStatus? = null,
    type: BudgetType? = null,
    typeValue: String? = null
): BudgetResponse {
    return BudgetResponse(
            budgetId = budgetId ?: randomNumber().toLong(),
            isCurrent = isCurrent,
            imageUrl = "https://example.com/image.png",
            trackingStatus = trackingStatus ?: BudgetTrackingStatus.values().randomElement(),
            status = status ?: BudgetStatus.values().randomElement(),
            frequency = frequency ?: BudgetFrequency.values().randomElement(),
            userId = randomNumber(IntRange(0, 1000)).toLong(),
            currency = "AUD",
            currentAmount = BigDecimal("7500.0"),
            periodAmount = BigDecimal("300.0"),
            startDate = "2019-01-02",
            type = type ?: BudgetType.values().randomElement(),
            typeValue = typeValue ?: randomString(5),
            periodsCount = 52,
            currentPeriod = testBudgetPeriodResponseData(),
            metadata = JsonObject().apply {
                addProperty("seen", true)
            })
}

internal fun testBudgetPeriodResponseData(
    budgetPeriodId: Long? = null,
    budgetId: Long? = null,
    trackingStatus: BudgetTrackingStatus? = null,
    fromDate: String? = null,
    toDate: String? = null
): BudgetPeriodResponse {
    return BudgetPeriodResponse(
            budgetPeriodId = budgetPeriodId ?: randomNumber().toLong(),
            budgetId = budgetId ?: randomNumber().toLong(),
            startDate = fromDate ?: "2019-02-01",
            endDate = toDate ?: "2020-01-31",
            trackingStatus = trackingStatus ?: BudgetTrackingStatus.values().randomElement(),
            currentAmount = BigDecimal("243.11"),
            targetAmount = BigDecimal("300.0"),
            requiredAmount = BigDecimal("355.0"),
            index = 0)
}