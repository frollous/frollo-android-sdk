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
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.reactivex.Observable
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

    @RawQuery
    fun loadMessageCount(queryStr: SupportSQLiteQuery): Long

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

    /**
     * RxJava Return Types
     */

    @Query("SELECT * FROM message")
    fun loadRx(): Observable<List<MessageResponse>>

    @Query("SELECT * FROM message WHERE read = :readBool")
    fun loadRx(readBool: Boolean): Observable<List<MessageResponse>>

    @Query("SELECT * FROM message WHERE message_types LIKE '%|'||:messageType||'|%'")
    fun loadRx(messageType: String): Observable<List<MessageResponse>>

    @Query("SELECT * FROM message WHERE msg_id = :messageId")
    fun loadRx(messageId: Long): Observable<MessageResponse?>

    @RawQuery(observedEntities = [MessageResponse::class])
    fun loadByQueryRx(queryStr: SupportSQLiteQuery): Observable<List<MessageResponse>>
}
