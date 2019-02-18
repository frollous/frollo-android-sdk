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
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.extensions.*
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toAccount
import us.frollo.frollosdk.mapping.toProvider
import us.frollo.frollosdk.mapping.toProviderAccount
import us.frollo.frollosdk.mapping.toTransaction
import us.frollo.frollosdk.model.api.aggregation.accounts.AccountResponse
import us.frollo.frollosdk.model.api.aggregation.accounts.AccountUpdateRequest
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountCreateRequest
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountResponse
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountUpdateRequest
import us.frollo.frollosdk.model.api.aggregation.providers.ProviderResponse
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionResponse
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionUpdateRequest
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Account
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountSubType
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccount
import us.frollo.frollosdk.model.coredata.aggregation.providers.Provider
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderLoginForm
import us.frollo.frollosdk.model.coredata.aggregation.transactions.Transaction
import kotlin.collections.ArrayList

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

    fun fetchProviderAccountsByProviderId(providerId: Long): LiveData<Resource<List<ProviderAccount>>> =
            Transformations.map(db.provideraccounts().loadByProviderId(providerId)) { models ->
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

    // Account

    fun fetchAccount(accountId: Long): LiveData<Resource<Account>> =
            Transformations.map(db.accounts().load(accountId)) { model ->
                Resource.success(model)
            }.apply { (this as? MutableLiveData<Resource<Account>>)?.value = Resource.loading(null) }

    fun fetchAccounts(): LiveData<Resource<List<Account>>> =
            Transformations.map(db.accounts().load()) { models ->
                Resource.success(models)
            }.apply { (this as? MutableLiveData<Resource<List<Account>>>)?.value = Resource.loading(null) }

    fun fetchAccountsByProviderAccountId(providerAccountId: Long): LiveData<Resource<List<Account>>> =
            Transformations.map(db.accounts().loadByProviderAccountId(providerAccountId)) { models ->
                Resource.success(models)
            }.apply { (this as? MutableLiveData<Resource<List<Account>>>)?.value = Resource.loading(null) }

    fun refreshAccount(accountId: Long, completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.fetchAccount(accountId).enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#refreshAccount", error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // Explicitly invoke completion callback if response is null.
                completion?.invoke(null)
            } else
                handleAccountResponse(response, completion)
        }
    }

    fun refreshAccounts(completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.fetchAccounts().enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#refreshAccounts", error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // Explicitly invoke completion callback if response is null.
                completion?.invoke(null)
            } else {
                handleAccountsResponse(response = response, completion = completion)
            }
        }
    }

    fun updateAccount(accountId: Long, hidden: Boolean, included: Boolean, favourite: Boolean? = null,
                      accountSubType: AccountSubType? = null, nickName: String? = null,
                      completion: OnFrolloSDKCompletionListener? = null) {

        val request = AccountUpdateRequest(
                hidden = hidden,
                included = included,
                favourite = favourite,
                accountSubType = accountSubType,
                nickName = nickName)

        if (!request.valid) {
            Log.e("$TAG#updateAccount", "'hidden' and 'included' must compliment each other. Both cannot be true.")
            completion?.invoke(DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA))
            return
        }

        aggregationAPI.updateAccount(accountId, request).enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#updateAccount", error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // Explicitly invoke completion callback if response is null.
                completion?.invoke(null)
            } else
                handleAccountResponse(response, completion)
        }
    }

    private fun handleAccountsResponse(response: List<AccountResponse>, completion: OnFrolloSDKCompletionListener? = null) {
        doAsync {
            val models = mapAccountResponse(response)
            db.accounts().insertAll(*models.toTypedArray())

            val apiIds = response.map { it.accountId }.toList()
            val staleIds = db.accounts().getStaleIds(apiIds.toLongArray())

            if (staleIds.isNotEmpty()) {
                db.accounts().deleteMany(staleIds.toLongArray())
            }

            uiThread { completion?.invoke(null) }
        }
    }

    private fun handleAccountResponse(response: AccountResponse, completion: OnFrolloSDKCompletionListener? = null) {
        doAsync {
            db.accounts().insert(response.toAccount())

            uiThread { completion?.invoke(null) }
        }
    }

    private fun mapAccountResponse(models: List<AccountResponse>): List<Account> =
            models.map { it.toAccount() }.toList()

    // Transaction

    fun fetchTransaction(transactionId: Long): LiveData<Resource<Transaction>> =
            Transformations.map(db.transactions().load(transactionId)) { model ->
                Resource.success(model)
            }.apply { (this as? MutableLiveData<Resource<Transaction>>)?.value = Resource.loading(null) }

    fun fetchTransactions(transactionIds: LongArray? = null): LiveData<Resource<List<Transaction>>> {
        val result = if (transactionIds != null) db.transactions().load(transactionIds)
                     else db.transactions().load()

        return Transformations.map(result) { models ->
            Resource.success(models)
        }.apply { (this as? MutableLiveData<Resource<List<Transaction>>>)?.value = Resource.loading(null) }
    }

    fun fetchTransactionsByAccountId(accountId: Long): LiveData<Resource<List<Transaction>>> =
            Transformations.map(db.transactions().loadByAccountId(accountId)) { models ->
                Resource.success(models)
            }.apply { (this as? MutableLiveData<Resource<List<Transaction>>>)?.value = Resource.loading(null) }

    fun refreshTransaction(transactionId: Long, completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.fetchTransaction(transactionId).enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#refreshTransaction", error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // Explicitly invoke completion callback if response is null.
                completion?.invoke(null)
            } else
                handleTransactionResponse(response, completion)
        }
    }

    fun refreshTransactions(fromDate: String, toDate: String, accountIds: LongArray? = null,
                            transactionIncluded: Boolean? = null, completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.fetchTransactionsByQuery(fromDate = fromDate, toDate = toDate,
                accountIds = accountIds, transactionIncluded = transactionIncluded).enqueue { response, error ->

            if (error != null) {
                Log.e("$TAG#refreshTransactionsByQuery", error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // Explicitly invoke completion callback if response is null.
                completion?.invoke(null)
            } else {
                handleTransactionsResponse(response = response, fromDate = fromDate, toDate = toDate,
                        accountIds = accountIds, transactionIncluded = transactionIncluded, completion = completion)
            }
        }
    }

    fun refreshTransactions(transactionIds: LongArray, completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.fetchTransactionsByIDs(transactionIds).enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#refreshTransactionsByIDs", error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // Explicitly invoke completion callback if response is null.
                completion?.invoke(null)
            } else {
                handleTransactionsResponse(response = response, completion = completion)
            }
        }
    }

    fun updateTransaction(transactionId: Long, transaction: Transaction,
                          recategoriseAll: Boolean? = null, includeApplyAll: Boolean? = null,
                          completion: OnFrolloSDKCompletionListener? = null) {

        val request = TransactionUpdateRequest(
                budgetCategory = transaction.budgetCategory,
                categoryId = transaction.categoryId,
                included = transaction.included,
                memo = transaction.memo,
                userDescription = transaction.description?.user,
                recategoriseAll = recategoriseAll,
                includeApplyAll = includeApplyAll)

        aggregationAPI.updateTransaction(transactionId, request).enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#updateTransaction", error.localizedDescription)
                completion?.invoke(error)
            } else if (response == null) {
                // Explicitly invoke completion callback if response is null.
                completion?.invoke(null)
            } else
                handleTransactionResponse(response, completion)
        }
    }

    private fun handleTransactionsResponse(response: List<TransactionResponse>, fromDate: String? = null, toDate: String? = null,
                                           accountIds: LongArray? = null, transactionIncluded: Boolean? = null,
                                           completion: OnFrolloSDKCompletionListener? = null) {
        doAsync {
            val models = mapTransactionResponse(response)
            db.transactions().insertAll(*models.toTypedArray())

            ifNotNull(fromDate, toDate) { from, to ->
                val apiIds = response.map { it.transactionId }.toList().sorted()
                val staleIds = ArrayList(db.transactions().getIdsQuery(
                        sqlForTransactionStaleIds(fromDate = from, toDate = to,
                                accountIds = accountIds, transactionIncluded = transactionIncluded)).sorted())

                staleIds.removeAll(apiIds)

                if (staleIds.isNotEmpty()) {
                    db.transactions().deleteMany(staleIds.toLongArray())
                }
            }

            uiThread { completion?.invoke(null) }
        }
    }

    private fun handleTransactionResponse(response: TransactionResponse, completion: OnFrolloSDKCompletionListener? = null) {
        doAsync {
            db.transactions().insert(response.toTransaction())

            uiThread { completion?.invoke(null) }
        }
    }

    private fun mapTransactionResponse(models: List<TransactionResponse>): List<Transaction> =
            models.map { it.toTransaction() }.toList()
}