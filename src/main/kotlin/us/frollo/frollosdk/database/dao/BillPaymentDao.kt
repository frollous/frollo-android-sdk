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
import us.frollo.frollosdk.model.coredata.bills.BillPayment
import us.frollo.frollosdk.model.coredata.bills.BillPaymentRelation

@Dao
internal interface BillPaymentDao {

    @Query("SELECT * FROM bill_payment WHERE date BETWEEN Date(:fromDate) AND Date(:toDate)")
    fun load(fromDate: String, toDate: String): LiveData<List<BillPayment>>

    @Query("SELECT * FROM bill_payment WHERE bill_payment_id = :billPaymentId")
    fun load(billPaymentId: Long): LiveData<BillPayment?>

    @Query("SELECT * FROM bill_payment WHERE bill_id = :billId AND (date BETWEEN Date(:fromDate) AND Date(:toDate))")
    fun loadByBillId(billId: Long, fromDate: String, toDate: String): LiveData<List<BillPayment>>

    @Query("SELECT * FROM bill_payment WHERE bill_id = :billId")
    fun loadByBillId(billId: Long): LiveData<List<BillPayment>>

    @RawQuery(observedEntities = [BillPayment::class])
    fun loadByQuery(queryStr: SupportSQLiteQuery): LiveData<List<BillPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: BillPayment): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: BillPayment): Long

    @Query("SELECT bill_payment_id FROM bill_payment WHERE bill_id IN (:billIds)")
    fun getIdsByBillIds(billIds: LongArray): LongArray

    @Query("SELECT bill_payment_id FROM bill_payment WHERE bill_payment_id NOT IN (:apiIds) AND (date BETWEEN Date(:fromDate) AND Date(:toDate))")
    fun getStaleIds(apiIds: LongArray, fromDate: String, toDate: String): List<Long>

    @Query("DELETE FROM bill_payment WHERE bill_payment_id IN (:billPaymentIds)")
    fun deleteMany(billPaymentIds: LongArray)

    @Query("DELETE FROM bill_payment WHERE bill_payment_id = :billPaymentId")
    fun delete(billPaymentId: Long)

    @Query("DELETE FROM bill_payment")
    fun clear()

    // Relation methods

    @androidx.room.Transaction
    @Query("SELECT * FROM bill_payment WHERE date BETWEEN Date(:fromDate) AND Date(:toDate)")
    fun loadWithRelation(fromDate: String, toDate: String): LiveData<List<BillPaymentRelation>>

    @androidx.room.Transaction
    @Query("SELECT * FROM bill_payment WHERE bill_payment_id = :billPaymentId")
    fun loadWithRelation(billPaymentId: Long): LiveData<BillPaymentRelation?>

    @androidx.room.Transaction
    @Query("SELECT * FROM bill_payment WHERE bill_id = :billId AND (date BETWEEN Date(:fromDate) AND Date(:toDate))")
    fun loadByBillIdWithRelation(billId: Long, fromDate: String, toDate: String): LiveData<List<BillPaymentRelation>>

    @androidx.room.Transaction
    @Query("SELECT * FROM bill_payment WHERE bill_id = :billId")
    fun loadByBillIdWithRelation(billId: Long): LiveData<List<BillPaymentRelation>>

    @androidx.room.Transaction
    @RawQuery(observedEntities = [BillPaymentRelation::class])
    fun loadByQueryWithRelation(queryStr: SupportSQLiteQuery): LiveData<List<BillPaymentRelation>>
}