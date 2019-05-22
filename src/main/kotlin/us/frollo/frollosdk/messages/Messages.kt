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

package us.frollo.frollosdk.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.sqlite.db.SimpleSQLiteQuery
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.authentication.Authentication
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.MessagesAPI
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.generateSQLQueryMessages
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toMessage
import us.frollo.frollosdk.model.api.messages.MessageResponse
import us.frollo.frollosdk.model.api.messages.MessageUpdateRequest
import us.frollo.frollosdk.model.coredata.messages.ContentType
import us.frollo.frollosdk.model.coredata.notifications.NotificationPayload
import us.frollo.frollosdk.model.coredata.messages.Message

/**
 * Manages caching and refreshing of messages
 */
class Messages(network: NetworkService, private val db: SDKDatabase, private val authentication: Authentication) {

    companion object {
        private const val TAG = "Messages"
    }

    private val messagesAPI: MessagesAPI = network.create(MessagesAPI::class.java)

    /**
     * Fetch message by ID from the cache
     *
     * @param messageId Unique message ID to fetch
     *
     * @return LiveData object of Resource<Message> which can be observed using an Observer for future changes as well.
     */
    fun fetchMessage(messageId: Long): LiveData<Resource<Message>> =
            Transformations.map(db.messages().load(messageId)) { response ->
                Resource.success(response?.toMessage())
            }

    /**
     * Fetch messages from the cache
     *
     * Fetches all messages if no params are passed.
     *
     * @param messageTypes List of message types to find matching Messages for (optional)
     * @param read Fetch only read/unread messages (optional)
     * @param contentType Filter the message by the type of content it contains (optional)
     *
     * @return LiveData object of Resource<List<Message>> which can be observed using an Observer for future changes as well.
     */
    fun fetchMessages(messageTypes: List<String>? = null, read: Boolean? = null, contentType: ContentType? = null): LiveData<Resource<List<Message>>> =
            Transformations.map(db.messages().loadByQuery(generateSQLQueryMessages(messageTypes, read, contentType))) { response ->
                Resource.success(mapMessageResponse(response))
            }

    /**
     * Advanced method to fetch messages by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches messages from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<Message>> which can be observed using an Observer for future changes as well.
     */
    fun fetchMessages(query: SimpleSQLiteQuery): LiveData<Resource<List<Message>>> =
            Transformations.map(db.messages().loadByQuery(query)) { response ->
                Resource.success(mapMessageResponse(response))
            }

    /**
     * Refresh a specific message by ID from the host
     *
     * @param messageId ID of the message to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshMessage(messageId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!authentication.loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            Log.e("$TAG#refreshMessage", error.localizedDescription)
            completion?.invoke(Result.error(error))
            return
        }

        messagesAPI.fetchMessage(messageId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    handleMessageResponse(response = resource.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshMessage", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    /**
     * Refresh all available messages from the host.
     *
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshMessages(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!authentication.loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            Log.e("$TAG#refreshMessages", error.localizedDescription)
            completion?.invoke(Result.error(error))
            return
        }

        messagesAPI.fetchMessages().enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    handleMessagesResponse(response = resource.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshMessages", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    /**
     * Refresh all unread messages from the host.
     *
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshUnreadMessages(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!authentication.loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            Log.e("$TAG#refreshUnreadMessages", error.localizedDescription)
            completion?.invoke(Result.error(error))
            return
        }

        messagesAPI.fetchUnreadMessages().enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    handleMessagesResponse(response = resource.data, unread = true, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshUnreadMessages", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    /**
     * Update a message on the host
     *
     * @param messageId ID of the message to be updated
     * @param read Mark message read/unread
     * @param interacted Mark message interacted or not
     * @param messageId ID of the message to be updated
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun updateMessage(messageId: Long, read: Boolean, interacted: Boolean, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!authentication.loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            Log.e("$TAG#updateMessage", error.localizedDescription)
            completion?.invoke(Result.error(error))
            return
        }

        messagesAPI.updateMessage(messageId, MessageUpdateRequest(read, interacted)).enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    handleMessageResponse(response = resource.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateMessage", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    internal fun handleMessageNotification(notification: NotificationPayload) {
        if (notification.userMessageID == null)
            return

        refreshMessage(notification.userMessageID)
    }

    private fun handleMessagesResponse(response: List<MessageResponse>?, unread: Boolean = false, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                db.messages().insertAll(*response.toTypedArray())

                val apiIds = response.map { it.messageId }.toList()
                val staleIds = if (unread) db.messages().getUnreadStaleIds(apiIds.toLongArray())
                else db.messages().getStaleIds(apiIds.toLongArray())

                if (staleIds.isNotEmpty()) {
                    db.messages().deleteMany(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleMessageResponse(response: MessageResponse?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                db.messages().insert(response)

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun mapMessageResponse(models: List<MessageResponse>): List<Message> =
            models.mapNotNull { it.toMessage() }.toList()
}