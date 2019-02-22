package us.frollo.frollosdk.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import us.frollo.frollosdk.model.coredata.aggregation.merchants.Merchant

@Dao
internal interface MerchantDao {

    @Query("SELECT * FROM merchant")
    fun load(): LiveData<List<Merchant>>

    @Query("SELECT * FROM merchant WHERE merchant_id = :merchantId")
    fun load(merchantId: Long): LiveData<Merchant?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: Merchant): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: Merchant): Long

    @Query("SELECT merchant_id FROM merchant WHERE merchant_id NOT IN (:apiIds)")
    fun getStaleIds(apiIds: LongArray): List<Long>

    @Query("SELECT merchant_id FROM merchant")
    fun getIds(): List<Long>

    @Query("DELETE FROM merchant WHERE merchant_id IN (:merchantIds)")
    fun deleteMany(merchantIds: LongArray)

    @Query("DELETE FROM merchant WHERE merchant_id = :merchantId")
    fun delete(merchantId: Long)

    @Query("DELETE FROM merchant")
    fun clear()
}