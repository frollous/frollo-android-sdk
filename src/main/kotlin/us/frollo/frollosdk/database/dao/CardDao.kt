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
import us.frollo.frollosdk.model.coredata.cards.Card
import us.frollo.frollosdk.model.coredata.cards.CardRelation

@Dao
internal interface CardDao {

    @Query("SELECT * FROM card")
    fun load(): LiveData<List<Card>>

    @Query("SELECT * FROM card WHERE card_id = :cardId")
    fun load(cardId: Long): LiveData<Card?>

    @Query("SELECT * FROM card WHERE card_id = :cardId LIMIT 1")
    fun loadCard(cardId: Long): Card?

    @RawQuery(observedEntities = [Card::class])
    fun loadByQuery(queryStr: SupportSQLiteQuery): LiveData<List<Card>>

    @RawQuery
    fun getIdsByQuery(queryStr: SupportSQLiteQuery): MutableList<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: Card): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: Card): Long

    @Query("SELECT card_id FROM card WHERE account_id IN (:accountIds)")
    fun getIdsByAccountIds(accountIds: LongArray): LongArray

    @Query("DELETE FROM card WHERE card_id IN (:cardIds)")
    fun deleteMany(cardIds: LongArray)

    @Query("DELETE FROM card WHERE card_id = :cardId")
    fun delete(cardId: Long)

    @Query("DELETE FROM card")
    fun clear()

    // Relation methods

    @androidx.room.Transaction
    @Query("SELECT * FROM card")
    fun loadWithRelation(): LiveData<List<CardRelation>>

    @androidx.room.Transaction
    @Query("SELECT * FROM card WHERE card_id = :cardId")
    fun loadWithRelation(cardId: Long): LiveData<CardRelation?>

    @RawQuery(observedEntities = [Card::class])
    fun loadByQueryWithRelation(queryStr: SupportSQLiteQuery): LiveData<List<CardRelation>>

    /**
     * RxJava Return Types
     */

    @Query("SELECT * FROM card")
    fun loadRx(): Observable<List<Card>>

    @Query("SELECT * FROM card WHERE card_id = :cardId")
    fun loadRx(cardId: Long): Observable<Card?>

    @Query("SELECT * FROM card WHERE card_id in (:cardIds)")
    fun loadRx(cardIds: LongArray): Observable<List<Card>>

    @RawQuery(observedEntities = [Card::class])
    fun loadByQueryRx(queryStr: SupportSQLiteQuery): Observable<List<Card>>

    // Relation methods

    @androidx.room.Transaction
    @Query("SELECT * FROM card")
    fun loadWithRelationRx(): Observable<List<Card>>

    @androidx.room.Transaction
    @Query("SELECT * FROM card WHERE card_id = :cardId")
    fun loadWithRelationRx(cardId: Long): Observable<CardRelation?>

    @androidx.room.Transaction
    @Query("SELECT * FROM card WHERE card_id in (:cardIds)")
    fun loadWithRelationRx(cardIds: LongArray): Observable<List<CardRelation>>

    @RawQuery(observedEntities = [Card::class])
    fun loadByQueryWithRelationRx(queryStr: SupportSQLiteQuery): Observable<List<CardRelation>>
}
