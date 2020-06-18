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
import retrofit2.http.PUT
import retrofit2.http.Path
import us.frollo.frollosdk.model.api.messages.MessageResponse
import us.frollo.frollosdk.model.api.messages.MessageUpdateRequest

internal interface MessagesAPI {
    companion object {
        const val URL_UNREAD = "messages/unread"
        const val URL_MESSAGES = "messages"
        const val URL_MESSAGE = "messages/{message_id}"
    }

    @GET(URL_MESSAGES)
    fun fetchMessages(): Call<List<MessageResponse>>

    @GET(URL_UNREAD)
    fun fetchUnreadMessages(): Call<List<MessageResponse>>

    @GET(URL_MESSAGE)
    fun fetchMessage(@Path("message_id") id: Long): Call<MessageResponse>

    @PUT(URL_MESSAGE)
    fun updateMessage(@Path("message_id") id: Long, @Body request: MessageUpdateRequest): Call<MessageResponse>
}
