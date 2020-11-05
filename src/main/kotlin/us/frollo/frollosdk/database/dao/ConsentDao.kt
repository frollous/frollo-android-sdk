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
import us.frollo.frollosdk.model.coredata.cdr.Consent
import us.frollo.frollosdk.model.coredata.cdr.ConsentRelation

@Dao
internal interface ConsentDao {

    @Query("SELECT * FROM consent")
    fun load(): LiveData<List<Consent>>

    @Query("SELECT * FROM consent WHERE consent_id = :consentId")
    fun load(consentId: Long): LiveData<Consent?>

    @RawQuery(observedEntities = [Consent::class])
    fun loadByQuery(queryStr: SupportSQLiteQuery): LiveData<List<Consent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: Consent): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: Consent): Long

    @Query("SELECT consent_id FROM consent WHERE consent_id NOT IN (:apiIds)")
    fun getStaleIds(apiIds: LongArray): List<Long>

    @Query("DELETE FROM consent WHERE consent_id IN (:consentIds)")
    fun deleteMany(consentIds: LongArray)

    @Query("DELETE FROM consent WHERE consent_id = :consentId")
    fun delete(consentId: Long)

    @Query("DELETE FROM consent")
    fun clear()

    // Relation methods

    @androidx.room.Transaction
    @Query("SELECT * FROM consent")
    fun loadWithRelation(): LiveData<List<ConsentRelation>>

    @androidx.room.Transaction
    @Query("SELECT * FROM consent WHERE consent_id = :consentId")
    fun loadWithRelation(consentId: Long): LiveData<ConsentRelation?>

    @androidx.room.Transaction
    @RawQuery(observedEntities = [ConsentRelation::class])
    fun loadByQueryWithRelation(queryStr: SupportSQLiteQuery): LiveData<List<ConsentRelation>>

    /**
     * RxJava Return Types
     */

    @Query("SELECT * FROM consent")
    fun loadRx(): Observable<List<Consent>>

    @Query("SELECT * FROM consent WHERE consent_id = :consentId")
    fun loadRx(consentId: Long): Observable<Consent?>

    @RawQuery(observedEntities = [Consent::class])
    fun loadByQueryRx(queryStr: SupportSQLiteQuery): Observable<List<Consent>>

    // Relation methods

    @androidx.room.Transaction
    @Query("SELECT * FROM consent")
    fun loadWithRelationRx(): Observable<List<ConsentRelation>>

    @androidx.room.Transaction
    @Query("SELECT * FROM consent WHERE consent_id = :consentId")
    fun loadWithRelationRx(consentId: Long): Observable<ConsentRelation?>

    @androidx.room.Transaction
    @RawQuery(observedEntities = [ConsentRelation::class])
    fun loadByQueryWithRelationRx(queryStr: SupportSQLiteQuery): Observable<List<ConsentRelation>>
}
