/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.frollo.frollosdk.aggregation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.sqlite.db.SimpleSQLiteQuery
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.ACTION.ACTION_REFRESH_TRANSACTIONS
import us.frollo.frollosdk.core.ARGUMENT.ARG_DATA
import us.frollo.frollosdk.core.ARGUMENT.ARG_TRANSACTION_IDS
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
import us.frollo.frollosdk.model.api.aggregation.tags.TransactionTagResponse
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
import us.frollo.frollosdk.model.coredata.shared.OrderType
import us.frollo.frollosdk.model.coredata.aggregation.tags.TagsSortType
import us.frollo.frollosdk.model.coredata.aggregation.tags.TransactionTag
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategory
import us.frollo.frollosdk.model.coredata.aggregation.transactions.Transaction
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionDescription
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionRelation
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionsSummary
import kotlin.collections.ArrayList

/**
 * Manages all aggregation data including accounts, transactions, categories and merchants.
 */
class Aggregation(network: NetworkService, private val db: SDKDatabase, localBroadcastManager: LocalBroadcastManager) {

    companion object {
        private const val TAG = "Aggregation"
        private const val TRANSACTION_BATCH_SIZE = 200
    }

    private val aggregationAPI: AggregationAPI = network.create(AggregationAPI::class.java)

    private var refreshingMerchantIDs = setOf<Long>()
    private var refreshingProviderIDs = setOf<Long>()

