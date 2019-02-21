package us.frollo.frollosdk.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import us.frollo.frollosdk.model.api.messages.MessageResponse

@Dao
internal interface MessageDao {
    @Query("SELECT * FROM message")
    fun load(): LiveData<List<MessageResponse>>

    @Query("SELECT * FROM message WHERE read = :readBool")
    fun load(readBool: Boolean): LiveData<List<MessageResponse>>

    @Query("SELECT * FROM message WHERE message_types LIKE '%|'||:messageType||'|%'")
    fun load(messageType: String): LiveData<List<MessageResponse>>

    @Query("SELECT * FROM message WHERE msg_id = :messageId")
    fun load(messageId: Long): LiveData<MessageResponse?>

    @RawQuery(observedEntities = [MessageResponse::class])
    fun loadByQuery(queryStr: SupportSQLiteQuery): LiveData<List<MessageResponse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: MessageResponse): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: MessageResponse): Long

    @Query("SELECT msg_id FROM message WHERE msg_id NOT IN (:apiIds)")
    fun getStaleIds(apiIds: LongArray): List<Long>

    @Query("SELECT msg_id FROM message WHERE (msg_id NOT IN (:apiIds)) AND (read = 0)")
    fun getUnreadStaleIds(apiIds: LongArray): List<Long>

    @Query("DELETE FROM message WHERE msg_id IN (:messageIds)")
    fun deleteMany(messageIds: LongArray)

    @Query("DELETE FROM message WHERE msg_id = :messageId")
    fun delete(messageId: Long)

    @Query("DELETE FROM message")
    fun clear()
}