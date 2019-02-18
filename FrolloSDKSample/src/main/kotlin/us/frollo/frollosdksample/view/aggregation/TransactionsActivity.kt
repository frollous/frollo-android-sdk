package us.frollo.frollosdksample.view.aggregation

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_transactions.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.onRefresh
import org.threeten.bp.LocalDate
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.model.coredata.aggregation.transactions.Transaction
import us.frollo.frollosdksample.base.ARGUMENT
import us.frollo.frollosdksample.R
import us.frollo.frollosdksample.base.BaseStackActivity
import us.frollo.frollosdksample.utils.displayError
import us.frollo.frollosdksample.utils.observe
import us.frollo.frollosdksample.utils.toString
import us.frollo.frollosdksample.view.aggregation.adapters.TransactionsAdapter

class TransactionsActivity : BaseStackActivity() {

    companion object {
        private const val TAG = "TransactionsActivity"
    }

    private val transactionsAdapter = TransactionsAdapter()
    private var accountId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_transactions)

        accountId = intent.getLongExtra(ARGUMENT.ARG_GENERIC, -1)

        initView()
        initLiveData()
        refresh_layout.onRefresh { refreshTransactions() }
    }

    override fun onResume() {
        super.onResume()

        refreshTransactions()
    }

    private fun initView() {
        recycler_transactions.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(this@TransactionsActivity, LinearLayoutManager.VERTICAL))
            adapter = transactionsAdapter.apply {
                onItemClick { model, _, _ ->
                    model?.let { showTransactionDetails(it) }
                }
            }
        }
    }

    private fun initLiveData() {
        FrolloSDK.aggregation.fetchTransactionsByAccountId(accountId).observe(this) {
            when (it?.status) {
                Resource.Status.SUCCESS -> it.data?.let { transactions -> loadTransactions(transactions) }
                Resource.Status.ERROR -> displayError(it.error?.localizedDescription, "Fetch Transactions Failed")
                Resource.Status.LOADING -> Log.d(TAG, "Loading Transactions...")
            }
        }
    }

    private fun loadTransactions(transactions: List<Transaction>) {
        transactionsAdapter.replaceAll(transactions)
    }

    private fun refreshTransactions() {
        val toDate = LocalDate.now().toString(Transaction.DATE_FORMAT_PATTERN)
        val fromDate = LocalDate.now().minusMonths(3).toString(Transaction.DATE_FORMAT_PATTERN) // 3 months ago

        FrolloSDK.aggregation.refreshTransactions(fromDate = fromDate, toDate = toDate, accountIds = longArrayOf(accountId)) { error ->
            refresh_layout.isRefreshing = false
            if (error != null)
                displayError(error.localizedDescription, "Refreshing Transactions Failed")
        }
    }

    private fun showTransactionDetails(transaction: Transaction) {
        startActivity<TransactionDetailsActivity>(ARGUMENT.ARG_GENERIC to transaction.transactionId)
    }
}
