package us.frollo.frollosdk.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import us.frollo.frollosdk.model.coredata.aggregation.tags.TransactionTags

@Dao
internal interface UserTagsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(tagsList: List<TransactionTags>): LongArray

    @Query("delete from transaction_tags where name not in (:ids)")
    fun deleteByNames(ids: List<String>)

    @Query("SELECT * FROM transaction_tags")
    fun load(): LiveData<List<TransactionTags>>
}