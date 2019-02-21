package us.frollo.frollosdk.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategory

@Dao
internal interface TransactionCategoryDao {

    @Query("SELECT * FROM transaction_category")
    fun load(): LiveData<List<TransactionCategory>>

    @Query("SELECT * FROM transaction_category WHERE transaction_category_id = :transactionCategoryId")
    fun load(transactionCategoryId: Long): LiveData<TransactionCategory?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: TransactionCategory): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: TransactionCategory): Long

    @Query("SELECT transaction_category_id FROM transaction_category WHERE transaction_category_id NOT IN (:apiIds)")
    fun getStaleIds(apiIds: LongArray): List<Long>

    @Query("DELETE FROM transaction_category WHERE transaction_category_id IN (:transactionCategoryIds)")
    fun deleteMany(transactionCategoryIds: LongArray)

    @Query("DELETE FROM transaction_category WHERE transaction_category_id = :transactionCategoryId")
    fun delete(transactionCategoryId: Long)

    @Query("DELETE FROM transaction_category")
    fun clear()
}