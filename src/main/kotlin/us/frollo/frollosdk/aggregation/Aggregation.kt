package us.frollo.frollosdk.aggregation

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.AggregationAPI
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.extensions.*
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.*
import us.frollo.frollosdk.model.api.aggregation.accounts.AccountResponse
import us.frollo.frollosdk.model.api.aggregation.accounts.AccountUpdateRequest
import us.frollo.frollosdk.model.api.aggregation.merchants.MerchantResponse
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountCreateRequest
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountResponse
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountUpdateRequest
import us.frollo.frollosdk.model.api.aggregation.providers.ProviderResponse
import us.frollo.frollosdk.model.api.aggregation.transactioncategories.TransactionCategoryResponse
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionResponse
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionUpdateRequest
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Account
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountRelation
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountSubType
import us.frollo.frollosdk.model.coredata.aggregation.merchants.Merchant
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccount
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccountRelation
import us.frollo.frollosdk.model.coredata.aggregation.providers.Provider
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderLoginForm
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderRelation
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategory
import us.frollo.frollosdk.model.coredata.aggregation.transactions.Transaction
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionRelation
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionsSummary
import kotlin.collections.ArrayList

/**
 * Manages all aggregation data including accounts, transactions, categories and merchants.
 */
class Aggregation(network: NetworkService, private val db: SDKDatabase) {

    companion object {
        private const val TAG = "Aggregation"
    }

    private val aggregationAPI: AggregationAPI = network.create(AggregationAPI::class.java)

    private var refreshingMerchantIDs = setOf<Long>()
    private var refreshingProviderIDs = setOf<Long>()

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

    fun fetchProviderWithRelation(providerId: Long): LiveData<Resource<ProviderRelation>> =
            Transformations.map(db.providers().loadWithRelation(providerId)) { model ->
                Resource.success(model)
            }

    fun fetchProvidersWithRelation(): LiveData<Resource<List<ProviderRelation>>> =
            Transformations.map(db.providers().loadWithRelation()) { models ->
                Resource.success(models)
            }

