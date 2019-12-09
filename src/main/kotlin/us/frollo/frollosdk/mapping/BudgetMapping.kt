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

import us.frollo.frollosdk.model.api.budgets.BudgetPeriodResponse
import us.frollo.frollosdk.model.api.budgets.BudgetResponse
import us.frollo.frollosdk.model.coredata.budgets.Budget
import us.frollo.frollosdk.model.coredata.budgets.BudgetPeriod

internal fun BudgetResponse.toBudget(): Budget = Budget(
        budgetId = this.budgetId,
        isCurrent = this.isCurrent,
        imageUrl = this.imageUrl,
        trackingStatus = this.trackingStatus,
        status = this.status,
        frequency = this.frequency,
        userId = this.userId,
        currency = this.currency,
        currentAmount = this.currentAmount,
        periodAmount = this.periodAmount,
        startDate = this.startDate,
        type = this.type,
        typeValue = this.typeValue,
        periodsCount = this.periodsCount,
        metadata = this.metadata,
        currentPeriod = this.currentPeriod?.toBudgetPeriod())

internal fun BudgetPeriodResponse.toBudgetPeriod(): BudgetPeriod = BudgetPeriod(
        budgetPeriodId = this.budgetPeriodId,
        budgetId = this.budgetId,
        startDate = this.startDate,
        endDate = this.endDate,
        currentAmount = this.currentAmount,
        targetAmount = this.targetAmount,
        requiredAmount = this.requiredAmount,
        trackingStatus = this.trackingStatus,
        index = this.index)
