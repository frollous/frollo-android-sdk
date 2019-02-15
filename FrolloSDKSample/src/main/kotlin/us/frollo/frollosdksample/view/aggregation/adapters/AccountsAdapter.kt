package us.frollo.frollosdksample.view.aggregation.adapters

import android.view.View
import kotlinx.android.synthetic.main.template_account_item.view.*
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Account
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshStatus
import us.frollo.frollosdksample.*
import us.frollo.frollosdksample.base.BaseRecyclerAdapter
import us.frollo.frollosdksample.base.BaseViewHolder
import us.frollo.frollosdksample.utils.display
import us.frollo.frollosdksample.utils.hide
import us.frollo.frollosdksample.utils.show
import us.frollo.frollosdksample.utils.toString

class AccountsAdapter : BaseRecyclerAdapter<Account, AccountsAdapter.AccountViewHolder>(Account::class.java, accountComparator) {

    companion object {
        private val accountComparator = compareBy<Account> { it.attributes.accountType }.thenBy { it.accountName }
    }

    override fun getViewHolderLayout(viewType: Int) =
            R.layout.template_account_item

    override fun getViewHolder(view: View, viewType: Int) =
            AccountViewHolder(view)

    inner class AccountViewHolder(itemView: View) : BaseViewHolder<Account>(itemView) {

        override fun bind(model: Account) {
            itemView.text_account_name.text = model.nickName ?: model.accountName
            itemView.text_last_updated.text = model.refreshStatus?.lastRefreshed?.let {
                String.format("Last updated %s", it.toString("dd/MM/yyyy"))
            } ?: run { "Never updated" }
            itemView.text_amount.text = model.availableBalance?.display ?: model.currentBalance?.display

            itemView.text_amount.show()
            itemView.progress_bar.hide()

            when (model.refreshStatus?.status) {
                AccountRefreshStatus.ADDING -> {
                    itemView.progress_bar.show()
                    itemView.text_amount.hide()
                    itemView.text_status.text = "Adding"
                }
                AccountRefreshStatus.FAILED -> itemView.text_status.text = "Failed"
                AccountRefreshStatus.NEEDS_ACTION -> itemView.text_status.text = "Needs Action"
                AccountRefreshStatus.SUCCESS -> itemView.text_status.text = "Success"
                AccountRefreshStatus.UPDATING -> {
                    itemView.text_amount.hide()
                    itemView.text_status.text = "Updating"
                }
            }
        }

        override fun recycle() {
            itemView.text_account_name.text = null
            itemView.text_status.text = null
            itemView.text_last_updated.text = null
            itemView.text_amount.text = null
        }
    }
}