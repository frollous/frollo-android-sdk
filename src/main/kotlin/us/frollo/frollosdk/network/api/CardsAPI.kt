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

package us.frollo.frollosdk.network.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import us.frollo.frollosdk.model.api.cards.CardActivateRequest
import us.frollo.frollosdk.model.api.cards.CardCreateRequest
import us.frollo.frollosdk.model.api.cards.CardLockOrReplaceRequest
import us.frollo.frollosdk.model.api.cards.CardPublicKeyResponse
import us.frollo.frollosdk.model.api.cards.CardResponse
import us.frollo.frollosdk.model.api.cards.CardSetPINRequest
import us.frollo.frollosdk.model.api.cards.CardUpdateRequest

internal interface CardsAPI {
    companion object {
        // Card URLs
        const val URL_CARDS = "cards"
        const val URL_CARD = "$URL_CARDS/{card_id}"
        const val URL_CARD_REPLACE = "$URL_CARD/replace"
        const val URL_CARD_SET_PIN = "$URL_CARD/pin"
        const val URL_CARD_ACTIVATE = "$URL_CARD/activate"
        const val URL_CARD_LOCK = "$URL_CARD/lock"
        const val URL_CARD_UNLOCK = "$URL_CARD/unlock"
        const val URL_CARD_PUBLIC_KEY = "$URL_CARDS/pin/key"
    }

    // Card API

    @GET(URL_CARDS)
    fun fetchCards(): Call<List<CardResponse>>

    @GET(URL_CARD)
    fun fetchCard(@Path("card_id") cardId: Long): Call<CardResponse>

    @POST(URL_CARDS)
    fun createCard(@Body request: CardCreateRequest): Call<CardResponse>

    @PUT(URL_CARD)
    fun updateCard(@Path("card_id") cardId: Long, @Body request: CardUpdateRequest): Call<CardResponse>

    @PUT(URL_CARD_REPLACE)
    fun replaceCard(@Path("card_id") cardId: Long, @Body request: CardLockOrReplaceRequest): Call<Void>

    @PUT(URL_CARD_SET_PIN)
    fun setCardPin(@Path("card_id") cardId: Long, @Body request: CardSetPINRequest): Call<CardResponse>

    @PUT(URL_CARD_ACTIVATE)
    fun activateCard(@Path("card_id") cardId: Long, @Body request: CardActivateRequest): Call<CardResponse>

    @PUT(URL_CARD_LOCK)
    fun lockCard(@Path("card_id") cardId: Long, @Body request: CardLockOrReplaceRequest): Call<CardResponse>

    @PUT(URL_CARD_UNLOCK)
    fun unlockCard(@Path("card_id") cardId: Long): Call<CardResponse>

    @GET(URL_CARD_PUBLIC_KEY)
    fun fetchPublicKey(): Call<CardPublicKeyResponse>
}
