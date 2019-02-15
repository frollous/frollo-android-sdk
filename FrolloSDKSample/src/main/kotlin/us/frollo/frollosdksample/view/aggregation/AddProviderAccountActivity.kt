package us.frollo.frollosdksample.view.aggregation

import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_add_provider_account.*
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.model.coredata.aggregation.providers.Provider
import us.frollo.frollosdksample.base.ARGUMENT
import us.frollo.frollosdksample.R
import us.frollo.frollosdksample.base.BaseStackActivity
import us.frollo.frollosdksample.utils.displayError
import us.frollo.frollosdksample.utils.observe

class AddProviderAccountActivity : BaseStackActivity() {

    companion object {
        private const val TAG = "AddProviderAccount"
    }

    private var providerId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_provider_account)

        providerId = intent.getLongExtra(ARGUMENT.ARG_GENERIC, -1)

        initLiveData()

        refreshProvider()
    }

    private fun initLiveData() {
        FrolloSDK.aggregation.fetchProvider(providerId).observe(this) {
            when (it?.status) {
                Resource.Status.SUCCESS -> it.data?.let { provider -> loadProviderForm(provider) }
                Resource.Status.ERROR -> displayError(it.error?.localizedDescription, "Fetch Provider Failed")
                Resource.Status.LOADING -> Log.d(TAG, "Loading Provider...")
            }
        }
    }

    private fun refreshProvider() {
        FrolloSDK.aggregation.refreshProvider(providerId) { error ->
            if (error != null)
                displayError(error.localizedDescription, "Refreshing Provider Failed")
        }
    }

    private fun loadProviderForm(provider: Provider) {
        text_title.text = provider.providerName
        text_form.text = String.format("Form Type: %s", provider.loginForm?.formType.toString())
        //TODO: to be implemented
    }
}
