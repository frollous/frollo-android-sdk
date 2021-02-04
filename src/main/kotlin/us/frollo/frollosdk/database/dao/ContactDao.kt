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
import us.frollo.frollosdk.model.coredata.contacts.Contact

@Dao
internal interface ContactDao {

    @Query("SELECT * FROM contact")
    fun load(): LiveData<List<Contact>>

    @Query("SELECT * FROM contact WHERE contact_id = :contactId")
    fun load(contactId: Long): LiveData<Contact?>

    @RawQuery(observedEntities = [Contact::class])
    fun loadByQuery(queryStr: SupportSQLiteQuery): LiveData<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: Contact): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: Contact): Long

    @RawQuery
    fun getIdsByQuery(queryStr: SupportSQLiteQuery): MutableList<Long>

    @Query("DELETE FROM contact WHERE contact_id IN (:contactIds)")
    fun deleteMany(contactIds: LongArray)

    @Query("DELETE FROM contact")
    fun clear()

    /**
     * RxJava Return Types
     */

    @Query("SELECT * FROM contact")
    fun loadRx(): Observable<List<Contact>>

    @Query("SELECT * FROM contact WHERE contact_id = :contactId")
    fun loadRx(contactId: Long): Observable<Contact?>

    @RawQuery(observedEntities = [Contact::class])
    fun loadByQueryRx(queryStr: SupportSQLiteQuery): Observable<List<Contact>>
}
