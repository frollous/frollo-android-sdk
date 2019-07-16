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

package us.frollo.frollosdk.model.coredata.goals

import androidx.room.Embedded
import androidx.room.Relation
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Account
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountRelation

data class GoalRelation(

    @Embedded
    var goal: Goal? = null,

    @Relation(parentColumn = "goal_id", entityColumn = "goal_id", entity = GoalPeriod::class)
    var periods: List<GoalPeriod>? = null,

    @Relation(parentColumn = "account_id", entityColumn = "account_id", entity = Account::class)
    var accounts: List<AccountRelation>? = null

) : IAdapterModel {

    val account: AccountRelation?
        get() {
            val models = accounts
            return if (models?.isNotEmpty() == true) models[0] else null
        }
}