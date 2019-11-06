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
import us.frollo.frollosdk.model.coredata.aggregation.merchants.Merchant

@Dao
internal interface MerchantDao {

    @Query("SELECT * FROM merchant")
    fun load(): LiveData<List<Merchant>>

    @Query("SELECT * FROM merchant WHERE merchant_id = :merchantId")
    fun load(merchantId: Long): LiveData<Merchant?>

    @Query("SELECT COUNT ( merchant_id ) FROM merchant")
    fun getMerchantsCount(): Long

    @RawQuery(observedEntities = [Merchant::class])
    fun loadByQuery(queryStr: SupportSQLiteQuery): LiveData<List<Merchant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: Merchant): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: Merchant): Long

    @Query("SELECT merchant_id FROM merchant")
    fun getIds(): List<Long>

    @Query("SELECT merchant_id FROM merchant LIMIT :limit OFFSET :offset")
    fun getIdsByOffset(limit: Int, offset: Int): List<Long>

    @Query("DELETE FROM merchant WHERE merchant_id IN (:merchantIds)")
    fun deleteMany(merchantIds: LongArray)

    @Query("DELETE FROM merchant WHERE merchant_id = :merchantId")
    fun delete(merchantId: Long)

    @Query("DELETE FROM merchant")
    fun clear()
}