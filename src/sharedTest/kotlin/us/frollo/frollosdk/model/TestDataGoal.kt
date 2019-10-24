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
import us.frollo.frollosdk.model.api.goals.GoalCreateRequest
import us.frollo.frollosdk.model.api.goals.GoalPeriodResponse
import us.frollo.frollosdk.model.api.goals.GoalResponse
import us.frollo.frollosdk.model.coredata.goals.GoalFrequency
import us.frollo.frollosdk.model.coredata.goals.GoalStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTarget
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingType
import us.frollo.frollosdk.testutils.randomElement
import us.frollo.frollosdk.testutils.randomNumber
import us.frollo.frollosdk.testutils.randomString
import java.math.BigDecimal

internal fun testGoalResponseData(
    goalId: Long? = null,
    accountId: Long? = null,
    frequency: GoalFrequency? = null,
    status: GoalStatus? = null,
    trackingStatus: GoalTrackingStatus? = null
): GoalResponse {
    return GoalResponse(
            goalId = goalId ?: randomNumber().toLong(),
            name = randomString(20),
            description = randomString(100),
            imageUrl = "https://example.com/image.png",
            accountId = accountId ?: randomNumber().toLong(),
            trackingStatus = trackingStatus ?: GoalTrackingStatus.values().randomElement(),
            trackingType = GoalTrackingType.values().randomElement(),
            status = status ?: GoalStatus.values().randomElement(),
            frequency = frequency ?: GoalFrequency.values().randomElement(),
            target = GoalTarget.values().randomElement(),
            currency = "AUD",
            currentAmount = BigDecimal("7500.0"),
            periodAmount = BigDecimal("300.0"),
            startAmount = BigDecimal("0.0"),
            targetAmount = BigDecimal("20000.0"),
            startDate = "2019-01-02",
            endDate = "2019-11-02",
            estimatedEndDate = "2019-12-02",
            estimatedTargetAmount = BigDecimal("20000.0"),
            periodsCount = 52,
            currentPeriod = testGoalPeriodResponseData(),
            metadata = JsonObject().apply {
                addProperty("seen", true)
            })
}

internal fun testGoalPeriodResponseData(
    goalPeriodId: Long? = null,
    goalId: Long? = null,
    trackingStatus: GoalTrackingStatus? = null
): GoalPeriodResponse {
    return GoalPeriodResponse(
            goalPeriodId = goalPeriodId ?: randomNumber().toLong(),
            goalId = goalId ?: randomNumber().toLong(),
            startDate = "2019-02-01",
            endDate = "2020-01-31",
            trackingStatus = trackingStatus ?: GoalTrackingStatus.values().randomElement(),
            currentAmount = BigDecimal("243.11"),
            targetAmount = BigDecimal("300.0"),
            requiredAmount = BigDecimal("355.0"),
            index = 0)
}

internal fun testGoalRequestTargetData(
    target: GoalTarget,
    targetAmount: BigDecimal? = null,
    periodAmount: BigDecimal? = null,
    endDate: String? = null
): GoalCreateRequest {
    return GoalCreateRequest(
            accountId = randomNumber().toLong(),
            description = randomString(200),
            endDate = endDate,
            frequency = GoalFrequency.MONTHLY,
            imageUrl = "https://example.com/image.png",
            name = randomString(20),
            periodAmount = periodAmount,
            startAmount = BigDecimal(0),
            startDate = "2018-10-01",
            target = target,
            targetAmount = targetAmount,
            trackingType = GoalTrackingType.values().randomElement(),
            metadata = JsonObject().apply {
                addProperty("seen", true)
            })
}