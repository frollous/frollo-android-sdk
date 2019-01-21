package us.frollo.frollosdk.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import timber.log.Timber
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.data.local.SDKDatabase
import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.data.remote.api.MessagesAPI
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.generateSQLQueryMessages
import us.frollo.frollosdk.mapping.toMessage
import us.frollo.frollosdk.model.api.messages.MessageResponse
import us.frollo.frollosdk.model.api.messages.MessageUpdateRequest
import us.frollo.frollosdk.model.coredata.messages.Message

class Messages(network: NetworkService, private val db: SDKDatabase) {

    private val messagesAPI: MessagesAPI = network.create(MessagesAPI::class.java)

    fun fetchMessage(messageId: Long): LiveData<Resource<Message>> =
            Transformations.map(db.messages().load(messageId)) { response ->
                Resource.success(response?.toMessage())
            }.apply { (this as? MutableLiveData<Resource<Message>>)?.value = Resource.loading(null) }

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

    fun refreshMessage(messageId: Long, completion: OnFrolloSDKCompletionListener? = null) {
        messagesAPI.fetchMessage(messageId).enqueue { response, error ->
            if (error != null) {
                Timber.d(error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // HACK: invoke completion callback if response is null else app will never be notified if response is null.
                completion?.invoke(null)
            } else
                handleMessageResponse(response, completion)
        }
    }

    fun refreshMessages(completion: OnFrolloSDKCompletionListener? = null) {
        messagesAPI.fetchMessages().enqueue { response, error ->
            if (error != null) {
                Timber.d(error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // HACK: invoke completion callback if response is null else app will never be notified if response is null.
                completion?.invoke(null)
            } else
                handleMessagesResponse(response = response, completion = completion)
        }
    }

    fun refreshUnreadMessages(completion: OnFrolloSDKCompletionListener? = null) {
        messagesAPI.fetchUnreadMessages().enqueue { response, error ->
            if (error != null) {
                Timber.d(error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // HACK: invoke completion callback if response is null else app will never be notified if response is null.
                completion?.invoke(null)
            } else
                handleMessagesResponse(response = response, unread = true, completion = completion)
        }
    }

    //TODO: interacted might not be getting updated. Verify.
    fun updateMessage(messageId: Long, read: Boolean, interacted: Boolean, completion: OnFrolloSDKCompletionListener? = null) {
        messagesAPI.updateMessage(messageId, MessageUpdateRequest(read, interacted)).enqueue { response, error ->
            if (error != null) {
                Timber.d(error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // HACK: invoke completion callback if response is null else app will never be notified if response is null.
                completion?.invoke(null)
            } else
                handleMessageResponse(response, completion)
        }
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