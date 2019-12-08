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

internal fun BudgetResponse.toBudget(): Budget = Budget(this.budgetId,
        this.isCurrent,
        this.imageUrl,
        this.trackingStatus,
        this.status,
        this.frequency,
        this.userId,
        this.currency,
        this.currentAmount,
        this.periodAmount,
        this.startDate,
        this.type,
        this.typeValue,
        this.periodsCount,
        this.metadata,
        this.currentPeriod?.toBudgetPeriod())

internal fun BudgetPeriodResponse.toBudgetPeriod(): BudgetPeriod = BudgetPeriod(this.budgetPeriodId,
        this.budgetId,
        this.startDate,
        this.endDate,
        this.currentAmount,
        this.targetAmount,
        this.requiredAmount,
        this.trackingStatus,
        this.index)