    private val refreshTransactionsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val transactionIds = intent.getBundleExtra(ARG_DATA).getLongArray(ARG_TRANSACTION_IDS)
            transactionIds?.let { refreshTransactions(it) }
        }
    }

    init {
        localBroadcastManager.registerReceiver(refreshTransactionsReceiver,
                IntentFilter(ACTION_REFRESH_TRANSACTIONS))
    }

    // Provider

    /**
     * Fetch provider by ID from the cache
     *
     * @param providerId Unique provider ID to fetch
     *
     * @return LiveData object of Resource<Provider> which can be observed using an Observer for future changes as well.
     */
    fun fetchProvider(providerId: Long): LiveData<Resource<Provider>> =
            Transformations.map(db.providers().load(providerId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch providers from the cache
     *
     * @return LiveData object of Resource<List<Provider>> which can be observed using an Observer for future changes as well.
     */
    fun fetchProviders(): LiveData<Resource<List<Provider>>> =
            Transformations.map(db.providers().load()) { models ->
                Resource.success(models)
            }

    /**
     * Fetch provider by ID from the cache along with other associated data.
     *
     * @param providerId Unique provider ID to fetch
     *
     * @return LiveData object of Resource<ProviderRelation> which can be observed using an Observer for future changes as well.
     */
    fun fetchProviderWithRelation(providerId: Long): LiveData<Resource<ProviderRelation>> =
            Transformations.map(db.providers().loadWithRelation(providerId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch providers from the cache along with other associated data.
     *
     * @return LiveData object of Resource<List<ProviderRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchProvidersWithRelation(): LiveData<Resource<List<ProviderRelation>>> =
            Transformations.map(db.providers().loadWithRelation()) { models ->
                Resource.success(models)
            }

    /**
     * Refresh a specific provider by ID from the host
     *
     * @param providerId ID of the provider to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
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

    /**
     * Refresh all available providers from the host.
     *
     * Includes beta and supported providers. Unsupported and Disabled providers must be fetched by ID.
     *
     * @param completion Optional completion handler with optional error if the request fails
     */
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

    /**
     * Fetch provider account by ID from the cache
     *
     * @param providerAccountId Unique ID of the provider account to fetch
     *
     * @return LiveData object of Resource<ProviderAccount> which can be observed using an Observer for future changes as well.
     */
    fun fetchProviderAccount(providerAccountId: Long): LiveData<Resource<ProviderAccount>> =
            Transformations.map(db.providerAccounts().load(providerAccountId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch provider accounts from the cache
     *
     * @return LiveData object of Resource<List<ProviderAccount>> which can be observed using an Observer for future changes as well.
     */
    fun fetchProviderAccounts(): LiveData<Resource<List<ProviderAccount>>> =
            Transformations.map(db.providerAccounts().load()) { models ->
                Resource.success(models)
            }

    /**
     * Fetch provider accounts by provider ID from the cache
     *
     * @param providerId Provider ID of the provider accounts to fetch
     *
     * @return LiveData object of Resource<List<ProviderAccount>> which can be observed using an Observer for future changes as well.
     */
    fun fetchProviderAccountsByProviderId(providerId: Long): LiveData<Resource<List<ProviderAccount>>> =
            Transformations.map(db.providerAccounts().loadByProviderId(providerId)) { models ->
                Resource.success(models)
            }

    /**
     * Fetch provider account by ID from the cache along with other associated data.
     *
     * @param providerAccountId Unique provider account ID to fetch
     *
     * @return LiveData object of Resource<ProviderAccountRelation> which can be observed using an Observer for future changes as well.
     */
    fun fetchProviderAccountWithRelation(providerAccountId: Long): LiveData<Resource<ProviderAccountRelation>> =
            Transformations.map(db.providerAccounts().loadWithRelation(providerAccountId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch provider accounts from the cache along with other associated data.
     *
     * @return LiveData object of Resource<List<ProviderAccountRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchProviderAccountsWithRelation(): LiveData<Resource<List<ProviderAccountRelation>>> =
            Transformations.map(db.providerAccounts().loadWithRelation()) { models ->
                Resource.success(models)
            }

    /**
     * Fetch provider accounts by provider ID from the cache along with other associated data.
     *
     * @param providerId Provider ID of the provider accounts to fetch
     *
     * @return LiveData object of Resource<List<ProviderAccountRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchProviderAccountsByProviderIdWithRelation(providerId: Long): LiveData<Resource<List<ProviderAccountRelation>>> =
            Transformations.map(db.providerAccounts().loadByProviderIdWithRelation(providerId)) { models ->
                Resource.success(models)
            }

    /**
     * Refresh a specific provider account by ID from the host
     *
     * @param providerAccountId ID of the provider account to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
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

    /**
     * Refresh all provider accounts from the host
     *
     * @param completion Optional completion handler with optional error if the request fails
     */
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

    /**
     * Create a provider account
     *
     * @param providerId ID of the provider which an account should be created for
     * @param loginForm Provider login form with validated and encrypted values with the user's details
     * @param completion Optional completion handler with optional error if the request fails
     */
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

    /**
     * Delete a provider account from the host
     *
     * @param providerAccountId ID of the provider account to be deleted
     * @param completion Optional completion handler with optional error if the request fails
     */
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

    /**
     * Update a provider account on the host
     *
     * @param providerAccountId ID of the provider account to be updated
     * @param loginForm Provider account login form with validated and encrypted values with the user's details
     * @param completion Optional completion handler with optional error if the request fails
     */
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

    /**
     * Fetch account by ID from the cache
     *
     * @param accountId Unique ID of the account to fetch
     *
     * @return LiveData object of Resource<Account> which can be observed using an Observer for future changes as well.
     */
    fun fetchAccount(accountId: Long): LiveData<Resource<Account>> =
            Transformations.map(db.accounts().load(accountId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch accounts from the cache
     *
     * @return LiveData object of Resource<List<Account>> which can be observed using an Observer for future changes as well.
     */
    fun fetchAccounts(): LiveData<Resource<List<Account>>> =
            Transformations.map(db.accounts().load()) { models ->
                Resource.success(models)
            }

    /**
     * Fetch accounts by provider account ID from the cache
     *
     * @param providerAccountId Provider account ID of the accounts to fetch
     *
     * @return LiveData object of Resource<List<Account>> which can be observed using an Observer for future changes as well.
     */
    fun fetchAccountsByProviderAccountId(providerAccountId: Long): LiveData<Resource<List<Account>>> =
            Transformations.map(db.accounts().loadByProviderAccountId(providerAccountId)) { models ->
                Resource.success(models)
            }

    /**
     * Fetch account by ID from the cache along with other associated data.
     *
     * @param accountId Unique provider account ID to fetch
     *
     * @return LiveData object of Resource<AccountRelation> which can be observed using an Observer for future changes as well.
     */
    fun fetchAccountWithRelation(accountId: Long): LiveData<Resource<AccountRelation>> =
            Transformations.map(db.accounts().loadWithRelation(accountId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch accounts from the cache along with other associated data.
     *
     * @return LiveData object of Resource<List<AccountRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchAccountsWithRelation(): LiveData<Resource<List<AccountRelation>>> =
            Transformations.map(db.accounts().loadWithRelation()) { models ->
                Resource.success(models)
            }

    /**
     * Fetch accounts by provider account ID from the cache along with other associated data.
     *
     * @param providerAccountId Provider account ID of the accounts to fetch
     *
     * @return LiveData object of Resource<List<AccountRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchAccountsByProviderAccountIdWithRelation(providerAccountId: Long): LiveData<Resource<List<AccountRelation>>> =
            Transformations.map(db.accounts().loadByProviderAccountIdWithRelation(providerAccountId)) { models ->
                Resource.success(models)
            }

    /**
     * Refresh a specific account by ID from the host
     *
     * @param accountId ID of the account to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
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

    /**
     * Refresh all accounts from the host
     *
     * @param completion Optional completion handler with optional error if the request fails
     */
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

    /**
     * Update an account on the host
     *
     * @param accountId ID of the account to be updated
     * @param hidden Used to hide the account in the UI
     * @param included Used to exclude accounts from counting towards the user's budgets
     * @param favourite Mark the account as favourite for UI purposes (optional)
     * @param accountSubType Sub type of the account indicating more detail what the account is (optional)
     * @param nickName Nickname given to the account for display and identification purposes (optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
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

    /**
     * Fetch transaction by ID from the cache
     *
     * @param transactionId Unique ID of the transaction to fetch
     *
     * @return LiveData object of Resource<Transaction> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransaction(transactionId: Long): LiveData<Resource<Transaction>> =
            Transformations.map(db.transactions().load(transactionId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch transactions by user's tags
     *
     * @param userTags list of tags that are linked to a transaction
     *
     * @return LiveData object of LiveData<Resource<List<Transaction>>> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactionsByTags(userTags: List<String>): LiveData<Resource<List<Transaction>>> =
            Transformations.map(db.transactions().loadByQuery(sqlForTransactionByUserTags(userTags))) { models ->
                Resource.success(models)
            }

    /**
     * Fetch transactions by user's tags
     *
     * @param userTags list of tags that are linked to a transaction
     *
     * @return LiveData object of LiveData<Resource<List<TransactionRelation>>> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactionsByTagsWithRelation(userTags: List<String>): LiveData<Resource<List<TransactionRelation>>> =
            Transformations.map(db.transactions().loadByQueryWithRelation(sqlForTransactionByUserTags(userTags))) { models ->
                Resource.success(models)
            }

    /**
     * Fetch transactions from the cache
     *
     * @param transactionIds Unique list of IDs of the transactions to fetch (optional). If not specified this method returns all transactions from cache.
     *
     * @return LiveData object of Resource<List<Transaction>> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactions(transactionIds: LongArray? = null): LiveData<Resource<List<Transaction>>> {
        val result = if (transactionIds != null) db.transactions().load(transactionIds)
                     else db.transactions().load()

        return Transformations.map(result) { models ->
            Resource.success(models)
        }
    }

    /**
     * Advanced method to fetch transactions by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches transactions from the cache
     *
     * @return LiveData object of Resource<List<Transaction>> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactions(query: SimpleSQLiteQuery): LiveData<Resource<List<Transaction>>> =
        Transformations.map(db.transactions().loadByQuery(query)) { model ->
            Resource.success(model)
        }

    /**
     * Fetch transactions by account ID from the cache.
     *
     * @param accountId Account ID of the transactions to fetch
     *
     * @return LiveData object of Resource<List<Transaction>> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactionsByAccountId(accountId: Long): LiveData<Resource<List<Transaction>>> =
            Transformations.map(db.transactions().loadByAccountId(accountId)) { models ->
                Resource.success(models)
            }

    /**
     * Fetch transaction by ID from the cache along with other associated data.
     *
     * @param transactionId Unique transaction ID to fetch
     *
     * @return LiveData object of Resource<TransactionRelation> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactionWithRelation(transactionId: Long): LiveData<Resource<TransactionRelation>> =
            Transformations.map(db.transactions().loadWithRelation(transactionId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch transactions from the cache along with other associated data.
     *
     * @param transactionIds Unique list of IDs of the transactions to fetch (optional). If not specified this method returns all transactions from cache.
     *
     * @return LiveData object of Resource<List<TransactionRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactionsWithRelation(transactionIds: LongArray? = null): LiveData<Resource<List<TransactionRelation>>> {
        val result = if (transactionIds != null) db.transactions().loadWithRelation(transactionIds)
        else db.transactions().loadWithRelation()

        return Transformations.map(result) { models ->
            Resource.success(models)
        }
    }

    /**
     * Advanced method to fetch transactions by SQL query from the cache with other associated data.
     *
     * @param query SimpleSQLiteQuery: Select query which fetches transactions from the cache
     *
     * @return LiveData object of Resource<List<TransactionRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactionsWithRelation(query: SimpleSQLiteQuery): LiveData<Resource<List<TransactionRelation>>> =
            Transformations.map(db.transactions().loadByQueryWithRelation(query)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch transactions by account ID from the cache with other associated data.
     *
     * @param accountId Account ID of the transactions to fetch
     *
     * @return LiveData object of Resource<List<TransactionRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactionsByAccountIdWithRelation(accountId: Long): LiveData<Resource<List<TransactionRelation>>> =
            Transformations.map(db.transactions().loadByAccountIdWithRelation(accountId)) { models ->
                Resource.success(models)
            }

    /**
     * Refresh a specific transaction by ID from the host
     *
     * @param transactionId ID of the transaction to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
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

    /**
     * Refresh transactions from a certain period from the host
     *
     * @param fromDate Start date to fetch transactions from (inclusive). Please use [Transaction.DATE_FORMAT_PATTERN] for the format pattern.
     * @param toDate End date to fetch transactions up to (inclusive). Please use [Transaction.DATE_FORMAT_PATTERN] for the format pattern.
     * @param accountIds Specific account IDs of the transactions to fetch (optional)
     * @param transactionIncluded Boolean flag to indicate to fetch only those transactions that are excluded/included in budget (optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshTransactions(fromDate: String, toDate: String, accountIds: LongArray? = null,
                            transactionIncluded: Boolean? = null, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        doAsync {
            refreshNextTransactions(fromDate, toDate, accountIds, transactionIncluded, 0, longArrayOf(), longArrayOf(), completion)
        }
    }

    private fun refreshNextTransactions(fromDate: String, toDate: String, accountIds: LongArray? = null,
                                        transactionIncluded: Boolean? = null, skip: Int,
                                        updatedTransactionIds: LongArray, updatedMerchantIds: LongArray,
                                        completion: OnFrolloSDKCompletionListener<Result>? = null) {

        aggregationAPI.fetchTransactionsByQuery(fromDate = fromDate, toDate = toDate,
                accountIds = accountIds, transactionIncluded = transactionIncluded,
                count = TRANSACTION_BATCH_SIZE, skip = skip).enqueue { resource ->

            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    val response = resource.data
                    response?.let {
                        val updatedIds = insertTransactions(response).plus(updatedTransactionIds)
                        val merchantIds = it.map { model -> model.merchant.id }.toLongArray().plus(updatedMerchantIds)

                        if (it.size >= TRANSACTION_BATCH_SIZE) {
                            refreshNextTransactions(fromDate, toDate, accountIds, transactionIncluded,
                                    skip + TRANSACTION_BATCH_SIZE, updatedIds, merchantIds, completion)
                        } else {
                            fetchMissingMerchants(merchantIds.toSet())
                            removeTransactions(fromDate, toDate, accountIds, transactionIncluded, updatedIds, completion)
                        }
                    } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshNextTransactions", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    /**
     * Refresh specific transactions by IDs from the host
     *
     * @param transactionIds List of transaction IDs to fetch
     * @param  completion Optional completion handler with optional error if the request fails
     */
    fun refreshTransactions(transactionIds: LongArray, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchTransactionsByIDs(transactionIds).enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleTransactionsByIDsResponse(response = resource.data, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshTransactionsByIDs", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    /**
     * Exclude a transaction from budgets and reports and update on the host
     *
     * @param transactionId ID of the transaction to be updated
     * @param excluded Exclusion status of the transaction. True will mark a transaction as no longer included in budgets etc
     * @param applyToAll Apply exclusion status to all similar transactions
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun excludeTransaction(transactionId: Long, excluded: Boolean, applyToAll: Boolean,
                           completion: OnFrolloSDKCompletionListener<Result>? = null) {
        doAsync {

            val transaction = db.transactions().loadTransaction(transactionId)

            uiThread {
                transaction?.let { model ->
                    val request = TransactionUpdateRequest(
                            budgetCategory = model.budgetCategory,
                            included = !excluded,
                            includeApplyAll = applyToAll)

                    aggregationAPI.updateTransaction(transactionId, request).enqueue { resource ->
                        when(resource.status) {
                            Resource.Status.SUCCESS -> {
                                handleTransactionResponse(response = resource.data, completion = completion)
                            }
                            Resource.Status.ERROR -> {
                                Log.e("$TAG#excludeTransaction", resource.error?.localizedDescription)
                                completion?.invoke(Result.error(resource.error))
                            }
                        }
                    }
                } ?: run {
                    completion?.invoke(Result.error(DataError(DataErrorType.DATABASE, DataErrorSubType.NOT_FOUND)))
                }
            }
        }
    }

    /**
     * Recategorise a transaction and update on the host
     *
     * @param transactionId ID of the transaction to be updated
     * @param transactionCategoryId The transaction category ID to recategorise the transaction to
     * @param applyToAll Apply recategorisation to all similar transactions
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun recategoriseTransaction(transactionId: Long, transactionCategoryId: Long, applyToAll: Boolean,
                                completion: OnFrolloSDKCompletionListener<Result>? = null) {
        doAsync {

            val transaction = db.transactions().loadTransaction(transactionId)

            uiThread {
                transaction?.let { model ->
                    val request = TransactionUpdateRequest(
                            budgetCategory = model.budgetCategory,
                            categoryId = transactionCategoryId,
                            recategoriseAll = applyToAll)

                    aggregationAPI.updateTransaction(transactionId, request).enqueue { resource ->
                        when(resource.status) {
                            Resource.Status.SUCCESS -> {
                                handleTransactionResponse(response = resource.data, completion = completion)
                            }
                            Resource.Status.ERROR -> {
                                Log.e("$TAG#recategoriseTransaction", resource.error?.localizedDescription)
                                completion?.invoke(Result.error(resource.error))
                            }
                        }
                    }
                } ?: run {
                    completion?.invoke(Result.error(DataError(DataErrorType.DATABASE, DataErrorSubType.NOT_FOUND)))
                }
            }
        }
    }

    /**
     * Update a transaction on the host
     *
     * @param transactionId ID of the transaction to be updated
     * @param transaction Updated transaction data model
     * @param recategoriseAll Apply recategorisation to all similar transactions (Optional)
     * @param includeApplyAll Apply included flag to all similar transactions (Optional)
     * @param userTags userTags Updated list of tags to be applied for the transaction. These tags will replace the existing ones. (Optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun updateTransaction(transactionId: Long, transaction: Transaction,
                          recategoriseAll: Boolean? = null, includeApplyAll: Boolean? = null,
                          userTags: List<String>? = null,
                          completion: OnFrolloSDKCompletionListener<Result>? = null) {

        val request = TransactionUpdateRequest(
                budgetCategory = transaction.budgetCategory,
                categoryId = transaction.categoryId,
                included = transaction.included,
                memo = transaction.memo,
                userDescription = transaction.description?.user,
                recategoriseAll = recategoriseAll,
                includeApplyAll = includeApplyAll,
                userTags = userTags)

        aggregationAPI.updateTransaction(transactionId, request).enqueue { resource ->
            when (resource.status) {
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

    /**
     * Search transactions
     *
     * Search for transactions from the server. Transactions will be cached and a list of matching transaction IDs returned.
     * Search results are paginated and retrieving the full list of more than 200 will require incrementing the [page] parameter.
     *
     * Example: Fetch results 201-400
     *
     * `transactionSearch(searchTerm: "supermarket", page: 1)`
     *
     * The search term will match the following fields on a transaction:
     *
     * - [TransactionDescription.original]
     * - [TransactionDescription.simple]
     * - [TransactionDescription.user]
     * - [Transaction.amount]
     * - [Merchant.name]
     * - [TransactionCategory.name]
     *
     * Magic search terms can also be used where the following will match specific types or properties rather than the fields above.
     *
     * - excluded - Only transactions where [Transaction.included] is false.
     * - pending - Only transactions where [Transaction.status] is pending.
     * - income - Budget category is income.
     * - lifestyle - Budget category is living.
     * - living - Budget category is lifestyle.
     * - goals - Budget category is goals.
     *
     * @param searchTerm Search term to match, either text, amount or magic term
     * @param page Page to start search from. Defaults to 0
     * @param fromDate Start date (inclusive) to fetch transactions from (optional). Please use [Transaction.DATE_FORMAT_PATTERN] for the format pattern.
     * @param toDate End date (inclusive) to fetch transactions up to (optional). Please use [Transaction.DATE_FORMAT_PATTERN] for the format pattern.
     * @param accountIds A list of account IDs to restrict search to (optional)
     * @param accountIncluded Only return results from accounts included in the budget (optional)
     * @param completion Completion handler with optional error if the request fails and array of transaction ids if succeeds
     */
    fun transactionSearch(searchTerm: String, page: Int = 0, fromDate: String? = null, toDate: String? = null,
                          accountIds: LongArray? = null, accountIncluded: Boolean? = null,
                          completion: OnFrolloSDKCompletionListener<Resource<LongArray>>) {

        if (searchTerm.isBlank()) {
            Log.d("$TAG#transactionSearch", "Search term is empty")
            val error = DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)
            completion.invoke(Resource.error(error))
            return
        }

        val skip = page * TRANSACTION_BATCH_SIZE

        aggregationAPI.transactionSearch(
                searchTerm = searchTerm, fromDate = fromDate, toDate = toDate, accountIds = accountIds,
                accountIncluded = accountIncluded, count = TRANSACTION_BATCH_SIZE, skip = skip).enqueue { resource ->

            when(resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#transactionSearch", resource.error?.localizedDescription)
                    completion.invoke(Resource.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    doAsync {
                        val response = resource.data
                        val transactionIds = mutableListOf<Long>()

                        response?.let { list ->
                            transactionIds.addAll(list.map { it.transactionId }.toList())
                            val merchantIds = list.map { it.merchant.id }.toSet()

                            insertTransactions(response)
                            fetchMissingMerchants(merchantIds)
                        }

                        uiThread {
                            completion.invoke(Resource.success(data = transactionIds.toLongArray()))
                        }
                    }
                }
            }
        }
    }

    /**
     * Fetch transactions summary from a certain period from the host
     *
     * @param fromDate Start date to fetch transactions summary from (inclusive). Please use [TransactionsSummary.DATE_FORMAT_PATTERN] for the format pattern.
     * @param toDate End date to fetch transactions summary up to (inclusive). Please use [TransactionsSummary.DATE_FORMAT_PATTERN] for the format pattern.
     * @param accountIds Specific account IDs of the transactions to fetch summary (optional)
     * @param onlyIncludedTransactions Boolean flag to indicate to fetch summary for only those transactions that are excluded/included in budget (optional)
     * @param onlyIncludedAccounts Boolean flag to indicate to fetch summary for only those transactions of excluded/included Accounts (optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun fetchTransactionsSummary(fromDate: String, toDate: String, accountIds: LongArray? = null,
                                 onlyIncludedTransactions: Boolean? = null, onlyIncludedAccounts: Boolean? = null,
                                 completion: OnFrolloSDKCompletionListener<Resource<TransactionsSummary>>) {
        aggregationAPI.fetchTransactionsSummaryByQuery(
                fromDate = fromDate, toDate = toDate,
                accountIds = accountIds, transactionIncluded = onlyIncludedTransactions,
                accountIncluded = onlyIncludedAccounts).enqueue { resource ->

            if (resource.status == Resource.Status.ERROR)
                Log.e("$TAG#fetchTransactionsSummary", resource.error?.localizedDescription)

            completion.invoke(resource.map { it?.toTransactionsSummary() })
        }
    }

    /**
     * Fetch transactions summary of specific transaction IDs from the host
     *
     * @param transactionIds List of transaction IDs to fetch summary of
     * @param  completion Optional completion handler with optional error if the request fails
     */
    fun fetchTransactionsSummary(transactionIds: LongArray, completion: OnFrolloSDKCompletionListener<Resource<TransactionsSummary>>) {
        aggregationAPI.fetchTransactionsSummaryByIDs(transactionIds).enqueue { resource ->

            if (resource.status == Resource.Status.ERROR)
                Log.e("$TAG#fetchTransactionsSummaryByIDs", resource.error?.localizedDescription)

            completion.invoke(resource.map { it?.toTransactionsSummary() })
        }
    }

    // Do not call this method from main thread. Call this asynchronously.
    private fun insertTransactions(response: List<TransactionResponse>) : LongArray {
        val models = mapTransactionResponse(response)
        db.transactions().insertAll(*models.toTypedArray())

        return response.map { it.transactionId }.toLongArray()
    }

    // Do not call this method from main thread. Call this asynchronously.
    private fun removeTransactions(fromDate: String, toDate: String,
                                   accountIds: LongArray? = null, transactionIncluded: Boolean? = null,
                                   excludingIds: LongArray, completion: OnFrolloSDKCompletionListener<Result>? = null) {

        val apiIds = excludingIds.sorted()
        val staleIds = ArrayList(db.transactions().getIdsQuery(
                sqlForTransactionStaleIds(fromDate = fromDate, toDate = toDate,
                        accountIds = accountIds, transactionIncluded = transactionIncluded)).sorted())

        staleIds.removeAll(apiIds)

        if (staleIds.isNotEmpty()) {
            removeCachedTransactions(staleIds.toLongArray())
        }

        completion?.invoke(Result.success())
    }

    private fun handleTransactionsByIDsResponse(response: List<TransactionResponse>?,
                                                completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                fetchMissingMerchants(response.map { it.merchant.id }.toSet())

                val models = mapTransactionResponse(response)
                db.transactions().insertAll(*models.toTypedArray())

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleTransactionResponse(response: TransactionResponse?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                fetchMissingMerchants(setOf(response.merchant.id))

                db.transactions().insert(response.toTransaction())

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun mapTransactionResponse(models: List<TransactionResponse>): List<Transaction> =
            models.map { it.toTransaction() }.toList()

    //Transaction user tags
    /**
     * Fetch transactions from the cache
     *
     * @param searchTerm tag name you want to search
     * @param sortBy sort results by SortByEnum.NAME, SortByEnum.CREATED_AT,SortByEnum.LAST_USED,SortByEnum.COUNT,SortByEnum.RELEVANCE
     * @param orderBy order results by OrderByEnum.ASC, OrderByEnum.DESC
     *
     * @return LiveData object of LiveData<Resource<List<TransactionTags>>> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactionUserTags(searchTerm: String? = null, sortBy: TagsSortType? = null, orderBy: OrderType? = null): LiveData<Resource<List<TransactionTag>>> {
        return Transformations.map(db.userTags().custom(sqlForUserTags(searchTerm,sortBy,orderBy))) { models ->
            Resource.success(models)
        }
    }

    /**
     * Get all existing transaction tags tagged by the user.
     *
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshTransactionUserTags(completion: OnFrolloSDKCompletionListener<Result>? = null) {

        aggregationAPI.userTagsSearch().enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleTransactionUserTagsResponse(resource.data, completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshTransactionUserTags", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    private fun handleTransactionUserTagsResponse(response: List<TransactionTagResponse>?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                val userTagList = it.map { it.toTransactionTag() }.toList()
                val userTagNames = it.map { it.name }.toList()
                db.userTags().deleteByNamesInverse(userTagNames)
                db.userTags().insertAll(userTagList)
                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    // Transaction Category

    /**
     * Fetch transaction category by ID from the cache
     *
     * @param transactionCategoryId Unique ID of the transaction category to fetch
     *
     * @return LiveData object of Resource<TransactionCategory> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactionCategory(transactionCategoryId: Long): LiveData<Resource<TransactionCategory>> =
            Transformations.map(db.transactionCategories().load(transactionCategoryId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch transaction categories from the cache
     *
     * @return LiveData object of Resource<List<TransactionCategory>> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactionCategories(): LiveData<Resource<List<TransactionCategory>>> =
            Transformations.map(db.transactionCategories().load()) { models ->
                Resource.success(models)
            }

    /**
     * Refresh all transaction categories from the host
     *
     * @param completion Optional completion handler with optional error if the request fails
     */
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

    /**
     * Fetch merchant by ID from the cache
     *
     * @param merchantId Unique ID of the merchant to fetch
     *
     * @return LiveData object of Resource<Merchant> which can be observed using an Observer for future changes as well.
     */
    fun fetchMerchant(merchantId: Long): LiveData<Resource<Merchant>> =
            Transformations.map(db.merchants().load(merchantId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch merchants from the cache
     *
     * @return LiveData object of Resource<List<Merchant>> which can be observed using an Observer for future changes as well.
     */
    fun fetchMerchants(): LiveData<Resource<List<Merchant>>> =
            Transformations.map(db.merchants().load()) { models ->
                Resource.success(models)
            }

    /**
     * Refresh a specific merchant by ID from the host
     *
     * @param merchantId ID of the merchant to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
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

    /**
     * Refresh all merchants by IDs from the host
     *
     * @param merchantIds IDs of the merchants to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
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

    /**
     * Refresh all merchants from the host
     *
     * @param completion Optional completion handler with optional error if the request fails
     */
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

    internal fun fetchMissingMerchants(merchantIds: Set<Long>) {
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