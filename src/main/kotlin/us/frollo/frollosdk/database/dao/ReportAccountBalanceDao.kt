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
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import us.frollo.frollosdk.model.coredata.reports.ReportAccountBalance
import us.frollo.frollosdk.model.coredata.reports.ReportAccountBalanceRelation

@Dao
internal interface ReportAccountBalanceDao {

    @Transaction
    @RawQuery(observedEntities = [ReportAccountBalance::class])
    fun loadWithRelation(queryStr: SupportSQLiteQuery): LiveData<List<ReportAccountBalanceRelation>>

    @RawQuery
    fun find(queryStr: SupportSQLiteQuery): MutableList<ReportAccountBalance>

    @RawQuery
    fun findStaleIds(queryStr: SupportSQLiteQuery): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg models: ReportAccountBalance): LongArray

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg models: ReportAccountBalance): Int

    @Query("DELETE FROM report_account_balance WHERE report_id IN (:reportIds)")
    fun deleteMany(reportIds: LongArray)

    @Query("DELETE FROM report_account_balance")
    fun clear()
}