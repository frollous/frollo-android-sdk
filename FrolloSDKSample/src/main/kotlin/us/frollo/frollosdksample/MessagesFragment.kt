package us.frollo.frollosdksample

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import us.frollo.frollosdksample.base.BaseFragment

class MessagesFragment : BaseFragment() {

    companion object {
        private const val TAG = "MessagesFragment"
    }

    private val messagesAdapter = MessagesAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        actionBar?.title = getString(R.string.title_messages)
        initView()
        initLiveData()
        refresh_layout.onRefresh { refreshUnreadMessages() }
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
        viewLifecycleOwner?.let { owner ->
            FrolloSDK.messages.fetchMessages(read = false).observe(owner) {
                when (it?.status) {
                    Resource.Status.SUCCESS -> loadMessages(it.data)
                    Resource.Status.ERROR -> displayError(it.error?.localizedDescription, "Fetch Messages Failed")
                    Resource.Status.LOADING -> Log.d(TAG, "Loading Messages...")
                }
            }
        }
    }

    private fun loadMessages(messages: List<Message>?) {
        val textMessages = messages?.filter { it.contentType == ContentType.TEXT }?.map { it as MessageText }?.toList()
        textMessages?.let { messagesAdapter.replaceAll(textMessages) }
    }

    private fun refreshUnreadMessages() {
        FrolloSDK.messages.refreshUnreadMessages { error ->
            refresh_layout.isRefreshing = false
            if (error != null)
                displayError(error.localizedDescription, "Refreshing Messages Failed")
        }
    }
}
