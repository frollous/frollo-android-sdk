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
import us.frollo.frollosdk.model.coredata.goals.Goal
import us.frollo.frollosdk.model.coredata.goals.GoalRelation

@Dao
internal interface GoalDao {

    @Query("SELECT * FROM goal")
    fun load(): LiveData<List<Goal>>

    @Query("SELECT * FROM goal WHERE goal_id = :goalId")
    fun load(goalId: Long): LiveData<Goal?>

    @RawQuery(observedEntities = [Goal::class])
    fun loadByQuery(queryStr: SupportSQLiteQuery): LiveData<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: Goal): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: Goal): Long

    @RawQuery
    fun getIdsByQuery(queryStr: SupportSQLiteQuery): List<Long>

    @Query("SELECT goal_id FROM goal WHERE account_id IN (:accountIds)")
    fun getIdsByAccountIds(accountIds: LongArray): LongArray

    @Query("DELETE FROM goal WHERE goal_id IN (:goalIds)")
    fun deleteMany(goalIds: LongArray)

    @Query("DELETE FROM goal WHERE goal_id = :goalId")
    fun delete(goalId: Long)

    @Query("DELETE FROM goal")
    fun clear()

    // Relation methods

    @Transaction
    @Query("SELECT * FROM goal")
    fun loadWithRelation(): LiveData<List<GoalRelation>>

    @Transaction
    @Query("SELECT * FROM goal WHERE goal_id = :goalId")
    fun loadWithRelation(goalId: Long): LiveData<GoalRelation?>

    @Transaction
    @RawQuery(observedEntities = [GoalRelation::class])
    fun loadByQueryWithRelation(queryStr: SupportSQLiteQuery): LiveData<List<GoalRelation>>
}
