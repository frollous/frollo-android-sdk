package us.frollo.frollosdksample.view.aggregation.adapters

import android.view.View
import kotlinx.android.synthetic.main.template_provider_account_item.view.*
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccount
import us.frollo.frollosdksample.R
import us.frollo.frollosdksample.base.BaseRecyclerAdapter
import us.frollo.frollosdksample.base.BaseViewHolder

class ProviderAccountsAdapter : BaseRecyclerAdapter<ProviderAccount, ProviderAccountsAdapter.ProviderAccountViewHolder>(ProviderAccount::class.java, providerAccountsComparator) {

    companion object {
        private val providerAccountsComparator = compareBy<ProviderAccount> { it.providerAccountId }
    }

    override fun getViewHolderLayout(viewType: Int) =
            R.layout.template_provider_account_item

    override fun getViewHolder(view: View, viewType: Int) =
            ProviderAccountViewHolder(view)

    inner class ProviderAccountViewHolder(itemView: View) : BaseViewHolder<ProviderAccount>(itemView) {

        override fun bind(model: ProviderAccount) {
            itemView.text_title.text = itemView.context.resources.getString(R.string.str_unknown_provider_account)
            itemView.text_accounts.text = itemView.context.resources.getString(R.string.str_accounts_count, 0)
        }

        override fun recycle() {
            itemView.text_title.text = null
        }
    }
}