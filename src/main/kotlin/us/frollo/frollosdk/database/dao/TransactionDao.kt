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
}