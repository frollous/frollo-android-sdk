package us.frollo.frollosdk.network.api

import retrofit2.Call
import retrofit2.http.*
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