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
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportTransactionCurrent
import us.frollo.frollosdk.model.coredata.reports.ReportTransactionCurrentRelation
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory

@Dao
internal interface ReportTransactionCurrentDao {

    @androidx.room.Transaction
    @Query("SELECT * FROM report_transaction_current WHERE report_grouping = :grouping AND filtered_budget_category = :budgetCategory")
    fun load(grouping: ReportGrouping, budgetCategory: BudgetCategory?): LiveData<List<ReportTransactionCurrentRelation>>

    @Query("SELECT * FROM report_transaction_current WHERE report_grouping = :grouping AND filtered_budget_category = :budgetCategory AND linked_id = :linkedId AND day IN (:days)")
    fun find(grouping: ReportGrouping, budgetCategory: BudgetCategory?, linkedId: Long?, days: IntArray): MutableList<ReportTransactionCurrent>

    @Query("SELECT report_id FROM report_transaction_current WHERE report_grouping = :grouping AND filtered_budget_category = :budgetCategory AND linked_id = :linkedId AND day NOT IN (:days)")
    fun findStaleIds(grouping: ReportGrouping, budgetCategory: BudgetCategory?, linkedId: Long?, days: IntArray): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: ReportTransactionCurrent): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: ReportTransactionCurrent): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAll(vararg models: ReportTransactionCurrent): Int

    @Query("DELETE FROM report_transaction_current WHERE report_id IN (:reportIds)")
    fun deleteMany(reportIds: LongArray)

    @Transaction
    fun insertAndDeleteInTransaction(new: List<ReportTransactionCurrent>, existing: List<ReportTransactionCurrent>, staleIds: List<Long>) {
        // Anything inside this method runs in a single transaction.
        insertAll(*new.toTypedArray())
        updateAll(*existing.toTypedArray())
        deleteMany(staleIds.toLongArray())
    }

    @Query("DELETE FROM report_transaction_current")
    fun clear()
}