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

import androidx.room.*
import us.frollo.frollosdk.model.coredata.reports.ReportGroupTransactionHistory

@Dao
internal interface ReportGroupTransactionHistoryDao {

    @Query("SELECT * FROM report_group_transaction_history WHERE report_id = :reportId AND linked_id IN (:linkedIds)")
    fun find(reportId: Long, linkedIds: LongArray): MutableList<ReportGroupTransactionHistory>

    @Query("SELECT report_group_id FROM report_group_transaction_history WHERE report_id = :reportId AND linked_id NOT IN (:linkedIds)")
    fun findStaleIds(reportId: Long, linkedIds: LongArray): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: ReportGroupTransactionHistory): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: ReportGroupTransactionHistory): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg models: ReportGroupTransactionHistory): Int

    @Query("DELETE FROM report_group_transaction_history WHERE report_group_id IN (:reportGroupIds)")
    fun deleteMany(reportGroupIds: LongArray)

    @Query("DELETE FROM report_group_transaction_history WHERE report_id IN (:reportIds)")
    fun deleteByReportIds(reportIds: LongArray)

    @Query("DELETE FROM report_group_transaction_history")
    fun clear()
}