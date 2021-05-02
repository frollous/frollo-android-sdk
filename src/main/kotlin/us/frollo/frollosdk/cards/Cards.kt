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

import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.extensions.encryptValueBase64
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.sqlForCards
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toCard
import us.frollo.frollosdk.model.api.cards.CardActivateRequest
import us.frollo.frollosdk.model.api.cards.CardCreateRequest
import us.frollo.frollosdk.model.api.cards.CardLockOrReplaceRequest
import us.frollo.frollosdk.model.api.cards.CardPublicKeyResponse
import us.frollo.frollosdk.model.api.cards.CardResponse
import us.frollo.frollosdk.model.api.cards.CardSetPINRequest
import us.frollo.frollosdk.model.api.cards.CardUpdateRequest
import us.frollo.frollosdk.model.coredata.cards.Card
import us.frollo.frollosdk.model.coredata.cards.CardLockOrReplaceReason
import us.frollo.frollosdk.model.coredata.cards.CardRelation
import us.frollo.frollosdk.model.coredata.cards.CardStatus
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.CardsAPI

/** Manages all aspects of Cards */
class Cards(network: NetworkService, internal val db: SDKDatabase) {

    companion object {
        private const val TAG = "Cards"
    }

    private val cardsAPI: CardsAPI = network.create(CardsAPI::class.java)

    // Cards

    /**
     * Fetch card by ID from the cache
     *
     * @param cardId Unique card ID to fetch
     *
     * @return LiveData object of Card which can be observed using an Observer for future changes as well.
     */
    fun fetchCard(cardId: Long): LiveData<Card?> {
        return db.cards().load(cardId)
    }

    /**
     * Fetch card by ID from the cache along with other associated data.
     *
     * @param cardId Unique card ID to fetch
     *
     * @return LiveData object of CardRelation which can be observed using an Observer for future changes as well.
     */
    fun fetchCardWithRelation(cardId: Long): LiveData<CardRelation?> {
        return db.cards().loadWithRelation(cardId)
    }

    /**
     * Fetch cards from the cache
     *
     * @param status Filter by the status of the card (optional)
     * @param accountId Filter by the accountId with which the cards are associated with (optional)
     *
     * @return LiveData object of List<Card> which can be observed using an Observer for future changes as well.
     */
    fun fetchCards(status: CardStatus? = null, accountId: Long? = null): LiveData<List<Card>> {
        return db.cards().loadByQuery(sqlForCards(status = status, accountId = accountId))
    }

    /**
     * Advanced method to fetch cards by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches cards from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of List<Card> which can be observed using an Observer for future changes as well.
     */
    fun fetchCards(query: SimpleSQLiteQuery): LiveData<List<Card>> {
        return db.cards().loadByQuery(query)
    }

    /**
     * Fetch cards from the cache with associated data
     *
     * @param status Filter by the status of the card (optional)
     * @param accountId Filter by the accountId with which the cards are associated with (optional)
     *
     * @return LiveData object of List<CardRelation> which can be observed using an Observer for future changes as well.
     */
    fun fetchCardsWithRelation(status: CardStatus? = null, accountId: Long? = null): LiveData<List<CardRelation>> {
        return db.cards().loadByQueryWithRelation(sqlForCards(status = status, accountId = accountId))
    }

    /**
     * Advanced method to fetch cards by SQL query from the cache with associated data
     *
     * @param query SimpleSQLiteQuery: Select query which fetches cards from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of List<CardRelation> which can be observed using an Observer for future changes as well.
     */
    fun fetchCardWithRelation(query: SimpleSQLiteQuery): LiveData<List<CardRelation>> {
        return db.cards().loadByQueryWithRelation(query)
    }

