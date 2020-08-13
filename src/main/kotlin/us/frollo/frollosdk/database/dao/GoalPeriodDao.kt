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
import io.reactivex.Observable
import us.frollo.frollosdk.model.coredata.goals.GoalPeriod
import us.frollo.frollosdk.model.coredata.goals.GoalPeriodRelation

@Dao
internal interface GoalPeriodDao {

    @Query("SELECT * FROM goal_period WHERE goal_period_id = :goalPeriodId")
    fun load(goalPeriodId: Long): LiveData<GoalPeriod?>

    @Query("SELECT * FROM goal_period WHERE goal_id = :goalId")
    fun loadByGoalId(goalId: Long): LiveData<List<GoalPeriod>>

    @RawQuery(observedEntities = [GoalPeriod::class])
    fun loadByQuery(queryStr: SupportSQLiteQuery): LiveData<List<GoalPeriod>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: GoalPeriod): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: GoalPeriod): Long

    @Query("SELECT goal_period_id FROM goal_period WHERE goal_id IN (:goalIds)")
    fun getIdsByGoalIds(goalIds: LongArray): LongArray

    @Query("DELETE FROM goal_period WHERE goal_period_id IN (:goalPeriodIds)")
    fun deleteMany(goalPeriodIds: LongArray)

    @Query("DELETE FROM goal_period WHERE goal_period_id = :goalPeriodId")
    fun delete(goalPeriodId: Long)

    @Query("DELETE FROM goal_period")
    fun clear()

    // Relation methods

    @Transaction
    @Query("SELECT * FROM goal_period WHERE goal_period_id = :goalPeriodId")
    fun loadWithRelation(goalPeriodId: Long): LiveData<GoalPeriodRelation?>

    @Transaction
    @Query("SELECT * FROM goal_period WHERE goal_id = :goalId")
    fun loadByGoalIdWithRelation(goalId: Long): LiveData<List<GoalPeriodRelation>>

    @Transaction
    @RawQuery(observedEntities = [GoalPeriodRelation::class])
    fun loadByQueryWithRelation(queryStr: SupportSQLiteQuery): LiveData<List<GoalPeriodRelation>>

    /**
     * RxJava Return Types
     */

    @Query("SELECT * FROM goal_period WHERE goal_period_id = :goalPeriodId")
    fun loadRx(goalPeriodId: Long): Observable<GoalPeriod?>

    @Query("SELECT * FROM goal_period WHERE goal_id = :goalId")
    fun loadByGoalIdRx(goalId: Long): Observable<List<GoalPeriod>>

    @RawQuery(observedEntities = [GoalPeriod::class])
    fun loadByQueryRx(queryStr: SupportSQLiteQuery): Observable<List<GoalPeriod>>

    // Relation methods

    @Transaction
    @Query("SELECT * FROM goal_period WHERE goal_period_id = :goalPeriodId")
    fun loadWithRelationRx(goalPeriodId: Long): Observable<GoalPeriodRelation?>

    @Transaction
    @Query("SELECT * FROM goal_period WHERE goal_id = :goalId")
    fun loadByGoalIdWithRelationRx(goalId: Long): Observable<List<GoalPeriodRelation>>

    @Transaction
    @RawQuery(observedEntities = [GoalPeriodRelation::class])
    fun loadByQueryWithRelationRx(queryStr: SupportSQLiteQuery): Observable<List<GoalPeriodRelation>>
}
