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

package us.frollo.frollosdk.cards

import androidx.sqlite.db.SimpleSQLiteQuery
import io.reactivex.Observable
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.extensions.sqlForCards
import us.frollo.frollosdk.model.coredata.cards.Card
import us.frollo.frollosdk.model.coredata.cards.CardRelation
import us.frollo.frollosdk.model.coredata.cards.CardStatus

// Card

/**
 * Fetch card by ID from the cache
 *
 * @param cardId Unique card ID to fetch
 *
 * @return Rx Observable object of Card which can be observed using an Observer for future changes as well.
 */
fun Cards.fetchCardRx(cardId: Long): Observable<Card?> {
    return db.cards().loadRx(cardId)
}

/**
 * Fetch card by ID from the cache along with other associated data.
 *
 * @param cardId Unique card ID to fetch
 *
 * @return Rx Observable object of CardRelation which can be observed using an Observer for future changes as well.
 */
fun Cards.fetchCardWithRelationRx(cardId: Long): Observable<CardRelation?> {
    return db.cards().loadWithRelationRx(cardId)
}

/**
 * Fetch cards from the cache
 *
 * @param status Filter by the status of the card (optional)
 * @param accountId Filter by the accountId with which the cards are associated with (optional)
 *
 * @return Rx Observable object of List<Card> which can be observed using an Observer for future changes as well.
 */
fun Cards.fetchCardsRx(status: CardStatus? = null, accountId: Long? = null): Observable<List<Card>> {
    return db.cards().loadByQueryRx(sqlForCards(status = status, accountId = accountId))
}

/**
 * Advanced method to fetch cards by SQL query from the cache
 *
 * @param query SimpleSQLiteQuery: Select query which fetches cards from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<Card> which can be observed using an Observer for future changes as well.
 */
fun Cards.fetchCardsRx(query: SimpleSQLiteQuery): Observable<List<Card>> {
    return db.cards().loadByQueryRx(query)
}

/**
 * Fetch cards from the cache with associated data
 *
 * @param status Filter by the status of the card (optional)
 * @param accountId Filter by the accountId with which the cards are associated with (optional)
 *
 * @return Rx Observable object of List<CardRelation> which can be observed using an Observer for future changes as well.
 */
fun Cards.fetchCardsWithRelationRx(status: CardStatus? = null, accountId: Long? = null): Observable<List<CardRelation>> {
    return db.cards().loadByQueryWithRelationRx(sqlForCards(status = status, accountId = accountId))
}

/**
 * Advanced method to fetch cards by SQL query from the cache with associated data
 *
 * @param query SimpleSQLiteQuery: Select query which fetches cards from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<CardRelation> which can be observed using an Observer for future changes as well.
 */
fun Cards.fetchCardsWithRelationRx(query: SimpleSQLiteQuery): Observable<List<CardRelation>> {
    return db.cards().loadByQueryWithRelationRx(query)
}
