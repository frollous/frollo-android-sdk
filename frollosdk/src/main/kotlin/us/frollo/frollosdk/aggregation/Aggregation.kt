package us.frollo.frollosdk.aggregation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.data.local.SDKDatabase
import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.data.remote.api.AggregationAPI
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toProvider
import us.frollo.frollosdk.model.api.aggregation.providers.ProviderResponse
import us.frollo.frollosdk.model.coredata.aggregation.providers.Provider

/**
 * Manages all aggregation data including accounts, transactions, categories and merchants.
 */
class Aggregation(network: NetworkService, private val db: SDKDatabase) {

    companion object {
        private const val TAG = "Aggregation"
    }

    private val aggregationAPI: AggregationAPI = network.create(AggregationAPI::class.java)

    //TODO: Refresh Transactions Broadcast local implementation

    fun fetchProvider(providerId: Long): LiveData<Resource<Provider>> =
            Transformations.map(db.providers().load(providerId)) { response ->
                Resource.success(response?.toProvider())
            }.apply { (this as? MutableLiveData<Resource<Provider>>)?.value = Resource.loading(null) }

    fun fetchProviders(): LiveData<Resource<List<Provider>>> =
            Transformations.map(db.providers().load()) { response ->
                Resource.success(mapProviderResponse(response))
            }.apply { (this as? MutableLiveData<Resource<List<Provider>>>)?.value = Resource.loading(null) }

    fun refreshProvider(providerId: Long, completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.fetchProvider(providerId).enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#refreshProvider", error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // Explicitly invoke completion callback if response is null.
                completion?.invoke(null)
            } else
                handleProviderResponse(response, completion)
        }
    }

    fun refreshProviders(completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.fetchProviders().enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#refreshProviders", error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // Explicitly invoke completion callback if response is null.
                completion?.invoke(null)
            } else {
                handleProvidersResponse(response = response, completion = completion)
            }
        }
    }

    private fun handleProvidersResponse(response: List<ProviderResponse>, completion: OnFrolloSDKCompletionListener? = null) {
        doAsync {
            db.providers().insertAll(*response.toTypedArray())

            val apiIds = response.map { it.providerId }.toList()
            val staleIds = db.providers().getStaleIds(apiIds.toLongArray())

            if (staleIds.isNotEmpty()) {
                db.providers().deleteMany(staleIds.toLongArray())
            }

            uiThread { completion?.invoke(null) }
        }
    }

    private fun handleProviderResponse(response: ProviderResponse, completion: OnFrolloSDKCompletionListener? = null) {
        doAsync {
            db.providers().insert(response)

            uiThread { completion?.invoke(null) }
        }
    }

    private fun mapProviderResponse(models: List<ProviderResponse>): List<Provider> =
            models.mapNotNull { it.toProvider() }.toList()
}