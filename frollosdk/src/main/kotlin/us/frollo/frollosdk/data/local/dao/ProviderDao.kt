package us.frollo.frollosdk.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import us.frollo.frollosdk.model.coredata.aggregation.providers.Provider

@Dao
internal interface ProviderDao {

    @Query("SELECT * FROM provider")
    fun load(): LiveData<List<Provider>>

    @Query("SELECT * FROM provider WHERE provider_id = :providerId")
    fun load(providerId: Long): LiveData<Provider?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: Provider): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: Provider): Long

    @Query("SELECT provider_id FROM provider WHERE provider_id NOT IN (:apiIds)")
    fun getStaleIds(apiIds: LongArray): List<Long>

    @Query("DELETE FROM provider WHERE provider_id IN (:providerIds)")
    fun deleteMany(providerIds: LongArray)

    @Query("DELETE FROM provider WHERE provider_id = :providerId")
    fun delete(providerId: Long)

    @Query("DELETE FROM provider")
    fun clear()
}