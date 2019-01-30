package us.frollo.frollosdksample

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_messages.*
import androidx.recyclerview.widget.DividerItemDecoration
import org.jetbrains.anko.support.v4.onRefresh
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.model.coredata.messages.ContentType
import us.frollo.frollosdk.model.coredata.messages.Message
import us.frollo.frollosdk.model.coredata.messages.MessageText
import us.frollo.frollosdksample.adapter.MessagesAdapter

class MessagesFragment : Fragment() {

    companion object {
        private const val TAG = "MessagesFragment"
    }

    private val messagesAdapter = MessagesAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initLiveData()
        refresh_layout.onRefresh {
            refreshUnreadMessages()
        }
    }

    private fun initView() {
        recycler_messages.apply {
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = false
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
            adapter = messagesAdapter
        }
    }

    private fun initLiveData() {
        FrolloSDK.messages.fetchMessages(read = false).observe(this) {
            when (it?.status) {
                Resource.Status.SUCCESS -> {
                    Log.d(TAG, "*** Success Fetching Messages")
                    refresh_layout.isRefreshing = false
                    loadMessages(it.data)
                }
                Resource.Status.ERROR -> {
                    refresh_layout.isRefreshing = false
                    displayError(it.error?.localizedDescription, "Fetch Messages Failed")
                }
                Resource.Status.LOADING -> refresh_layout.isRefreshing = true
            }
        }
    }

    private fun loadMessages(messages: List<Message>?) {
        val textMessages = messages?.filter { it.contentType == ContentType.TEXT }?.map { it as MessageText }?.toList()
        textMessages?.let { messagesAdapter.replaceAll(textMessages) }
    }

    private fun refreshUnreadMessages() {
        FrolloSDK.messages.refreshUnreadMessages { error ->
            if (error != null)
                displayError(error.localizedDescription, "Refreshing Messages Failed")
        }
    }
}
