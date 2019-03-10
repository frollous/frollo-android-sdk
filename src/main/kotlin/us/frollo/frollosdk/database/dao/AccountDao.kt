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
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Account
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountRelation

@Dao
internal interface AccountDao {

    @Query("SELECT * FROM account")
    fun load(): LiveData<List<Account>>

    @Query("SELECT * FROM account WHERE account_id = :accountId")
    fun load(accountId: Long): LiveData<Account?>

    @Query("SELECT * FROM account WHERE provider_account_id = :providerAccountId")
    fun loadByProviderAccountId(providerAccountId: Long): LiveData<List<Account>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: Account): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: Account): Long

    @Query("SELECT account_id FROM account WHERE provider_account_id IN (:providerAccountIds)")
    fun getIdsByProviderAccountIds(providerAccountIds: LongArray): LongArray

    @Query("SELECT account_id FROM account WHERE account_id NOT IN (:apiIds)")
    fun getStaleIds(apiIds: LongArray): List<Long>

    @Query("DELETE FROM account WHERE account_id IN (:accountIds)")
    fun deleteMany(accountIds: LongArray)

    @Query("DELETE FROM account WHERE account_id = :accountId")
    fun delete(accountId: Long)

    @Query("DELETE FROM account WHERE provider_account_id = :providerAccountId")
    fun deleteByProviderAccountId(providerAccountId: Int)

    @Query("DELETE FROM account")
    fun clear()

    // Relation methods

    @androidx.room.Transaction
    @Query("SELECT * FROM account")
    fun loadWithRelation(): LiveData<List<AccountRelation>>

    @androidx.room.Transaction
    @Query("SELECT * FROM account WHERE account_id = :accountId")
    fun loadWithRelation(accountId: Long): LiveData<AccountRelation?>

    @androidx.room.Transaction
    @Query("SELECT * FROM account WHERE provider_account_id = :providerAccountId")
    fun loadByProviderAccountIdWithRelation(providerAccountId: Long): LiveData<List<AccountRelation>>
}