    fun refreshProvider(providerId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchProvider(providerId).enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleProviderResponse(resource.data, completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshProvider", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    fun refreshProviders(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchProviders().enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleProvidersResponse(response = resource.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshProviders", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    private fun handleProvidersResponse(response: List<ProviderResponse>?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                val models = mapProviderResponse(response)
                db.providers().insertAll(*models.toTypedArray())

                val apiIds = response.map { it.providerId }.toList()
                val staleIds = db.providers().getStaleIds(apiIds.toLongArray())

                if (staleIds.isNotEmpty()) {
                    removeCachedProviders(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleProviderResponse(response: ProviderResponse?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                db.providers().insert(response.toProvider())

                refreshingProviderIDs = refreshingProviderIDs.minus(response.providerId)

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun mapProviderResponse(models: List<ProviderResponse>): List<Provider> =
            models.map { it.toProvider() }.toList()

    // Provider Account

    fun fetchProviderAccount(providerAccountId: Long): LiveData<Resource<ProviderAccount>> =
            Transformations.map(db.providerAccounts().load(providerAccountId)) { model ->
                Resource.success(model)
            }

    fun fetchProviderAccounts(): LiveData<Resource<List<ProviderAccount>>> =
            Transformations.map(db.providerAccounts().load()) { models ->
                Resource.success(models)
            }

    fun fetchProviderAccountsByProviderId(providerId: Long): LiveData<Resource<List<ProviderAccount>>> =
            Transformations.map(db.providerAccounts().loadByProviderId(providerId)) { models ->
                Resource.success(models)
            }

    fun fetchProviderAccountWithRelation(providerAccountId: Long): LiveData<Resource<ProviderAccountRelation>> =
            Transformations.map(db.providerAccounts().loadWithRelation(providerAccountId)) { model ->
                Resource.success(model)
            }

    fun fetchProviderAccountsWithRelation(): LiveData<Resource<List<ProviderAccountRelation>>> =
            Transformations.map(db.providerAccounts().loadWithRelation()) { models ->
                Resource.success(models)
            }

    fun fetchProviderAccountsByProviderIdWithRelation(providerId: Long): LiveData<Resource<List<ProviderAccountRelation>>> =
            Transformations.map(db.providerAccounts().loadByProviderIdWithRelation(providerId)) { models ->
                Resource.success(models)
            }

    fun refreshProviderAccount(providerAccountId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchProviderAccount(providerAccountId).enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleProviderAccountResponse(response = resource.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshProviderAccount", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    fun refreshProviderAccounts(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchProviderAccounts().enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleProviderAccountsResponse(response = resource.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshProviderAccounts", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    fun createProviderAccount(providerId: Long, loginForm: ProviderLoginForm, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        val request = ProviderAccountCreateRequest(loginForm = loginForm, providerID = providerId)

        aggregationAPI.createProviderAccount(request).enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleProviderAccountResponse(response = resource.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#createProviderAccount", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    fun deleteProviderAccount(providerAccountId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.deleteProviderAccount(providerAccountId).enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    removeCachedProviderAccounts(longArrayOf(providerAccountId))
                    completion?.invoke(Result.success())
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#deleteProviderAccount", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    fun updateProviderAccount(providerAccountId: Long, loginForm: ProviderLoginForm, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        val request = ProviderAccountUpdateRequest(loginForm = loginForm)

        aggregationAPI.updateProviderAccount(providerAccountId, request).enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleProviderAccountResponse(response = resource.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateProviderAccount", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    private fun handleProviderAccountsResponse(response: List<ProviderAccountResponse>?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                fetchMissingProviders(response.map { it.providerId }.toSet())

                val models = mapProviderAccountResponse(response)
                db.providerAccounts().insertAll(*models.toTypedArray())

                val apiIds = response.map { it.providerAccountId }.toList()
                val staleIds = db.providerAccounts().getStaleIds(apiIds.toLongArray())

                if (staleIds.isNotEmpty()) {
                    removeCachedProviderAccounts(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleProviderAccountResponse(response: ProviderAccountResponse?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                fetchMissingProviders(setOf(response.providerId))

                db.providerAccounts().insert(response.toProviderAccount())

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun mapProviderAccountResponse(models: List<ProviderAccountResponse>): List<ProviderAccount> =
            models.map { it.toProviderAccount() }.toList()

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

    fun fetchAccountWithRelation(accountId: Long): LiveData<Resource<AccountRelation>> =
            Transformations.map(db.accounts().loadWithRelation(accountId)) { model ->
                Resource.success(model)
            }

    fun fetchAccountsWithRelation(): LiveData<Resource<List<AccountRelation>>> =
            Transformations.map(db.accounts().loadWithRelation()) { models ->
                Resource.success(models)
            }

    fun fetchAccountsByProviderAccountIdWithRelation(providerAccountId: Long): LiveData<Resource<List<AccountRelation>>> =
            Transformations.map(db.accounts().loadByProviderAccountIdWithRelation(providerAccountId)) { models ->
                Resource.success(models)
            }

    fun refreshAccount(accountId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchAccount(accountId).enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleAccountResponse(response = resource.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshAccount", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    fun refreshAccounts(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchAccounts().enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleAccountsResponse(response = resource.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshAccounts", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    fun updateAccount(accountId: Long, hidden: Boolean, included: Boolean, favourite: Boolean? = null,
                      accountSubType: AccountSubType? = null, nickName: String? = null,
                      completion: OnFrolloSDKCompletionListener<Result>? = null) {

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

        aggregationAPI.updateAccount(accountId, request).enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleAccountResponse(response = resource.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateAccount", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    private fun handleAccountsResponse(response: List<AccountResponse>?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                val models = mapAccountResponse(response)
                db.accounts().insertAll(*models.toTypedArray())

                val apiIds = response.map { it.accountId }.toList()
                val staleIds = db.accounts().getStaleIds(apiIds.toLongArray())

                if (staleIds.isNotEmpty()) {
                    removeCachedAccounts(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleAccountResponse(response: AccountResponse?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
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

    fun fetchTransactionWithRelation(transactionId: Long): LiveData<Resource<TransactionRelation>> =
            Transformations.map(db.transactions().loadWithRelation(transactionId)) { model ->
                Resource.success(model)
            }

    fun fetchTransactionsWithRelation(transactionIds: LongArray? = null): LiveData<Resource<List<TransactionRelation>>> {
        val result = if (transactionIds != null) db.transactions().loadWithRelation(transactionIds)
        else db.transactions().loadWithRelation()

        return Transformations.map(result) { models ->
            Resource.success(models)
        }
    }

    fun fetchTransactionsByAccountIdWithRelation(accountId: Long): LiveData<Resource<List<TransactionRelation>>> =
            Transformations.map(db.transactions().loadByAccountIdWithRelation(accountId)) { models ->
                Resource.success(models)
            }

    fun refreshTransaction(transactionId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchTransaction(transactionId).enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleTransactionResponse(response = resource.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshTransaction", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    fun refreshTransactions(fromDate: String, toDate: String, accountIds: LongArray? = null,
                            transactionIncluded: Boolean? = null, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchTransactionsByQuery(fromDate = fromDate, toDate = toDate,
                accountIds = accountIds, transactionIncluded = transactionIncluded).enqueue { resource ->

            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleTransactionsResponse(response = resource.data, fromDate = fromDate, toDate = toDate,
                            accountIds = accountIds, transactionIncluded = transactionIncluded, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshTransactions", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    fun refreshTransactions(transactionIds: LongArray, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchTransactionsByIDs(transactionIds).enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleTransactionsResponse(response = resource.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshTransactionsByIDs", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    fun updateTransaction(transactionId: Long, transaction: Transaction,
                          recategoriseAll: Boolean? = null, includeApplyAll: Boolean? = null,
                          completion: OnFrolloSDKCompletionListener<Result>? = null) {

        val request = TransactionUpdateRequest(
                budgetCategory = transaction.budgetCategory,
                categoryId = transaction.categoryId,
                included = transaction.included,
                memo = transaction.memo,
                userDescription = transaction.description?.user,
                recategoriseAll = recategoriseAll,
                includeApplyAll = includeApplyAll)

        aggregationAPI.updateTransaction(transactionId, request).enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleTransactionResponse(response = resource.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateTransaction", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    fun fetchTransactionsSummary(fromDate: String, toDate: String, accountIds: LongArray? = null, transactionIncluded: Boolean? = null,
                                 completion: OnFrolloSDKCompletionListener<Resource<TransactionsSummary>>) {
        aggregationAPI.fetchTransactionsSummaryByQuery(fromDate = fromDate, toDate = toDate,
                accountIds = accountIds, transactionIncluded = transactionIncluded).enqueue { resource ->

            if (resource.status == Resource.Status.ERROR)
                Log.e("$TAG#fetchTransactionsSummary", resource.error?.localizedDescription)

            completion.invoke(resource.map { it?.toTransactionsSummary() })
        }
    }

    fun fetchTransactionsSummary(transactionIds: LongArray, completion: OnFrolloSDKCompletionListener<Resource<TransactionsSummary>>) {
        aggregationAPI.fetchTransactionsSummaryByIDs(transactionIds).enqueue { resource ->

            if (resource.status == Resource.Status.ERROR)
                Log.e("$TAG#fetchTransactionsSummaryByIDs", resource.error?.localizedDescription)

            completion.invoke(resource.map { it?.toTransactionsSummary() })
        }
    }

    private fun handleTransactionsResponse(response: List<TransactionResponse>?, fromDate: String? = null, toDate: String? = null,
                                           accountIds: LongArray? = null, transactionIncluded: Boolean? = null,
                                           completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                fetchMissingMerchants(response.map { it.merchantId }.toSet())

                val models = mapTransactionResponse(response)
                db.transactions().insertAll(*models.toTypedArray())

                ifNotNull(fromDate, toDate) { from, to ->
                    val apiIds = response.map { it.transactionId }.toList().sorted()
                    val staleIds = ArrayList(db.transactions().getIdsQuery(
                            sqlForTransactionStaleIds(fromDate = from, toDate = to,
                                    accountIds = accountIds, transactionIncluded = transactionIncluded)).sorted())

                    staleIds.removeAll(apiIds)

                    if (staleIds.isNotEmpty()) {
                        removeCachedTransactions(staleIds.toLongArray())
                    }
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleTransactionResponse(response: TransactionResponse?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                fetchMissingMerchants(setOf(response.merchantId))

                db.transactions().insert(response.toTransaction())

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun mapTransactionResponse(models: List<TransactionResponse>): List<Transaction> =
            models.map { it.toTransaction() }.toList()

    // Transaction Category

    fun fetchTransactionCategory(transactionCategoryId: Long): LiveData<Resource<TransactionCategory>> =
            Transformations.map(db.transactionCategories().load(transactionCategoryId)) { model ->
                Resource.success(model)
            }

    fun fetchTransactionCategories(): LiveData<Resource<List<TransactionCategory>>> =
            Transformations.map(db.transactionCategories().load()) { models ->
                Resource.success(models)
            }

    fun refreshTransactionCategories(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchTransactionCategories().enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleTransactionCategoriesResponse(response = resource.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshTransactionCategories", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    private fun handleTransactionCategoriesResponse(response: List<TransactionCategoryResponse>?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                val models = mapTransactionCategoryResponse(response)
                db.transactionCategories().insertAll(*models.toTypedArray())

                val apiIds = response.map { it.transactionCategoryId }.toList()
                val staleIds = db.transactionCategories().getStaleIds(apiIds.toLongArray())

                if (staleIds.isNotEmpty()) {
                    db.transactionCategories().deleteMany(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun mapTransactionCategoryResponse(models: List<TransactionCategoryResponse>): List<TransactionCategory> =
            models.map { it.toTransactionCategory() }.toList()

    // Merchant

    fun fetchMerchant(merchantId: Long): LiveData<Resource<Merchant>> =
            Transformations.map(db.merchants().load(merchantId)) { model ->
                Resource.success(model)
            }

    fun fetchMerchants(): LiveData<Resource<List<Merchant>>> =
            Transformations.map(db.merchants().load()) { models ->
                Resource.success(models)
            }

    fun refreshMerchant(merchantId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchMerchant(merchantId).enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleMerchantResponse(response = resource.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshMerchant", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    fun refreshMerchants(merchantIds: LongArray, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchMerchantsByIDs(merchantIds).enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleMerchantsResponse(response = resource.data, byIds = true, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshMerchantsByIDs", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    internal fun refreshMerchants(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchMerchants().enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleMerchantsResponse(response = resource.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshMerchants", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    private fun handleMerchantResponse(response: MerchantResponse?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                db.merchants().insert(response.toMerchant())

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleMerchantsResponse(response: List<MerchantResponse>?, byIds: Boolean = false, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                val models = mapMerchantResponse(response)
                db.merchants().insertAll(*models.toTypedArray())

                val apiIds = response.map { it.merchantId }.toList()
                refreshingMerchantIDs = refreshingMerchantIDs.minus(apiIds)

                if (!byIds) {
                    val staleIds = db.merchants().getStaleIds(apiIds.toLongArray())

                    if (staleIds.isNotEmpty()) {
                        db.merchants().deleteMany(staleIds.toLongArray())
                    }
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun mapMerchantResponse(models: List<MerchantResponse>): List<Merchant> =
            models.map { it.toMerchant() }.toList()

    ////////

    private fun fetchMissingProviders(providerIds: Set<Long>) {
        doAsync {
            val existingProviderIds = db.providers().getIds().toSet()
            val missingProviderIds = providerIds.compareToFindMissingItems(existingProviderIds).minus(refreshingProviderIDs)
            if (missingProviderIds.isNotEmpty()) {
                refreshingProviderIDs = refreshingProviderIDs.plus(missingProviderIds)
                missingProviderIds.forEach {
                    refreshProvider(it)
                }
            }
        }
    }

    private fun fetchMissingMerchants(merchantIds: Set<Long>) {
        doAsync {
            val existingMerchantIds = db.merchants().getIds().toSet()
            val missingMerchantIds = merchantIds.compareToFindMissingItems(existingMerchantIds).minus(refreshingMerchantIDs)
            if (missingMerchantIds.isNotEmpty()) {
                refreshingMerchantIDs = refreshingMerchantIDs.plus(missingMerchantIds)
                refreshMerchants(missingMerchantIds.toLongArray())
            }
        }
    }

    // WARNING: Do not call this method on the main thread
    private fun removeCachedProviders(providerIds: LongArray) {
        db.providers().deleteMany(providerIds)

        // Manually delete other data linked to this provider
        // as we are not using ForeignKeys because ForeignKey constraints
        // do not allow to insert data into child table prior to parent table
        val providerAccountIds = db.providerAccounts().getIdsByProviderIds(providerIds)
        removeCachedProviderAccounts(providerAccountIds)
    }

    // WARNING: Do not call this method on the main thread
    private fun removeCachedProviderAccounts(providerAccountIds: LongArray) {
        db.providerAccounts().deleteMany(providerAccountIds)

        // Manually delete other data linked to this provider account
        // as we are not using ForeignKeys because ForeignKey constraints
        // do not allow to insert data into child table prior to parent table
        val accountIds = db.accounts().getIdsByProviderAccountIds(providerAccountIds)
        removeCachedAccounts(accountIds)
    }

    // WARNING: Do not call this method on the main thread
    private fun removeCachedAccounts(accountIds: LongArray) {
        db.accounts().deleteMany(accountIds)

        // Manually delete other data linked to this account
        // as we are not using ForeignKeys because ForeignKey constraints
        // do not allow to insert data into child table prior to parent table
        val transactionIds = db.transactions().getIdsByAccountIds(accountIds)
        removeCachedTransactions(transactionIds)
    }

    // WARNING: Do not call this method on the main thread
    private fun removeCachedTransactions(transactionIds: LongArray) {
        db.transactions().deleteMany(transactionIds)
    }
}