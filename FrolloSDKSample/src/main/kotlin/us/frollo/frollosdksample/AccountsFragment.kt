package us.frollo.frollosdksample

import android.os.Bundle
import android.view.*
import org.jetbrains.anko.support.v4.startActivity
import us.frollo.frollosdksample.base.BaseFragment

class AccountsFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.accounts_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add -> {
                addAccount()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addAccount() {
        startActivity<ProvidersActivity>()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_accounts, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        actionBar?.title = getString(R.string.title_provider_accounts)
    }
}
