package us.frollo.frollosdkapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_messages.*
import timber.log.Timber
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.error.APIError
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.FrolloSDKError

class MessagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_messages)

        initView()
    }

    private fun initView() {
        btn_refresh_unread_messages.setOnClickListener { refreshUnreadMessages() }
        btn_fetch_unread_messages.setOnClickListener { fetchUnreadMessages() }
        btn_update_message.setOnClickListener { updateMessage() }
    }

    private fun fetchUnreadMessages() {
        FrolloSDK.messages.fetchMessages(read = false).observe(this) {
            when (it?.status) {
                Resource.Status.SUCCESS -> Timber.d("*** Unread messages size = ${ it.data?.size }")
                Resource.Status.ERROR -> Timber.d("Error fetching messages from cache")
                Resource.Status.LOADING -> Timber.d("Fetching messages from cache...")
            }
        }
    }

    private fun refreshUnreadMessages() {
        FrolloSDK.messages.refreshUnreadMessages { error ->
            if (error != null) handleError(error)
            else Timber.d("*** Unread messages refreshed")
        }
    }

    private fun updateMessage() {
        FrolloSDK.messages.updateMessage(messageId = 58213, read = true, interacted = true) { error ->
            if (error != null) handleError(error)
            else Timber.d("*** Message updated")
        }
    }

    private fun handleError(error: FrolloSDKError) {
        when (error) {
            is APIError -> Timber.d("*** Error: ${error.errorCode} - ${error.localizedDescription}")
            is DataError -> Timber.d("*** Error: ${error.type} - ${error.localizedDescription}")
            else -> Timber.d("*** Error: ${error.localizedDescription}")
        }
    }
}
