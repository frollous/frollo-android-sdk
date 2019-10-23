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

import us.frollo.frollosdk.model.api.goals.GoalPeriodResponse
import us.frollo.frollosdk.model.api.goals.GoalResponse
import us.frollo.frollosdk.model.coredata.goals.Goal
import us.frollo.frollosdk.model.coredata.goals.GoalPeriod

internal fun GoalResponse.toGoal(): Goal =
        Goal(
                goalId = goalId,
                name = name,
                description = description,
                imageUrl = imageUrl,
                accountId = accountId,
                type = type,
                subType = subType,
                trackingStatus = trackingStatus,
                trackingType = trackingType,
                status = status,
                frequency = frequency,
                target = target,
                currency = currency,
                currentAmount = currentAmount,
                periodAmount = periodAmount,
                startAmount = startAmount,
                targetAmount = targetAmount,
                startDate = startDate,
                endDate = endDate,
                estimatedEndDate = estimatedEndDate,
                estimatedTargetAmount = estimatedTargetAmount,
                periodsCount = periodsCount,
                currentPeriod = currentPeriod?.toGoalPeriod())

internal fun GoalPeriodResponse.toGoalPeriod(): GoalPeriod =
        GoalPeriod(
                goalPeriodId = goalPeriodId,
                goalId = goalId,
                startDate = startDate,
                endDate = endDate,
                trackingStatus = trackingStatus,
                currentAmount = currentAmount,
                targetAmount = targetAmount,
                requiredAmount = requiredAmount,
                index = index)