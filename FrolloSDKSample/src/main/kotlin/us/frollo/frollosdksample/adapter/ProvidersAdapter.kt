package us.frollo.frollosdksample.adapter

import android.view.View
import kotlinx.android.synthetic.main.template_message_item.view.*
import us.frollo.frollosdk.model.coredata.aggregation.providers.Provider
import us.frollo.frollosdksample.R
import us.frollo.frollosdksample.base.BaseRecyclerAdapter
import us.frollo.frollosdksample.base.BaseViewHolder

class ProvidersAdapter : BaseRecyclerAdapter<Provider, ProvidersAdapter.ProviderViewHolder>(Provider::class.java, providerComparator) {

    companion object {
        private val providerComparator = compareByDescending<Provider> { it.popular }.thenBy { it.providerName }
    }

    override fun getViewHolderLayout(viewType: Int) =
            R.layout.template_provider_item

    override fun getViewHolder(view: View, viewType: Int) =
            ProviderViewHolder(view)

    inner class ProviderViewHolder(itemView: View) : BaseViewHolder<Provider>(itemView) {

        override fun bind(model: Provider) {
            itemView.text_title.text = model.providerName
        }

        override fun recycle() {
            itemView.text_title.text = null
        }
    }
}