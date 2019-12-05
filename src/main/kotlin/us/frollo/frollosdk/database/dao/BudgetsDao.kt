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
import us.frollo.frollosdk.model.coredata.budgets.Budget
import us.frollo.frollosdk.model.coredata.budgets.BudgetRelation

@Dao
internal interface BudgetsDao {

    @Query("SELECT * FROM budget")
    fun load(): LiveData<List<Budget>>

    @Query("SELECT * FROM budget WHERE budget_id = :budgetId")
    fun load(budgetId: Long): LiveData<Budget?>

    @RawQuery(observedEntities = [Budget::class])
    fun loadByQuery(queryStr: SupportSQLiteQuery): LiveData<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: Budget): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: Budget): Long

    @RawQuery
    fun getIdsByQuery(queryStr: SupportSQLiteQuery): List<Long>

    @Query("DELETE FROM budget WHERE budget_id IN (:budgetId)")
    fun deleteMany(budgetId: LongArray)

    @Query("DELETE FROM budget WHERE budget_id = :budgetId")
    fun delete(budgetId: Long)

    @Query("DELETE FROM budget")
    fun clear()

    // Relation methods

    @Transaction
    @Query("SELECT * FROM budget")
    fun loadWithRelation(): LiveData<List<BudgetRelation>>

    @Transaction
    @Query("SELECT * FROM budget WHERE budget_id = :budgetId")
    fun loadWithRelation(budgetId: Long): LiveData<BudgetRelation?>

    @Transaction
    @RawQuery(observedEntities = [BudgetRelation::class])
    fun loadByQueryWithRelation(queryStr: SupportSQLiteQuery): LiveData<List<BudgetRelation>>
}