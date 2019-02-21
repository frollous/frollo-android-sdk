package us.frollo.frollosdk.aggregation

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
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
            }

    fun fetchProviders(): LiveData<Resource<List<Provider>>> =
            Transformations.map(db.providers().load()) { models ->
                Resource.success(models)
            }

    fun refreshProvider(providerId: Long, completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.fetchProvider(providerId).enqueue { result ->
            when(result.status) {
                Resource.Status.SUCCESS -> {
                    handleProviderResponse(result.data, completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshProvider", result.error?.localizedDescription)
                    completion?.invoke(Result.error(result.error))
                }
            }
        }
    }

    fun refreshProviders(completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.fetchProviders().enqueue { result ->
            when(result.status) {
                Resource.Status.SUCCESS -> {
                    handleProvidersResponse(response = result.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshProviders", result.error?.localizedDescription)
                    completion?.invoke(Result.error(result.error))
                }
            }
        }
    }

    private fun handleProvidersResponse(response: List<ProviderResponse>?, completion: OnFrolloSDKCompletionListener? = null) {
        response?.let {
            doAsync {
                val models = mapProviderResponse(response)
                db.providers().insertAll(*models.toTypedArray())

                val apiIds = response.map { it.providerId }.toList()
                val staleIds = db.providers().getStaleIds(apiIds.toLongArray())

                if (staleIds.isNotEmpty()) {
                    db.providers().deleteMany(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleProviderResponse(response: ProviderResponse?, completion: OnFrolloSDKCompletionListener? = null) {
        response?.let {
            doAsync {
                db.providers().insert(response.toProvider())

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun mapProviderResponse(models: List<ProviderResponse>): List<Provider> =
            models.map { it.toProvider() }.toList()

    // Provider Account

    fun fetchProviderAccount(providerAccountId: Long): LiveData<Resource<ProviderAccount>> =
            Transformations.map(db.provideraccounts().load(providerAccountId)) { model ->
                Resource.success(model)
            }

    fun fetchProviderAccounts(): LiveData<Resource<List<ProviderAccount>>> =
            Transformations.map(db.provideraccounts().load()) { models ->
                Resource.success(models)
            }

    fun fetchProviderAccountsByProviderId(providerId: Long): LiveData<Resource<List<ProviderAccount>>> =
            Transformations.map(db.provideraccounts().loadByProviderId(providerId)) { models ->
                Resource.success(models)
            }

    fun refreshProviderAccount(providerAccountId: Long, completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.fetchProviderAccount(providerAccountId).enqueue { result ->
            when(result.status) {
                Resource.Status.SUCCESS -> {
                    handleProviderAccountResponse(response = result.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshProviderAccount", result.error?.localizedDescription)
                    completion?.invoke(Result.error(result.error))
                }
            }
        }
    }

    fun refreshProviderAccounts(completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.fetchProviderAccounts().enqueue { result ->
            when(result.status) {
                Resource.Status.SUCCESS -> {
                    handleProviderAccountsResponse(response = result.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshProviderAccounts", result.error?.localizedDescription)
                    completion?.invoke(Result.error(result.error))
                }
            }
        }
    }

    fun createProviderAccount(providerId: Long, loginForm: ProviderLoginForm, completion: OnFrolloSDKCompletionListener? = null) {
        val request = ProviderAccountCreateRequest(loginForm = loginForm, providerID = providerId)

        aggregationAPI.createProviderAccount(request).enqueue { result ->
            when(result.status) {
                Resource.Status.SUCCESS -> {
                    handleProviderAccountResponse(response = result.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#createProviderAccount", result.error?.localizedDescription)
                    completion?.invoke(Result.error(result.error))
                }
            }
        }
    }

    fun deleteProviderAccount(providerAccountId: Long, completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.deleteProviderAccount(providerAccountId).enqueue { result ->
            when(result.status) {
                Resource.Status.SUCCESS -> {
                    // Manually delete other data linked to this provider account
                    // as we are not using ForeignKeys because ForeignKey constraints
                    // do not allow to insert data into child table prior to parent table
                    //TODO: Manually delete other data linked to this provider account
                    removeCachedProviderAccount(providerAccountId)
                    completion?.invoke(Result.success())
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#deleteProviderAccount", result.error?.localizedDescription)
                    completion?.invoke(Result.error(result.error))
                }
            }
        }
    }

    fun updateProviderAccount(providerAccountId: Long, loginForm: ProviderLoginForm, completion: OnFrolloSDKCompletionListener? = null) {
        val request = ProviderAccountUpdateRequest(loginForm = loginForm)

        aggregationAPI.updateProviderAccount(providerAccountId, request).enqueue { result ->
            when(result.status) {
                Resource.Status.SUCCESS -> {
                    handleProviderAccountResponse(response = result.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateProviderAccount", result.error?.localizedDescription)
                    completion?.invoke(Result.error(result.error))
                }
            }
        }
    }

    private fun handleProviderAccountsResponse(response: List<ProviderAccountResponse>?, completion: OnFrolloSDKCompletionListener? = null) {
        response?.let {
            doAsync {
                val models = mapProviderAccountResponse(response)
                db.provideraccounts().insertAll(*models.toTypedArray())

                val apiIds = response.map { it.providerAccountId }.toList()
                val staleIds = db.provideraccounts().getStaleIds(apiIds.toLongArray())

                if (staleIds.isNotEmpty()) {
                    db.provideraccounts().deleteMany(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleProviderAccountResponse(response: ProviderAccountResponse?, completion: OnFrolloSDKCompletionListener? = null) {
        response?.let {
            doAsync {
                db.provideraccounts().insert(response.toProviderAccount())

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
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
            }

    fun fetchAccounts(): LiveData<Resource<List<Account>>> =
            Transformations.map(db.accounts().load()) { models ->
                Resource.success(models)
            }

    fun fetchAccountsByProviderAccountId(providerAccountId: Long): LiveData<Resource<List<Account>>> =
            Transformations.map(db.accounts().loadByProviderAccountId(providerAccountId)) { models ->
                Resource.success(models)
            }

    fun refreshAccount(accountId: Long, completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.fetchAccount(accountId).enqueue { result ->
            when(result.status) {
                Resource.Status.SUCCESS -> {
                    handleAccountResponse(response = result.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshAccount", result.error?.localizedDescription)
                    completion?.invoke(Result.error(result.error))
                }
            }
        }
    }

    fun refreshAccounts(completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.fetchAccounts().enqueue { result ->
            when(result.status) {
                Resource.Status.SUCCESS -> {
                    handleAccountsResponse(response = result.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshAccounts", result.error?.localizedDescription)
                    completion?.invoke(Result.error(result.error))
                }
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
            val error = DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)
            completion?.invoke(Result.error(error))
            return
        }

        aggregationAPI.updateAccount(accountId, request).enqueue { result ->
            when(result.status) {
                Resource.Status.SUCCESS -> {
                    handleAccountResponse(response = result.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateAccount", result.error?.localizedDescription)
                    completion?.invoke(Result.error(result.error))
                }
            }
        }
    }

    private fun handleAccountsResponse(response: List<AccountResponse>?, completion: OnFrolloSDKCompletionListener? = null) {
        response?.let {
            doAsync {
                val models = mapAccountResponse(response)
                db.accounts().insertAll(*models.toTypedArray())

                val apiIds = response.map { it.accountId }.toList()
                val staleIds = db.accounts().getStaleIds(apiIds.toLongArray())

                if (staleIds.isNotEmpty()) {
                    db.accounts().deleteMany(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleAccountResponse(response: AccountResponse?, completion: OnFrolloSDKCompletionListener? = null) {
        response?.let {
            doAsync {
                db.accounts().insert(response.toAccount())

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun mapAccountResponse(models: List<AccountResponse>): List<Account> =
            models.map { it.toAccount() }.toList()

    // Transaction

    fun fetchTransaction(transactionId: Long): LiveData<Resource<Transaction>> =
            Transformations.map(db.transactions().load(transactionId)) { model ->
                Resource.success(model)
            }

    fun fetchTransactions(transactionIds: LongArray? = null): LiveData<Resource<List<Transaction>>> {
        val result = if (transactionIds != null) db.transactions().load(transactionIds)
                     else db.transactions().load()

        return Transformations.map(result) { models ->
            Resource.success(models)
        }
    }

    fun fetchTransactionsByAccountId(accountId: Long): LiveData<Resource<List<Transaction>>> =
            Transformations.map(db.transactions().loadByAccountId(accountId)) { models ->
                Resource.success(models)
            }

    fun refreshTransaction(transactionId: Long, completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.fetchTransaction(transactionId).enqueue { result ->
            when(result.status) {
                Resource.Status.SUCCESS -> {
                    handleTransactionResponse(response = result.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshTransaction", result.error?.localizedDescription)
                    completion?.invoke(Result.error(result.error))
                }
            }
        }
    }

    fun refreshTransactions(fromDate: String, toDate: String, accountIds: LongArray? = null,
                            transactionIncluded: Boolean? = null, completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.fetchTransactionsByQuery(fromDate = fromDate, toDate = toDate,
                accountIds = accountIds, transactionIncluded = transactionIncluded).enqueue { result ->

            when(result.status) {
                Resource.Status.SUCCESS -> {
                    handleTransactionsResponse(response = result.data, fromDate = fromDate, toDate = toDate,
                            accountIds = accountIds, transactionIncluded = transactionIncluded, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshTransactions", result.error?.localizedDescription)
                    completion?.invoke(Result.error(result.error))
                }
            }
        }
    }

    fun refreshTransactions(transactionIds: LongArray, completion: OnFrolloSDKCompletionListener? = null) {
        aggregationAPI.fetchTransactionsByIDs(transactionIds).enqueue { result ->
            when(result.status) {
                Resource.Status.SUCCESS -> {
                    handleTransactionsResponse(response = result.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshTransactionsByIDs", result.error?.localizedDescription)
                    completion?.invoke(Result.error(result.error))
                }
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

        aggregationAPI.updateTransaction(transactionId, request).enqueue { result ->
            when(result.status) {
                Resource.Status.SUCCESS -> {
                    handleTransactionResponse(response = result.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateTransaction", result.error?.localizedDescription)
                    completion?.invoke(Result.error(result.error))
                }
            }
        }
    }

    private fun handleTransactionsResponse(response: List<TransactionResponse>?, fromDate: String? = null, toDate: String? = null,
                                           accountIds: LongArray? = null, transactionIncluded: Boolean? = null,
                                           completion: OnFrolloSDKCompletionListener? = null) {
        response?.let {
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

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleTransactionResponse(response: TransactionResponse?, completion: OnFrolloSDKCompletionListener? = null) {
        response?.let {
            doAsync {
                db.transactions().insert(response.toTransaction())

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun mapTransactionResponse(models: List<TransactionResponse>): List<Transaction> =
            models.map { it.toTransaction() }.toList()
}