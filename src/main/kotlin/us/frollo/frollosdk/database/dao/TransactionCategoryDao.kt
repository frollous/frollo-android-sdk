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
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategory

@Dao
internal interface TransactionCategoryDao {

    @Query("SELECT * FROM transaction_category")
    fun load(): LiveData<List<TransactionCategory>>

    @Query("SELECT * FROM transaction_category WHERE transaction_category_id = :transactionCategoryId")
    fun load(transactionCategoryId: Long): LiveData<TransactionCategory?>

    @RawQuery(observedEntities = [TransactionCategory::class])
    fun loadByQuery(queryStr: SupportSQLiteQuery): LiveData<List<TransactionCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: TransactionCategory): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: TransactionCategory): Long

    @Query("SELECT transaction_category_id FROM transaction_category WHERE transaction_category_id NOT IN (:apiIds)")
    fun getStaleIds(apiIds: LongArray): List<Long>

    @Query("DELETE FROM transaction_category WHERE transaction_category_id IN (:transactionCategoryIds)")
    fun deleteMany(transactionCategoryIds: LongArray)

    @Query("DELETE FROM transaction_category WHERE transaction_category_id = :transactionCategoryId")
    fun delete(transactionCategoryId: Long)

    @Query("DELETE FROM transaction_category")
    fun clear()
}
