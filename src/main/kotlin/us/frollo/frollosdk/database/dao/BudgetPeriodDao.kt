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

package us.frollo.frollosdk.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery
import us.frollo.frollosdk.model.coredata.budgets.BudgetPeriod
import us.frollo.frollosdk.model.coredata.budgets.BudgetPeriodRelation

@Dao
internal interface BudgetPeriodDao {

    @Query("SELECT * FROM budget_period WHERE budget_period_id = :budgetPeriodId")
    fun load(budgetPeriodId: Long): LiveData<BudgetPeriod?>

    @Query("SELECT * FROM budget_period WHERE budget_id = :budgetId")
    fun loadByBudgetId(budgetId: Long): LiveData<List<BudgetPeriod>>

    @RawQuery(observedEntities = [BudgetPeriod::class])
    fun loadByQuery(queryStr: SupportSQLiteQuery): LiveData<List<BudgetPeriod>>

    @RawQuery
    fun getIds(queryStr: SupportSQLiteQuery): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: BudgetPeriod): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: BudgetPeriod): Long

    @Query("SELECT budget_period_id FROM budget_period WHERE budget_id IN (:budgetIds)")
    fun getIdsByBudgetIds(budgetIds: LongArray): LongArray

    @Query("DELETE FROM budget_period WHERE budget_period_id IN (:budgetPeriodIds)")
    fun deleteMany(budgetPeriodIds: LongArray)

    @Query("DELETE FROM budget_period WHERE budget_period_id = :budgetPeriodId")
    fun delete(budgetPeriodId: Long)

    @Query("DELETE FROM budget_period")
    fun clear()

    // Relation methods

    @Transaction
    @Query("SELECT * FROM budget_period WHERE budget_period_id = :budgetPeriodId")
    fun loadWithRelation(budgetPeriodId: Long): LiveData<BudgetPeriodRelation?>

    @Transaction
    @Query("SELECT * FROM budget_period WHERE budget_id = :budgetId")
    fun loadByBudgetIdWithRelation(budgetId: Long): LiveData<List<BudgetPeriodRelation>>

    @Transaction
    @RawQuery(observedEntities = [BudgetPeriodRelation::class])
    fun loadByQueryWithRelation(queryStr: SupportSQLiteQuery): LiveData<List<BudgetPeriodRelation>>
}