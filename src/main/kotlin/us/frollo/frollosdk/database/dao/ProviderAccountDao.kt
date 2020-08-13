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
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccount
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccountRelation

@Dao
internal interface ProviderAccountDao {

    @Query("SELECT * FROM provider_account")
    fun load(): LiveData<List<ProviderAccount>>

    @Query("SELECT * FROM provider_account WHERE provider_account_id = :providerAccountId")
    fun load(providerAccountId: Long): LiveData<ProviderAccount?>

    @Query("SELECT * FROM provider_account WHERE provider_id = :providerId")
    fun loadByProviderId(providerId: Long): LiveData<List<ProviderAccount>>

    @RawQuery(observedEntities = [ProviderAccount::class])
    fun loadByQuery(queryStr: SupportSQLiteQuery): LiveData<List<ProviderAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: ProviderAccount): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: ProviderAccount): Long

    @Query("SELECT provider_account_id FROM provider_account WHERE provider_id IN (:providerIds)")
    fun getIdsByProviderIds(providerIds: LongArray): LongArray

    @Query("SELECT provider_account_id FROM provider_account WHERE provider_account_id NOT IN (:apiIds)")
    fun getStaleIds(apiIds: LongArray): List<Long>

    @Query("DELETE FROM provider_account WHERE provider_account_id IN (:providerAccountIds)")
    fun deleteMany(providerAccountIds: LongArray)

    @Query("DELETE FROM provider_account WHERE provider_account_id = :providerAccountId")
    fun delete(providerAccountId: Long)

    @Query("DELETE FROM provider_account")
    fun clear()

    // Relation methods

    @androidx.room.Transaction
    @Query("SELECT * FROM provider_account")
    fun loadWithRelation(): LiveData<List<ProviderAccountRelation>>

    @androidx.room.Transaction
    @Query("SELECT * FROM provider_account WHERE provider_account_id = :providerAccountId")
    fun loadWithRelation(providerAccountId: Long): LiveData<ProviderAccountRelation?>

    @androidx.room.Transaction
    @Query("SELECT * FROM provider_account WHERE provider_id = :providerId")
    fun loadByProviderIdWithRelation(providerId: Long): LiveData<List<ProviderAccountRelation>>

    @androidx.room.Transaction
    @RawQuery(observedEntities = [ProviderAccount::class])
    fun loadByQueryWithRelation(queryStr: SupportSQLiteQuery): LiveData<List<ProviderAccountRelation>>

    /**
     * RxJava Return Types
     */

    @Query("SELECT * FROM provider_account")
    fun loadRx(): Observable<List<ProviderAccount>>

    @Query("SELECT * FROM provider_account WHERE provider_account_id = :providerAccountId")
    fun loadRx(providerAccountId: Long): Observable<ProviderAccount?>

    @Query("SELECT * FROM provider_account WHERE provider_id = :providerId")
    fun loadByProviderIdRx(providerId: Long): Observable<List<ProviderAccount>>

    @RawQuery(observedEntities = [ProviderAccount::class])
    fun loadByQueryRx(queryStr: SupportSQLiteQuery): Observable<List<ProviderAccount>>

    // Relation methods

    @androidx.room.Transaction
    @Query("SELECT * FROM provider_account")
    fun loadWithRelationRx(): Observable<List<ProviderAccountRelation>>

    @androidx.room.Transaction
    @Query("SELECT * FROM provider_account WHERE provider_account_id = :providerAccountId")
    fun loadWithRelationRx(providerAccountId: Long): Observable<ProviderAccountRelation?>

    @androidx.room.Transaction
    @Query("SELECT * FROM provider_account WHERE provider_id = :providerId")
    fun loadByProviderIdWithRelationRx(providerId: Long): Observable<List<ProviderAccountRelation>>

    @androidx.room.Transaction
    @RawQuery(observedEntities = [ProviderAccount::class])
    fun loadByQueryWithRelationRx(queryStr: SupportSQLiteQuery): Observable<List<ProviderAccountRelation>>
}
