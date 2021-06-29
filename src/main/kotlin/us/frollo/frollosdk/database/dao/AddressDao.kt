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
import us.frollo.frollosdk.model.coredata.address.Address

@Dao
internal interface AddressDao {

    @Query("SELECT * FROM addresses")
    fun load(): LiveData<List<Address>>

    @Query("SELECT * FROM addresses WHERE address_id = :addressId")
    fun load(addressId: Long): LiveData<Address?>

    @RawQuery(observedEntities = [Address::class])
    fun loadByQuery(queryStr: SupportSQLiteQuery): LiveData<List<Address>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: Address): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: Address): Long

    @Query("SELECT address_id FROM addresses")
    fun getIds(): MutableList<Long>

    @Query("DELETE FROM addresses WHERE address_id IN (:addressIds)")
    fun deleteMany(addressIds: LongArray)

    @Query("DELETE FROM addresses WHERE address_id = :addressId")
    fun delete(addressId: Long)

    @Query("DELETE FROM addresses")
    fun clear()

    /**
     * RxJava Return Types
     */

    @Query("SELECT * FROM addresses")
    fun loadRx(): Observable<List<Address>>

    @Query("SELECT * FROM addresses WHERE address_id = :addressId")
    fun loadRx(addressId: Long): Observable<Address?>

    @RawQuery(observedEntities = [Address::class])
    fun loadByQueryRx(queryStr: SupportSQLiteQuery): Observable<List<Address>>
}
