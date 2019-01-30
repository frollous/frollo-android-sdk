package us.frollo.frollosdksample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.template_message_item.view.*
import us.frollo.frollosdk.model.coredata.messages.MessageText
import us.frollo.frollosdksample.R
import us.frollo.frollosdksample.hide
import us.frollo.frollosdksample.show

class MessagesAdapter : RecyclerView.Adapter<MessagesAdapter.MyViewHolder>() {

    private var dataSet = listOf<MessageText>()

    fun replaceAll(data: List<MessageText>) {
        dataSet = data
        notifyDataSetChanged()
    }

    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesAdapter.MyViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.template_message_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        loadText(holder.view.text_header, dataSet[position].header)
        loadText(holder.view.text_title, dataSet[position].title)
        loadText(holder.view.text_body, dataSet[position].text)
        loadText(holder.view.text_footer, dataSet[position].footer)
        loadText(holder.view.text_action, dataSet[position].action?.title)
    }

    private fun loadText(tv: TextView, text: String?) {
        text?.let {
            tv.show()
            tv.text = it
        } ?: run {
            tv.hide()
        }
    }

    override fun getItemCount() = dataSet.size
}