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
import androidx.room.*
import us.frollo.frollosdk.model.coredata.bills.Bill
import us.frollo.frollosdk.model.coredata.bills.BillRelation

@Dao
internal interface BillDao {

    @Query("SELECT * FROM bill")
    fun load(): LiveData<List<Bill>>

    @Query("SELECT * FROM bill WHERE bill_id = :billId")
    fun load(billId: Long): LiveData<Bill?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: Bill): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: Bill): Long

    @Query("SELECT bill_id FROM bill WHERE bill_id NOT IN (:apiIds)")
    fun getStaleIds(apiIds: LongArray): List<Long>

    @Query("DELETE FROM bill WHERE bill_id IN (:billIds)")
    fun deleteMany(billIds: LongArray)

    @Query("DELETE FROM bill WHERE bill_id = :billId")
    fun delete(billId: Long)

    @Query("DELETE FROM bill")
    fun clear()

    // Relation methods

    @androidx.room.Transaction
    @Query("SELECT * FROM bill")
    fun loadWithRelation(): LiveData<List<BillRelation>>

    @androidx.room.Transaction
    @Query("SELECT * FROM bill WHERE bill_id = :billId")
    fun loadWithRelation(billId: Long): LiveData<BillRelation?>
}