    /**
     * Refresh a specific card by ID from the host
     *
     * @param cardId ID of the card to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshCard(cardId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        cardsAPI.fetchCard(cardId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshCard", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleCardResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Refresh all available cards from the host.
     *
     * @param completion Optional completion handler with optional error if the request fails and list of cards if succeeds
     */
    fun refreshCards(completion: OnFrolloSDKCompletionListener<Resource<List<Card>>>? = null) {
        cardsAPI.fetchCards().enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshCards", resource.error?.localizedDescription)
                    completion?.invoke(Resource.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleCardsResponse(resource.data, completion)
                }
            }
        }
    }

    /**
     * Create a new card on the host
     *
     * @param accountId ID of the account to which the card is to be linked
     * @param firstName First name of the card holder
     * @param middleName Middle name of the card holder (Optional)
     * @param lastName Last name of the card holder
     * @param addressId ID of the address postal address to which the card is to be sent
     * @param completion Optional completion handler with optional error if the request fails else ID of the Card created if success
     */
    fun createCard(
        accountId: Long,
        firstName: String,
        middleName: String? = null,
        lastName: String,
        addressId: Long,
        completion: OnFrolloSDKCompletionListener<Resource<Long>>? = null
    ) {
        val request = CardCreateRequest(
            accountId = accountId,
            firstName = firstName,
            middleName = middleName,
            lastName = lastName,
            addressId = addressId
        )

        cardsAPI.createCard(request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#createCard", resource.error?.localizedDescription)
                    completion?.invoke(Resource.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleCardResponse(response = resource.data, completionWithData = completion)
                }
            }
        }
    }

    /**
     * Update a card on the host
     *
     * @param card Updated card data model
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun updateCard(card: Card, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        val request = CardUpdateRequest(status = card.status, nickName = card.nickName)

        cardsAPI.updateCard(card.cardId, request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateCard", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleCardResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Activate a card on the host
     *
     * @param cardId ID of the card to be activated
     * @param panLastFourDigits Last 4 digits of the PAN on the physical card
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun activateCard(
        cardId: Long,
        panLastFourDigits: String,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        val request = CardActivateRequest(panLastFourDigits)

        cardsAPI.activateCard(cardId, request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#activateCard", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleCardResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Set PIN for a card on the host
     *
     * @param cardId ID of the card for which the PIN needs to be set
     * @param encryptedPIN Encrypted PIN using key from getPublicKey API
     * @param keyId KeyID returned from the getPublicKey API
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun setCardPin(
        cardId: Long,
        encryptedPIN: String,
        keyId: String,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        val request = CardSetPINRequest(encryptedPIN = encryptedPIN, keyId = keyId)

        cardsAPI.setCardPin(cardId, request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#setCardPin", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleCardResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Lock a card on the host
     *
     * @param cardId ID of the card to be locked
     * @param reason Reason for locking the card (Optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun lockCard(
        cardId: Long,
        reason: CardLockOrReplaceReason? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        val request = CardLockOrReplaceRequest(reason)

        cardsAPI.lockCard(cardId, request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#lockCard", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleCardResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Unlock a card on the host
     *
     * @param cardId ID of the card to be unlocked
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun unlockCard(cardId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        cardsAPI.unlockCard(cardId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#unlockCard", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleCardResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Replace a card on the host
     *
     * @param cardId ID of the card to be replaced
     * @param reason Reason for replacing the card (Optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun replaceCard(
        cardId: Long,
        reason: CardLockOrReplaceReason? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        val request = CardLockOrReplaceRequest(reason)

        cardsAPI.replaceCard(cardId, request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#replaceCard", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    refreshCards {
                        completion?.invoke(Result.success())
                    }
                }
            }
        }
    }

    /**
     * Get Public key for encrypting card PIN on the host
     *
     * @param completion Completion handler with optional error if the request fails else [CardPublicKeyResponse] if success
     */
    fun getPublicKey(completion: OnFrolloSDKCompletionListener<Resource<CardPublicKeyResponse>>) {
        cardsAPI.fetchPublicKey().enqueue { resource ->
            if (resource.status == Resource.Status.ERROR) {
                Log.e("$TAG#getPublicKey", resource.error?.localizedDescription)
            }
            completion.invoke(resource)
        }
    }

    /**
     * Encrypt card pin using public key
     *
     * @param pin Card's PIN
     * @param publicKey PEM formatted public key to use for encryption
     *
     * @return Encrypted value of the Card's PIN if success else null
     */
    fun encryptPin(pin: String, publicKey: String): String? {
        return try {
            encryptValueBase64(publicKey, pin)
        } catch (e: Exception) {
            Log.e("Cards#encryptPin", "Encryption failed: ${e.message}")
            null
        }
    }

    // Response Handlers

    private fun handleCardResponse(
        response: CardResponse?,
        completion: OnFrolloSDKCompletionListener<Result>? = null,
        completionWithData: OnFrolloSDKCompletionListener<Resource<Long>>? = null
    ) {
        response?.let {
            doAsync {
                val model = response.toCard()

                db.cards().insert(model)

                uiThread {
                    completion?.invoke(Result.success())
                    completionWithData?.invoke(Resource.success(response.cardId))
                }
            }
        } ?: run {
            completion?.invoke(Result.success())
            completionWithData?.invoke(Resource.success(null))
        } // Explicitly invoke completion callback if response is null.
    }

    private fun handleCardsResponse(
        response: List<CardResponse>?,
        completion: OnFrolloSDKCompletionListener<Resource<List<Card>>>?
    ) {
        var models = listOf<Card>()
        response?.let {
            doAsync {
                models = response.map { it.toCard() }

                db.cards().insertAll(*models.toTypedArray())

                val apiIds = models.map { it.cardId }.toHashSet()
                val allCardIds = db.cards().getIds().toHashSet()
                val staleIds = allCardIds.minus(apiIds)

                if (staleIds.isNotEmpty()) {
                    db.cards().deleteMany(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Resource.success(models)) }
            }
        } ?: run { completion?.invoke(Resource.success(models)) } // Explicitly invoke completion callback if response is null.
    }
}
