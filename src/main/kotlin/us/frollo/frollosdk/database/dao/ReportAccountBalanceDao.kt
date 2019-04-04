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
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountType
import us.frollo.frollosdk.model.coredata.reports.ReportAccountBalance
import us.frollo.frollosdk.model.coredata.reports.ReportAccountBalanceRelation
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod

@Dao
internal interface ReportAccountBalanceDao {

    @androidx.room.Transaction
    @Query("SELECT * FROM report_account_balance WHERE (date BETWEEN :fromDate AND :toDate) AND period = :period")
    fun load(fromDate: String, toDate: String, period: ReportPeriod): LiveData<List<ReportAccountBalanceRelation>>

    @androidx.room.Transaction
    @Query("SELECT * FROM report_account_balance WHERE (date BETWEEN :fromDate AND :toDate) AND period = :period AND account_id = :accountId")
    fun loadByAccountId(fromDate: String, toDate: String, period: ReportPeriod, accountId: Long): LiveData<List<ReportAccountBalanceRelation>>

    @androidx.room.Transaction
    @Query("SELECT rab.* " +
            "FROM report_account_balance AS rab " +
            "LEFT JOIN account AS a ON rab.account_id = a.account_id " +
            "WHERE (rab.date BETWEEN :fromDate AND :toDate) AND rab.period = :period AND a.attr_account_type = :accountType")
    fun loadByAccountType(fromDate: String, toDate: String, period: ReportPeriod, accountType: AccountType): LiveData<List<ReportAccountBalanceRelation>>

    @androidx.room.Transaction
    @Query("SELECT rab.* " +
            "FROM report_account_balance AS rab " +
            "LEFT JOIN account AS a ON rab.account_id = a.account_id " +
            "WHERE (rab.date BETWEEN :fromDate AND :toDate) AND rab.period = :period AND rab.account_id = :accountId AND a.attr_account_type = :accountType")
    fun loadByAccountIdAndAccountType(fromDate: String, toDate: String, period: ReportPeriod, accountId: Long, accountType: AccountType): LiveData<List<ReportAccountBalanceRelation>>

    //@Query("SELECT * FROM report_account_balance WHERE (date BETWEEN :fromDate AND :toDate) AND period = :period AND account_id = :accountId AND date IN (:dates)")
    //fun find(fromDate: String, toDate: String, period: ReportPeriod, accountId: Long, dates: Array<String>): MutableList<ReportAccountBalance>

    //@Query("SELECT report_id FROM report_account_balance WHERE report_grouping = :grouping AND filtered_budget_category IS :budgetCategory AND linked_id IS :linkedId AND day NOT IN (:days)")
    //fun findStaleIds(grouping: ReportGrouping, budgetCategory: BudgetCategory?, linkedId: Long?, days: IntArray): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: ReportAccountBalance): LongArray

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAll(vararg models: ReportAccountBalance): Int

    @Query("DELETE FROM report_account_balance WHERE report_id IN (:reportIds)")
    fun deleteMany(reportIds: LongArray)

    @Transaction
    fun insertAndDeleteInTransaction(new: List<ReportAccountBalance>, existing: List<ReportAccountBalance>, staleIds: List<Long>) {
        // Anything inside this method runs in a single transaction.
        insertAll(*new.toTypedArray())
        updateAll(*existing.toTypedArray())
        deleteMany(staleIds.toLongArray())
    }

    @Query("DELETE FROM report_account_balance")
    fun clear()
}