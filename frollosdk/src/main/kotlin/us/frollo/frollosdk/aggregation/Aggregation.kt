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
import us.frollo.frollosdk.mapping.toProviderAccount
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountCreateRequest
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountResponse
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountUpdateRequest
import us.frollo.frollosdk.model.api.aggregation.providers.ProviderResponse
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccount
import us.frollo.frollosdk.model.coredata.aggregation.providers.Provider
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderLoginForm

/**
 * Manages all aggregation data including accounts, transactions, categories and merchants.
 */
class Aggregation(network: NetworkService, private val db: SDKDatabase) {

    companion object {
        private const val TAG = "Aggregation"
    }

    private val aggregationAPI: AggregationAPI = network.create(AggregationAPI::class.java)

    //TODO: Refresh Transactions Broadcast local implementation

    // Provider

    fun fetchProvider(providerId: Long): LiveData<Resource<Provider>> =
            Transformations.map(db.providers().load(providerId)) { model ->
                Resource.success(model)
            }.apply { (this as? MutableLiveData<Resource<Provider>>)?.value = Resource.loading(null) }

    fun fetchProviders(): LiveData<Resource<List<Provider>>> =
            Transformations.map(db.providers().load()) { models ->
                Resource.success(models)
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
            val models = mapProviderResponse(response)
            db.providers().insertAll(*models.toTypedArray())

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
            db.providers().insert(response.toProvider())

            uiThread { completion?.invoke(null) }
        }
    }

    private fun mapProviderResponse(models: List<ProviderResponse>): List<Provider> =
            models.map { it.toProvider() }.toList()

    // Provider Account

    fun fetchProviderAccount(providerAccountId: Long): LiveData<Resource<ProviderAccount>> =
            Transformations.map(db.provideraccounts().load(providerAccountId)) { model ->
                Resource.success(model)
            }.apply { (this as? MutableLiveData<Resource<ProviderAccount>>)?.value = Resource.loading(null) }

    fun fetchProviderAccounts(): LiveData<Resource<List<ProviderAccount>>> =
            Transformations.map(db.provideraccounts().load()) { models ->
                Resource.success(models)
            }.apply { (this as? MutableLiveData<Resource<List<ProviderAccount>>>)?.value = Resource.loading(null) }

    fun refreshProviderAccount(providerAccountId: Long, completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.fetchProviderAccount(providerAccountId).enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#refreshProviderAccount", error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // Explicitly invoke completion callback if response is null.
                completion?.invoke(null)
            } else
                handleProviderAccountResponse(response, completion)
        }
    }

    fun refreshProviderAccounts(completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.fetchProviderAccounts().enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#refreshProviderAccounts", error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // Explicitly invoke completion callback if response is null.
                completion?.invoke(null)
            } else {
                handleProviderAccountsResponse(response = response, completion = completion)
            }
        }
    }

    fun createProviderAccount(providerId: Long, loginForm: ProviderLoginForm, completion: OnFrolloSDKCompletionListener? = null) {
        val request = ProviderAccountCreateRequest(loginForm = loginForm, providerID = providerId)

        aggregationAPI.createProviderAccount(request).enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#createProviderAccount", error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // Explicitly invoke completion callback if response is null.
                completion?.invoke(null)
            } else
                handleProviderAccountResponse(response, completion)
        }
    }

    fun deleteProviderAccount(providerAccountId: Long, completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.deleteProviderAccount(providerAccountId).enqueue { _, error ->
            if (error != null) {
                Log.e("$TAG#deleteProviderAccount", error.localizedDescription)
            }

            removeCachedProviderAccount(providerAccountId)

            // Manually delete other data linked to this provider account
            // as we are not using ForeignKeys because ForeignKey constraints
            // do not allow to insert data into child table prior to parent table
            //TODO: Manually delete other data linked to this provider account

            completion?.invoke(error)
        }
    }

    fun updateProviderAccount(providerAccountId: Long, loginForm: ProviderLoginForm, completion: OnFrolloSDKCompletionListener? = null) {
        val request = ProviderAccountUpdateRequest(loginForm = loginForm)

        aggregationAPI.updateProviderAccount(providerAccountId, request).enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#updateProviderAccount", error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // Explicitly invoke completion callback if response is null.
                completion?.invoke(null)
            } else
                handleProviderAccountResponse(response, completion)
        }
    }

    private fun handleProviderAccountsResponse(response: List<ProviderAccountResponse>, completion: OnFrolloSDKCompletionListener? = null) {
        doAsync {
            val models = mapProviderAccountResponse(response)
            db.provideraccounts().insertAll(*models.toTypedArray())

            val apiIds = response.map { it.providerAccountId }.toList()
            val staleIds = db.provideraccounts().getStaleIds(apiIds.toLongArray())

            if (staleIds.isNotEmpty()) {
                db.provideraccounts().deleteMany(staleIds.toLongArray())
            }

            uiThread { completion?.invoke(null) }
        }
    }

    private fun handleProviderAccountResponse(response: ProviderAccountResponse, completion: OnFrolloSDKCompletionListener? = null) {
        doAsync {
            db.provideraccounts().insert(response.toProviderAccount())

            uiThread { completion?.invoke(null) }
        }
    }

    private fun mapProviderAccountResponse(models: List<ProviderAccountResponse>): List<ProviderAccount> =
            models.map { it.toProviderAccount() }.toList()

    private fun removeCachedProviderAccount(providerAccountId: Long) {
        doAsync {
            db.provideraccounts().delete(providerAccountId)
        }
    }
}