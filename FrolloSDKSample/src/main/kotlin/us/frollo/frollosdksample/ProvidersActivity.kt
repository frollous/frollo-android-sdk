package us.frollo.frollosdksample

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_providers.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.onRefresh
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.model.coredata.aggregation.providers.Provider
import us.frollo.frollosdksample.adapter.ProvidersAdapter
import us.frollo.frollosdksample.base.BaseStackActivity

class ProvidersActivity : BaseStackActivity() {

    companion object {
        private const val TAG = "ProvidersActivity"
    }

    private val providersAdapter = ProvidersAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_providers)

        initView()
        initLiveData()
        refresh_layout.onRefresh { refreshProviders() }
    }

    private fun initView() {
        recycler_providers.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(this@ProvidersActivity, LinearLayoutManager.VERTICAL))
            adapter = providersAdapter.apply {
                onItemClick { model, _, _ ->
                    model?.let { showProviderLoginForm(it) }
                }
            }
        }
    }

    private fun initLiveData() {
        FrolloSDK.aggregation.fetchProviders().observe(this) {
            when (it?.status) {
                Resource.Status.SUCCESS -> it.data?.let { providers -> loadProviders(providers) }
                Resource.Status.ERROR -> displayError(it.error?.localizedDescription, "Fetch Providers Failed")
                Resource.Status.LOADING -> Log.d(TAG, "Loading Providers...")
            }
        }
    }

    private fun loadProviders(providers: List<Provider>) {
        providersAdapter.replaceAll(providers)
    }

    private fun refreshProviders() {
        FrolloSDK.aggregation.refreshProviders { error ->
            refresh_layout.isRefreshing = false
            if (error != null)
                displayError(error.localizedDescription, "Refreshing Providers Failed")
        }
    }

    private fun showProviderLoginForm(provider: Provider) {
        startActivity<AddProviderAccountActivity>(ARGUMENT.ARG_GENERIC to provider.providerId)
    }
}
