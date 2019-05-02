package us.frollo.frollosdk.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import us.frollo.frollosdk.model.coredata.user.UserTags

@Dao
internal interface UserTagsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: UserTags): LongArray
}