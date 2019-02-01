package us.frollo.frollosdk.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.data.local.SDKDatabase
import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.data.remote.api.MessagesAPI
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.generateSQLQueryMessages
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toMessage
import us.frollo.frollosdk.model.api.messages.MessageResponse
import us.frollo.frollosdk.model.api.messages.MessageUpdateRequest
import us.frollo.frollosdk.model.coredata.notifications.NotificationPayload
import us.frollo.frollosdk.model.coredata.messages.Message

/**
 * Manages caching and refreshing of messages
 */
class Messages(network: NetworkService, private val db: SDKDatabase) {

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
            }.apply { (this as? MutableLiveData<Resource<Message>>)?.value = Resource.loading(null) }

    /**
     * Fetch messages from the cache
     *
     * Fetches all messages if no params are passed.
     *
     * @param messageTypes: List of message types to find matching Messages for (optional)
     * @param read Fetch only read/unread messages (optional)
     *
     * @return LiveData object of Resource<List<Message>> which can be observed using an Observer for future changes as well.
     */
    fun fetchMessages(messageTypes: List<String>? = null, read: Boolean? = null): LiveData<Resource<List<Message>>> {
        return if (messageTypes != null) {
            Transformations.map(db.messages().loadByQuery(generateSQLQueryMessages(messageTypes, read))) { response ->
                Resource.success(messageRules(response))
            }.apply { (this as? MutableLiveData<Resource<List<Message>>>)?.value = Resource.loading(null) }
        } else if (read != null) {
            Transformations.map(db.messages().load(read)) { response ->
                Resource.success(messageRules(response))
            }.apply { (this as? MutableLiveData<Resource<List<Message>>>)?.value = Resource.loading(null) }
        } else {
            Transformations.map(db.messages().load()) { response ->
                Resource.success(messageRules(response))
            }.apply { (this as? MutableLiveData<Resource<List<Message>>>)?.value = Resource.loading(null) }
        }
    }

    /**
     * Refresh a specific message by ID from the host
     *
     * @param messageId ID of the message to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshMessage(messageId: Long, completion: OnFrolloSDKCompletionListener? = null) {
        messagesAPI.fetchMessage(messageId).enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#refreshMessage", error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // HACK: invoke completion callback if response is null else app will never be notified if response is null.
                completion?.invoke(null)
            } else
                handleMessageResponse(response, completion)
        }
    }

    /**
     * Refresh all available messages from the host.
     *
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshMessages(completion: OnFrolloSDKCompletionListener? = null) {
        messagesAPI.fetchMessages().enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#refreshMessages", error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // HACK: invoke completion callback if response is null else app will never be notified if response is null.
                completion?.invoke(null)
            } else
                handleMessagesResponse(response = response, completion = completion)
        }
    }

    /**
     * Refresh all unread messages from the host.
     *
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshUnreadMessages(completion: OnFrolloSDKCompletionListener? = null) {
        messagesAPI.fetchUnreadMessages().enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#refreshUnreadMessages", error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // HACK: invoke completion callback if response is null else app will never be notified if response is null.
                completion?.invoke(null)
            } else
                handleMessagesResponse(response = response, unread = true, completion = completion)
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
    fun updateMessage(messageId: Long, read: Boolean, interacted: Boolean, completion: OnFrolloSDKCompletionListener? = null) {
        messagesAPI.updateMessage(messageId, MessageUpdateRequest(read, interacted)).enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#updateMessage", error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // HACK: invoke completion callback if response is null else app will never be notified if response is null.
                completion?.invoke(null)
            } else
                handleMessageResponse(response, completion)
        }
    }

    internal fun handleMessageNotification(notification: NotificationPayload) {
        if (notification.userMessageID == null)
            return

        refreshMessage(notification.userMessageID)
    }

    private fun handleMessagesResponse(response: List<MessageResponse>, unread: Boolean = false, completion: OnFrolloSDKCompletionListener? = null) {
        doAsync {
            db.messages().insertAll(*response.toTypedArray())

            val apiIds = response.map { it.messageId }.toList()
            val staleIds = if (unread) db.messages().getUnreadStaleIds(apiIds.toLongArray())
                           else db.messages().getStaleIds(apiIds.toLongArray())

            if (staleIds.isNotEmpty()) {
                db.messages().deleteMany(staleIds.toLongArray())
            }

            uiThread { completion?.invoke(null) }
        }
    }

    private fun handleMessageResponse(response: MessageResponse, completion: OnFrolloSDKCompletionListener? = null) {
        doAsync {
            db.messages().insert(response)

            uiThread { completion?.invoke(null) }
        }
    }

    private fun messageRules(models: List<MessageResponse>): List<Message> =
            models.mapNotNull { it.toMessage() }.toList()
}