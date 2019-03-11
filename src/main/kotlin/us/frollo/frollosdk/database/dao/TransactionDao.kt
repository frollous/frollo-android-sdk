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
import us.frollo.frollosdk.model.coredata.aggregation.transactions.Transaction
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionRelation

@Dao
internal interface TransactionDao {

    @Query("SELECT * FROM transaction_model")
    fun load(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transaction_model WHERE transaction_id = :transactionId")
    fun load(transactionId: Long): LiveData<Transaction?>

    @Query("SELECT * FROM transaction_model WHERE transaction_id in (:transactionIds)")
    fun load(transactionIds: LongArray): LiveData<List<Transaction>>

    @Query("SELECT * FROM transaction_model WHERE account_id = :accountId")
    fun loadByAccountId(accountId: Long): LiveData<List<Transaction>>

    @Query("SELECT * FROM transaction_model WHERE transaction_id = :transactionId LIMIT 1")
    fun loadTransaction(transactionId: Long): Transaction?

    @RawQuery(observedEntities = [Transaction::class])
    fun loadByQuery(queryStr: SupportSQLiteQuery): LiveData<List<Transaction>>

    @RawQuery
    fun getIdsQuery(queryStr: SupportSQLiteQuery): MutableList<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: Transaction): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: Transaction): Long

    @Query("SELECT transaction_id FROM transaction_model WHERE account_id IN (:accountIds)")
    fun getIdsByAccountIds(accountIds: LongArray): LongArray

    @Query("SELECT transaction_id FROM transaction_model WHERE transaction_id NOT IN (:apiIds)")
    fun getStaleIds(apiIds: LongArray): List<Long>

    @Query("DELETE FROM transaction_model WHERE transaction_id IN (:transactionIds)")
    fun deleteMany(transactionIds: LongArray)

    @Query("DELETE FROM transaction_model WHERE transaction_id = :transactionId")
    fun delete(transactionId: Long)

    @Query("DELETE FROM transaction_model WHERE account_id in (:accountIds)")
    fun deleteByAccountIds(accountIds: LongArray)

    @Query("DELETE FROM transaction_model WHERE account_id = :accountId")
    fun deleteByAccountId(accountId: Long)

    @Query("DELETE FROM transaction_model")
    fun clear()

    // Relation methods

    @androidx.room.Transaction
    @Query("SELECT * FROM transaction_model")
    fun loadWithRelation(): LiveData<List<TransactionRelation>>

    @androidx.room.Transaction
    @Query("SELECT * FROM transaction_model WHERE transaction_id = :transactionId")
    fun loadWithRelation(transactionId: Long): LiveData<TransactionRelation?>

    @androidx.room.Transaction
    @Query("SELECT * FROM transaction_model WHERE transaction_id in (:transactionIds)")
    fun loadWithRelation(transactionIds: LongArray): LiveData<List<TransactionRelation>>

    @androidx.room.Transaction
    @Query("SELECT * FROM transaction_model WHERE account_id = :accountId")
    fun loadByAccountIdWithRelation(accountId: Long): LiveData<List<TransactionRelation>>

    @RawQuery(observedEntities = [Transaction::class])
    fun loadByQueryWithRelation(queryStr: SupportSQLiteQuery): LiveData<List<TransactionRelation>>
}