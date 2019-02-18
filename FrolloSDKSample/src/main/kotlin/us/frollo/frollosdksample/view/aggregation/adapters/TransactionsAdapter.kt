package us.frollo.frollosdksample.view.aggregation.adapters

import android.view.View
import kotlinx.android.synthetic.main.template_transaction_item.view.*
import us.frollo.frollosdk.model.coredata.aggregation.transactions.Transaction
import us.frollo.frollosdksample.*
import us.frollo.frollosdksample.base.BaseRecyclerAdapter
import us.frollo.frollosdksample.base.BaseViewHolder
import us.frollo.frollosdksample.utils.*

class TransactionsAdapter : BaseRecyclerAdapter<Transaction, TransactionsAdapter.TransactionViewHolder>(Transaction::class.java, transactionComparator) {

    companion object {
        private val transactionComparator = compareByDescending<Transaction> { it.transactionDate }
    }

    override fun getViewHolderLayout(viewType: Int) =
            R.layout.template_transaction_item

    override fun getViewHolder(view: View, viewType: Int) =
            TransactionViewHolder(view)

    inner class TransactionViewHolder(itemView: View) : BaseViewHolder<Transaction>(itemView) {

        override fun bind(model: Transaction) {
            itemView.text_name.text = model.description?.user ?: model.description?.simple ?: model.description?.original
            itemView.text_date.text = model.transactionDate.changeDateFormat(
                    originalPattern = Transaction.DATE_FORMAT_PATTERN, newPattern = "dd/MM/yyyy")
            itemView.text_amount.text = model.amount.display
        }

        override fun recycle() {
            itemView.text_name.text = null
            itemView.text_date.text = null
            itemView.text_amount.text = null
        }
    }
}