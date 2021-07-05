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
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.TemporalAdjusters
import us.frollo.frollosdk.base.PaginatedResult
import us.frollo.frollosdk.base.PaginationInfo
import us.frollo.frollosdk.base.PaginationInfoDatedCursor
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.base.doAsync
import us.frollo.frollosdk.base.uiThread
import us.frollo.frollosdk.core.ACTION.ACTION_REFRESH_TRANSACTIONS
import us.frollo.frollosdk.core.ARGUMENT.ARG_TRANSACTION_IDS
import us.frollo.frollosdk.core.LIMIT.SQLITE_MAX_VARIABLE_NUMBER
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.core.TagApplyAllPair
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.extensions.compareToFindMissingItems
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.fetchMerchants
import us.frollo.frollosdk.extensions.fetchProducts
import us.frollo.frollosdk.extensions.fetchSuggestedTags
import us.frollo.frollosdk.extensions.fetchTransactions
import us.frollo.frollosdk.extensions.fetchTransactionsSummaryByIDs
import us.frollo.frollosdk.extensions.fetchTransactionsSummaryByQuery
import us.frollo.frollosdk.extensions.fetchUserTags
import us.frollo.frollosdk.extensions.sqlForAccounts
import us.frollo.frollosdk.extensions.sqlForConsents
import us.frollo.frollosdk.extensions.sqlForMerchants
import us.frollo.frollosdk.extensions.sqlForMerchantsIds
import us.frollo.frollosdk.extensions.sqlForProviderAccounts
import us.frollo.frollosdk.extensions.sqlForProviders
import us.frollo.frollosdk.extensions.sqlForTransactionCategories
import us.frollo.frollosdk.extensions.sqlForTransactionIdsToGetStaleIds
import us.frollo.frollosdk.extensions.sqlForTransactions
import us.frollo.frollosdk.extensions.sqlForUpdateAccount
import us.frollo.frollosdk.extensions.sqlForUserTags
import us.frollo.frollosdk.extensions.toString
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toAccount
import us.frollo.frollosdk.mapping.toCDRConfiguration
import us.frollo.frollosdk.mapping.toConsent
import us.frollo.frollosdk.mapping.toConsentCreateRequest
import us.frollo.frollosdk.mapping.toConsentUpdateRequest
import us.frollo.frollosdk.mapping.toMerchant
import us.frollo.frollosdk.mapping.toProvider
import us.frollo.frollosdk.mapping.toProviderAccount
import us.frollo.frollosdk.mapping.toProvidersResponse
import us.frollo.frollosdk.mapping.toTransaction
import us.frollo.frollosdk.mapping.toTransactionCategory
import us.frollo.frollosdk.mapping.toTransactionTag
import us.frollo.frollosdk.mapping.toTransactionsSummary
import us.frollo.frollosdk.model.api.aggregation.accounts.AccountResponse
import us.frollo.frollosdk.model.api.aggregation.accounts.AccountUpdateRequest
import us.frollo.frollosdk.model.api.aggregation.merchants.MerchantResponse
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountCreateRequest
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountResponse
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountUpdateRequest
import us.frollo.frollosdk.model.api.aggregation.providers.ProviderResponse
import us.frollo.frollosdk.model.api.aggregation.tags.TransactionTagResponse
import us.frollo.frollosdk.model.api.aggregation.tags.TransactionTagUpdateRequest
import us.frollo.frollosdk.model.api.aggregation.transactioncategories.TransactionCategoryResponse
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionResponse
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionUpdateRequest
import us.frollo.frollosdk.model.api.cdr.CDRConfigurationResponse
import us.frollo.frollosdk.model.api.cdr.ConsentResponse
import us.frollo.frollosdk.model.api.shared.PaginatedResponse
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Account
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountClassification
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountRelation
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountStatus
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountSubType
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountType
import us.frollo.frollosdk.model.coredata.aggregation.merchants.Merchant
import us.frollo.frollosdk.model.coredata.aggregation.merchants.MerchantType
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccount
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccountRelation
import us.frollo.frollosdk.model.coredata.aggregation.providers.CDRProduct
import us.frollo.frollosdk.model.coredata.aggregation.providers.CDRProductCategory
import us.frollo.frollosdk.model.coredata.aggregation.providers.Provider
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderLoginForm
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderRelation
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderStatus
import us.frollo.frollosdk.model.coredata.aggregation.tags.SuggestedTagsSortType
import us.frollo.frollosdk.model.coredata.aggregation.tags.TagsSortType
import us.frollo.frollosdk.model.coredata.aggregation.tags.TransactionTag
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategory
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategoryType
import us.frollo.frollosdk.model.coredata.aggregation.transactions.Transaction
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionFilter
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionRelation
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionsSummary
import us.frollo.frollosdk.model.coredata.cdr.CDRConfiguration
import us.frollo.frollosdk.model.coredata.cdr.Consent
import us.frollo.frollosdk.model.coredata.cdr.ConsentCreateForm
import us.frollo.frollosdk.model.coredata.cdr.ConsentRelation
import us.frollo.frollosdk.model.coredata.cdr.ConsentStatus
import us.frollo.frollosdk.model.coredata.cdr.ConsentUpdateForm
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.model.coredata.shared.OrderType
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.AggregationAPI
import us.frollo.frollosdk.network.api.CdrAPI

/**
 * Manages all aggregation data including accounts, transactions, categories and merchants.
 */
class Aggregation(network: NetworkService, internal val db: SDKDatabase, localBroadcastManager: LocalBroadcastManager) {

    companion object {
        private const val TAG = "Aggregation"
        private const val MERCHANT_BATCH_SIZE = 500 // DO NOT INCREASE THIS. API MAX SIZE IS 500.
    }

    private val aggregationAPI: AggregationAPI = network.create(AggregationAPI::class.java)
    private val cdrAPI: CdrAPI = network.create(CdrAPI::class.java)

    private var refreshingMerchantIDs = setOf<Long>()
    private var refreshingProviderIDs = setOf<Long>()

    private val refreshTransactionsReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val transactionIds = intent.getLongArrayExtra(ARG_TRANSACTION_IDS)
            transactionIds?.let {
                refreshTransactionsWithPagination(TransactionFilter(transactionIds = it.toList()))
            }
        }
    }

    init {
        localBroadcastManager.registerReceiver(
            refreshTransactionsReceiver,
            IntentFilter(ACTION_REFRESH_TRANSACTIONS)
        )
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
     * @param status Filter by status of the provider support (Optional)
     *
     * @return LiveData object of Resource<List<Provider>> which can be observed using an Observer for future changes as well.
     */
    fun fetchProviders(status: ProviderStatus? = null): LiveData<Resource<List<Provider>>> =
        Transformations.map(db.providers().loadByQuery(sqlForProviders(status = status))) { models ->
            Resource.success(models)
        }

    /**
     * Advanced method to fetch providers by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches providers from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<Provider>> which can be observed using an Observer for future changes as well.
     */
    fun fetchProviders(query: SimpleSQLiteQuery): LiveData<Resource<List<Provider>>> =
        Transformations.map(db.providers().loadByQuery(query)) { model ->
            Resource.success(model)
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
     * @param status Filter by status of the provider support (Optional)
     *
     * @return LiveData object of Resource<List<ProviderRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchProvidersWithRelation(status: ProviderStatus? = null): LiveData<Resource<List<ProviderRelation>>> =
        Transformations.map(db.providers().loadByQueryWithRelation(sqlForProviders(status = status))) { models ->
            Resource.success(models)
        }

    /**
     * Advanced method to fetch providers by SQL query from the cache along with other associated data.
     *
     * @param query SimpleSQLiteQuery: Select query which fetches providers from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<ProviderRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchProvidersWithRelation(query: SimpleSQLiteQuery): LiveData<Resource<List<ProviderRelation>>> =
        Transformations.map(db.providers().loadByQueryWithRelation(query)) { model ->
            Resource.success(model)
        }

    /**
     * Refresh a specific provider by ID from the host
     *
     * @param providerId ID of the provider to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshProvider(providerId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchProvider(providerId).enqueue { resource ->
            when (resource.status) {
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
     * Includes Beta, Supported, Coming Soon and Outage providers. Unsupported and Disabled providers must be fetched by ID.
     *
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshProviders(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchProviders().enqueue { resource ->
            when (resource.status) {
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
                val existingIds = db.providers().getIdsByStatus().toHashSet() // These are providers with status BETA & SUPPORTED
                val models = mapProviderResponse(response)

                val modelsToInsert = models.filter { it.providerId !in existingIds }
                if (modelsToInsert.isNotEmpty()) {
                    db.providers().insertAll(*modelsToInsert.toTypedArray())
                }

                // As fetchAllProviders() response has lesser fields we have to use update() specific columns in DB
                // instead of conflict insert. Otherwise it will remove the extra data fetched by the fetchProvider() response.
                val modelsToUpdate = models.filter { it.providerId in existingIds }
                if (modelsToUpdate.isNotEmpty()) {
                    db.providers().update(*modelsToUpdate.map { it.toProvidersResponse() }.toTypedArray())
                }

                val apiIds = response.map { it.providerId }.toHashSet()
                val modelsToDelete = existingIds.minus(apiIds)
                if (modelsToDelete.isNotEmpty()) {
                    removeCachedProviders(modelsToDelete.toLongArray())
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
     * @param providerId Provider ID of the provider accounts to fetch (Optional)
     * @param refreshStatus Filter by the current refresh status of the provider account (Optional)
     * @param externalId External aggregator ID of the provider accounts to fetch (Optional)
     *
     * @return LiveData object of Resource<List<ProviderAccount>> which can be observed using an Observer for future changes as well.
     */
    fun fetchProviderAccounts(providerId: Long? = null, refreshStatus: AccountRefreshStatus? = null, externalId: String? = null): LiveData<Resource<List<ProviderAccount>>> =
        Transformations.map(db.providerAccounts().loadByQuery(sqlForProviderAccounts(providerId, refreshStatus, externalId))) { models ->
            Resource.success(models)
        }

    /**
     * Advanced method to fetch provider accounts by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches provider accounts from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<ProviderAccount>> which can be observed using an Observer for future changes as well.
     */
    fun fetchProviderAccounts(query: SimpleSQLiteQuery): LiveData<Resource<List<ProviderAccount>>> =
        Transformations.map(db.providerAccounts().loadByQuery(query)) { model ->
            Resource.success(model)
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
     * @param providerId Provider ID of the provider accounts to fetch (Optional)
     * @param refreshStatus Filter by the current refresh status of the provider account (Optional)
     * @param externalId External aggregator ID of the provider accounts to fetch (Optional)
     *
     * @return LiveData object of Resource<List<ProviderAccountRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchProviderAccountsWithRelation(providerId: Long? = null, refreshStatus: AccountRefreshStatus? = null, externalId: String? = null): LiveData<Resource<List<ProviderAccountRelation>>> =
        Transformations.map(db.providerAccounts().loadByQueryWithRelation(sqlForProviderAccounts(providerId, refreshStatus, externalId))) { models ->
            Resource.success(models)
        }

    /**
     * Advanced method to fetch provider accounts by SQL query from the cache along with other associated data.
     *
     * @param query SimpleSQLiteQuery: Select query which fetches provider accounts from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<ProviderAccountRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchProviderAccountsWithRelation(query: SimpleSQLiteQuery): LiveData<Resource<List<ProviderAccountRelation>>> =
        Transformations.map(db.providerAccounts().loadByQueryWithRelation(query)) { model ->
            Resource.success(model)
        }

    /**
     * Refresh a specific provider account by ID from the host
     *
     * @param providerAccountId ID of the provider account to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshProviderAccount(providerAccountId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchProviderAccount(providerAccountId).enqueue { resource ->
            when (resource.status) {
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
            when (resource.status) {
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
     * Fetches the latest account data from the aggregation partner.
     *
     * @param providerAccountIds Array of IDs of the provider accounts to be synced
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun syncProviderAccounts(providerAccountIds: LongArray, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (providerAccountIds.isEmpty()) {
            completion?.invoke(Result.success())
            return
        }
        aggregationAPI.refreshProviderAccounts(providerAccountIds).enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    handleProviderAccountsResponse(response = resource.data, providerAccountIds = providerAccountIds, completion = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#syncProviderAccounts", resource.error?.localizedDescription)
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
     * @param consentId ID of the consent for creating the account
     * @param completion Optional completion handler with optional error if the request fails else ID of the ProviderAccount created if success
     */
    fun createProviderAccount(providerId: Long, loginForm: ProviderLoginForm, consentId: Long? = null, completion: OnFrolloSDKCompletionListener<Resource<Long>>? = null) {
        val request = ProviderAccountCreateRequest(loginForm = loginForm, providerID = providerId, consentId = consentId)

        aggregationAPI.createProviderAccount(request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    handleProviderAccountResponse(response = resource.data, completionWithData = completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#createProviderAccount", resource.error?.localizedDescription)
                    completion?.invoke(Resource.error(resource.error))
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
            when (resource.status) {
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
            when (resource.status) {
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

    private fun handleProviderAccountsResponse(
        response: List<ProviderAccountResponse>?,
        providerAccountIds: LongArray? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        response?.let {
            doAsync {
                fetchMissingProviders(response.map { it.providerId }.toSet())

                val models = mapProviderAccountResponse(response)
                db.providerAccounts().insertAll(*models.toTypedArray())

                if (providerAccountIds == null) {
                    val apiIds = response.map { it.providerAccountId }.toList()
                    val staleIds = db.providerAccounts().getStaleIds(apiIds.toLongArray())

                    if (staleIds.isNotEmpty()) {
                        removeCachedProviderAccounts(staleIds.toLongArray())
                    }
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleProviderAccountResponse(
        response: ProviderAccountResponse?,
        completion: OnFrolloSDKCompletionListener<Result>? = null,
        completionWithData: OnFrolloSDKCompletionListener<Resource<Long>>? = null
    ) {
        response?.let {
            doAsync {
                fetchMissingProviders(setOf(response.providerId))

                db.providerAccounts().insert(response.toProviderAccount())

                uiThread {
                    completion?.invoke(Result.success())
                    completionWithData?.invoke(Resource.success(response.providerAccountId))
                }
            }
        } ?: run {
            completion?.invoke(Result.success())
            completionWithData?.invoke(Resource.success(null))
        } // Explicitly invoke completion callback if response is null.
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
     * @param providerAccountId Filter by the Provider account ID (Optional)
     * @param accountStatus Filter by the account status (Optional)
     * @param accountSubType Filter by the sub type of account (Optional)
     * @param accountType Filter by the type of the account (Optional)
     * @param accountClassification Filter by the classification of the account (Optional)
     * @param favourite Filter by favourited accounts (Optional)
     * @param hidden Filter by hidden accounts (Optional)
     * @param included Filter by accounts included in the budget (Optional)
     * @param refreshStatus Filter by the current refresh status of the provider account (Optional)
     * @param externalId External aggregator ID of the accounts to fetch (Optional)
     *
     * @return LiveData object of Resource<List<Account>> which can be observed using an Observer for future changes as well.
     */
    fun fetchAccounts(
        providerAccountId: Long? = null,
        accountStatus: AccountStatus? = null,
        accountSubType: AccountSubType? = null,
        accountType: AccountType? = null,
        accountClassification: AccountClassification? = null,
        favourite: Boolean? = null,
        hidden: Boolean? = null,
        included: Boolean? = null,
        refreshStatus: AccountRefreshStatus? = null,
        externalId: String? = null
    ): LiveData<Resource<List<Account>>> =
        Transformations.map(
            db.accounts().loadByQuery(
                sqlForAccounts(
                    providerAccountId = providerAccountId,
                    accountStatus = accountStatus,
                    accountSubType = accountSubType,
                    accountType = accountType,
                    accountClassification = accountClassification,
                    favourite = favourite,
                    hidden = hidden,
                    included = included,
                    refreshStatus = refreshStatus,
                    externalId = externalId
                )
            )
        ) { models ->
            Resource.success(models)
        }

    /**
     * Advanced method to fetch accounts by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches accounts from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<Account>> which can be observed using an Observer for future changes as well.
     */
    fun fetchAccounts(query: SimpleSQLiteQuery): LiveData<Resource<List<Account>>> =
        Transformations.map(db.accounts().loadByQuery(query)) { model ->
            Resource.success(model)
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
     * @param providerAccountId Filter by the Provider account ID (Optional)
     * @param accountStatus Filter by the account status (Optional)
     * @param accountSubType Filter by the sub type of account (Optional)
     * @param accountType Filter by the type of the account (Optional)
     * @param accountClassification Filter by the classification of the account (Optional)
     * @param favourite Filter by favourited accounts (Optional)
     * @param hidden Filter by hidden accounts (Optional)
     * @param included Filter by accounts included in the budget (Optional)
     * @param refreshStatus Filter by the current refresh status of the provider account (Optional)
     * @param externalId External aggregator ID of the accounts to fetch (Optional)
     *
     * @return LiveData object of Resource<List<AccountRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchAccountsWithRelation(
        providerAccountId: Long? = null,
        accountStatus: AccountStatus? = null,
        accountSubType: AccountSubType? = null,
        accountType: AccountType? = null,
        accountClassification: AccountClassification? = null,
        favourite: Boolean? = null,
        hidden: Boolean? = null,
        included: Boolean? = null,
        refreshStatus: AccountRefreshStatus? = null,
        externalId: String? = null
    ): LiveData<Resource<List<AccountRelation>>> =
        Transformations.map(
            db.accounts().loadByQueryWithRelation(
                sqlForAccounts(
                    providerAccountId = providerAccountId,
                    accountStatus = accountStatus,
                    accountSubType = accountSubType,
                    accountType = accountType,
                    accountClassification = accountClassification,
                    favourite = favourite,
                    hidden = hidden,
                    included = included,
                    refreshStatus = refreshStatus,
                    externalId = externalId
                )
            )
        ) { models ->
            Resource.success(models)
        }

    /**
     * Advanced method to fetch accounts by SQL query from the cache along with other associated data.
     *
     * @param query SimpleSQLiteQuery: Select query which fetches accounts from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<AccountRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchAccountsWithRelation(query: SimpleSQLiteQuery): LiveData<Resource<List<AccountRelation>>> =
        Transformations.map(db.accounts().loadByQueryWithRelation(query)) { model ->
            Resource.success(model)
        }

    /**
     * Refresh a specific account by ID from the host
     *
     * @param accountId ID of the account to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshAccount(accountId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchAccount(accountId).enqueue { resource ->
            when (resource.status) {
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
            when (resource.status) {
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
     * This method updates the cache immediately so the UI can be updated
     * and triggers a request in the background to update on the host.
     * Host may return error, in which case it is the responsibility
     * of the client app to handle it in the UI
     *
     * @param accountId ID of the account to be updated
     * @param hidden Used to hide the account in the UI
     * @param included Used to exclude accounts from counting towards the user's budgets
     * @param favourite Mark the account as favourite for UI purposes (optional)
     * @param accountSubType Sub type of the account indicating more detail what the account is (optional)
     * @param nickName Nickname given to the account for display and identification purposes (optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun updateAccount(
        accountId: Long,
        hidden: Boolean,
        included: Boolean,
        favourite: Boolean? = null,
        accountSubType: AccountSubType? = null,
        nickName: String? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        // Updating account cache immediately to update the UI
        updateAccountCache(
            accountId = accountId,
            hidden = hidden,
            included = included,
            favourite = favourite,
            accountSubType = accountSubType,
            nickName = nickName
        )

        val request = AccountUpdateRequest(
            hidden = hidden,
            included = included,
            favourite = favourite,
            accountSubType = accountSubType,
            nickName = nickName
        )

        if (!request.valid) {
            Log.e("$TAG#updateAccount", "'hidden' and 'included' must compliment each other. Both cannot be true.")
            val error = DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)
            completion?.invoke(Result.error(error))
            return
        }

        aggregationAPI.updateAccount(accountId, request).enqueue { resource ->
            when (resource.status) {
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

    private fun updateAccountCache(
        accountId: Long,
        hidden: Boolean,
        included: Boolean,
        favourite: Boolean? = null,
        accountSubType: AccountSubType? = null,
        nickName: String? = null
    ) {
        doAsync {
            db.accounts().updateByQuery(
                sqlForUpdateAccount(
                    accountId = accountId,
                    hidden = hidden,
                    included = included,
                    favourite = favourite,
                    accountSubType = accountSubType,
                    nickName = nickName
                )
            )
        }
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
     * Fetch transactions from the cache
     *
     * @param transactionFilter [TransactionFilter] object to apply filters (Optional)
     *
     * @return LiveData object of Resource<List<Transaction>> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactions(transactionFilter: TransactionFilter? = null): LiveData<Resource<List<Transaction>>> =
        Transformations.map(db.transactions().loadByQuery(sqlForTransactions(transactionFilter))) { models ->
            Resource.success(models)
        }

    /**
     * Advanced method to fetch transactions by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches transactions from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<Transaction>> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactions(query: SimpleSQLiteQuery): LiveData<Resource<List<Transaction>>> =
        Transformations.map(db.transactions().loadByQuery(query)) { model ->
            Resource.success(model)
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
     * Fetch transactions from the cache along with other associated data
     *
     * @param transactionFilter [TransactionFilter] object to apply filters (Optional)
     *
     * @return LiveData object of Resource<List<Transaction>> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactionsWithRelation(transactionFilter: TransactionFilter? = null): LiveData<Resource<List<TransactionRelation>>> =
        Transformations.map(db.transactions().loadByQueryWithRelation(sqlForTransactions(transactionFilter))) { models ->
            Resource.success(models)
        }

    /**
     * Advanced method to fetch transactions by SQL query from the cache with other associated data.
     *
     * @param query SimpleSQLiteQuery: Select query which fetches transactions from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<TransactionRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactionsWithRelation(query: SimpleSQLiteQuery): LiveData<Resource<List<TransactionRelation>>> =
        Transformations.map(db.transactions().loadByQueryWithRelation(query)) { model ->
            Resource.success(model)
        }

    /**
     * Refresh transactions from the host
     *
     * @param transactionFilter [TransactionFilter] object to filter transactions
     * @param completion Optional completion handler with optional error if the request fails else pagination data is success
     */
    fun refreshTransactionsWithPagination(
        transactionFilter: TransactionFilter? = null,
        completion: OnFrolloSDKCompletionListener<PaginatedResult<PaginationInfoDatedCursor>>? = null
    ) {
        aggregationAPI.fetchTransactions(transactionFilter).enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    handleRefreshTransactionsWithPaginationResponse(resource.data, transactionFilter, completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshTransactionsWithPagination", resource.error?.localizedDescription)
                    completion?.invoke(PaginatedResult.Error(resource.error))
                }
            }
        }
    }

    /**
     * Refresh a specific transaction by ID from the host
     *
     * @param transactionId ID of the transaction to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshTransaction(transactionId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchTransaction(transactionId).enqueue { resource ->
            when (resource.status) {
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
     * A convenience method that must refresh transactions between by IDs iteratively.
     *
     * @param transactionIds List of transaction IDs to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshTransactions(transactionIds: LongArray, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        val transactionFilter = TransactionFilter(transactionIds = transactionIds.toList())
        refreshNextTransactions(transactionFilter) { result ->
            when (result) {
                is PaginatedResult.Success -> {
                    completion?.invoke(Result.success())
                }
                is PaginatedResult.Error -> {
                    Log.e("$TAG#refreshTransactionsByIds", result.error?.localizedDescription)
                    completion?.invoke(Result.error(result.error))
                }
            }
        }
    }

    /**
     * A convenience method that must refresh transactions between two dates iteratively.
     *
     * @param fromDate Start date to fetch transactions from (inclusive). Please use [Transaction.DATE_FORMAT_PATTERN] for the format pattern. (Optional)
     * @param toDate End date to fetch transactions up to (inclusive). Please use [Transaction.DATE_FORMAT_PATTERN] for the format pattern. (Optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshTransactionsByDate(
        fromDate: String? = null,
        toDate: String? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        val startDate = fromDate ?: LocalDate.now().minusMonths(2).with(TemporalAdjusters.firstDayOfMonth()).toString(Transaction.DATE_FORMAT_PATTERN)
        val endDate = toDate ?: LocalDate.now().toString(Transaction.DATE_FORMAT_PATTERN)

        val transactionFilter = TransactionFilter(fromDate = startDate, toDate = endDate)

        refreshNextTransactions(transactionFilter) { result ->
            when (result) {
                is PaginatedResult.Success -> {
                    completion?.invoke(Result.success())
                }
                is PaginatedResult.Error -> {
                    Log.e("$TAG#refreshTransactionsByDate", result.error?.localizedDescription)
                    completion?.invoke(Result.error(result.error))
                }
            }
        }
    }

    private fun refreshNextTransactions(
        transactionFilter: TransactionFilter,
        completion: OnFrolloSDKCompletionListener<PaginatedResult<PaginationInfoDatedCursor>>? = null
    ) {
        refreshTransactionsWithPagination(transactionFilter) { result ->
            when (result) {
                is PaginatedResult.Success -> {
                    result.paginationInfo?.let { paginationInfo ->
                        if (paginationInfo.after == null) {
                            completion?.invoke(result)
                        } else {
                            val updatedTransactionFilter = transactionFilter.copy()
                            updatedTransactionFilter.after = paginationInfo.after
                            refreshNextTransactions(updatedTransactionFilter, completion)
                        }
                    }
                }
                is PaginatedResult.Error -> {
                    Log.e("$TAG#refreshNextTransactions", result.error?.localizedDescription)
                    completion?.invoke(result)
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
    fun excludeTransaction(
        transactionId: Long,
        excluded: Boolean,
        applyToAll: Boolean,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        doAsync {

            val transaction = db.transactions().loadTransaction(transactionId)

            uiThread {
                transaction?.let { model ->
                    val request = TransactionUpdateRequest(
                        budgetCategory = model.budgetCategory,
                        included = !excluded,
                        includeApplyAll = applyToAll
                    )

                    aggregationAPI.updateTransaction(transactionId, request).enqueue { resource ->
                        when (resource.status) {
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
    fun recategoriseTransaction(
        transactionId: Long,
        transactionCategoryId: Long,
        applyToAll: Boolean,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        doAsync {

            val transaction = db.transactions().loadTransaction(transactionId)

            uiThread {
                transaction?.let { model ->
                    val request = TransactionUpdateRequest(
                        budgetCategory = model.budgetCategory,
                        categoryId = transactionCategoryId,
                        recategoriseAll = applyToAll
                    )

                    aggregationAPI.updateTransaction(transactionId, request).enqueue { resource ->
                        when (resource.status) {
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
     * @param budgetCategory New budget category for the transaction (Optional)
     * @param budgetCategoryApplyAll Apply budget category to all similar transactions (Optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun updateTransaction(
        transactionId: Long,
        transaction: Transaction,
        recategoriseAll: Boolean? = null,
        includeApplyAll: Boolean? = null,
        budgetCategory: BudgetCategory = transaction.budgetCategory,
        budgetCategoryApplyAll: Boolean? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        val request = TransactionUpdateRequest(
            budgetCategory = budgetCategory,
            categoryId = transaction.categoryId,
            included = transaction.included,
            memo = transaction.memo,
            userDescription = transaction.description?.user,
            recategoriseAll = recategoriseAll,
            budgetCategoryApplyAll = budgetCategoryApplyAll,
            includeApplyAll = includeApplyAll
        )

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
     * Fetch transactions summary from a certain period from the host
     *
     * @param fromDate Start date to fetch transactions summary from (inclusive). Please use [TransactionsSummary.DATE_FORMAT_PATTERN] for the format pattern.
     * @param toDate End date to fetch transactions summary up to (inclusive). Please use [TransactionsSummary.DATE_FORMAT_PATTERN] for the format pattern.
     * @param accountIds Specific account IDs of the transactions to fetch summary (optional)
     * @param onlyIncludedTransactions Boolean flag to indicate to fetch summary for only those transactions that are excluded/included in budget (optional)
     * @param onlyIncludedAccounts Boolean flag to indicate to fetch summary for only those transactions of excluded/included Accounts (optional)
     * @param completion Optional completion handler with optional error if the request fails or the transactions summary model if succeeds
     */
    fun fetchTransactionsSummary(
        fromDate: String,
        toDate: String,
        accountIds: LongArray? = null,
        onlyIncludedTransactions: Boolean? = null,
        onlyIncludedAccounts: Boolean? = null,
        completion: OnFrolloSDKCompletionListener<Resource<TransactionsSummary>>
    ) {
        aggregationAPI.fetchTransactionsSummaryByQuery(
            fromDate = fromDate, toDate = toDate,
            accountIds = accountIds, transactionIncluded = onlyIncludedTransactions,
            accountIncluded = onlyIncludedAccounts
        ).enqueue { resource ->

            if (resource.status == Resource.Status.ERROR)
                Log.e("$TAG#fetchTransactionsSummary", resource.error?.localizedDescription)

            completion.invoke(resource.map { it?.toTransactionsSummary() })
        }
    }

    /**
     * Fetch transactions summary of specific transaction IDs from the host
     *
     * @param transactionIds List of transaction IDs to fetch summary of
     * @param completion Optional completion handler with optional error if the request fails or the transactions summary model if succeeds
     */
    fun fetchTransactionsSummary(transactionIds: LongArray, completion: OnFrolloSDKCompletionListener<Resource<TransactionsSummary>>) {
        aggregationAPI.fetchTransactionsSummaryByIDs(transactionIds).enqueue { resource ->
            if (resource.status == Resource.Status.ERROR)
                Log.e("$TAG#fetchTransactionsSummaryByIDs", resource.error?.localizedDescription)

            completion.invoke(resource.map { it?.toTransactionsSummary() })
        }
    }

    private fun handleRefreshTransactionsWithPaginationResponse(
        paginatedResponse: PaginatedResponse<TransactionResponse>?,
        transactionFilter: TransactionFilter?,
        completion: OnFrolloSDKCompletionListener<PaginatedResult<PaginationInfoDatedCursor>>? = null
    ) {
        paginatedResponse?.data?.let { transactions ->
            if (transactions.isEmpty()) {
                completion?.invoke(PaginatedResult.Success())
                return
            }

            doAsync {
                val firstTransaction = transactions.first()
                val lastTransaction = transactions.last()

                var beforeDate: String? = null
                var afterDate: String? = null
                var beforeId: Long? = null
                var afterId: Long? = null

                // Upper limit predicate if not first page
                paginatedResponse.paging.cursors?.before?.let {
                    beforeDate = firstTransaction.transactionDate
                    beforeId = firstTransaction.transactionId
                }

                // Lower limit predicate if not last page
                paginatedResponse.paging.cursors?.after?.let {
                    afterDate = lastTransaction.transactionDate
                    afterId = lastTransaction.transactionId
                }

                // Insert all transactions & fetch IDs from API response
                val apiIds = insertTransactions(transactions)

                // Fetch missing merchants
                val merchantIds = transactions.map { model -> model.merchant.id }.toLongArray()
                fetchMissingMerchants(merchantIds.toSet())

                // Get IDs from database
                val localIds = db.transactions().getIdsQuery(
                    sqlForTransactionIdsToGetStaleIds(
                        beforeDateString = beforeDate,
                        afterDateString = afterDate,
                        beforeId = beforeId,
                        afterId = afterId,
                        transactionFilter = transactionFilter
                    )
                )

                // Get stale IDs that are not present in the API response
                val staleIds = localIds.toHashSet().minus(apiIds.toHashSet())

                // Delete the entries for these stale IDs from database if they exist
                if (staleIds.isNotEmpty()) {
                    removeCachedTransactions(staleIds.toLongArray())
                }

                uiThread {
                    val paginationInfo = PaginatedResult.Success(
                        PaginationInfoDatedCursor(
                            before = paginatedResponse.paging.cursors?.before,
                            after = paginatedResponse.paging.cursors?.after,
                            total = paginatedResponse.paging.total,
                            beforeDate = firstTransaction.transactionDate,
                            beforeId = firstTransaction.transactionId,
                            afterDate = lastTransaction.transactionDate,
                            afterId = lastTransaction.transactionId
                        )
                    )
                    completion?.invoke(paginationInfo)
                }
            }
        } ?: run { completion?.invoke(PaginatedResult.Success()) } // Explicitly invoke completion callback if response is null.
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

    // Do not call this method from main thread. Call this asynchronously.
    private fun insertTransactions(response: List<TransactionResponse>): LongArray {
        val models = mapTransactionResponse(response)
        db.transactions().insertAll(*models.toTypedArray())

        return response.map { it.transactionId }.toLongArray()
    }

    private fun mapTransactionResponse(models: List<TransactionResponse>): List<Transaction> =
        models.map { it.toTransaction() }.toList()

    // Transaction Tags

    /**
     * Fetch all tags for a specific transaction from the host
     *
     * @param transactionId Transaction ID to fetch tags of
     * @param completion Optional completion handler with optional error if the request fails or the list of tags if succeeds
     */
    fun fetchTagsForTransaction(transactionId: Long, completion: OnFrolloSDKCompletionListener<Resource<List<String>>>) {
        aggregationAPI.fetchTags(transactionId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#fetchTagsForTransaction", resource.error?.localizedDescription)
                    completion.invoke(Resource.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    val tags = resource.data?.map { it.name }?.toList()
                    completion.invoke(Resource.success(data = tags))
                }
            }
        }
    }

    /**
     * Add a tag or a list of tags to a transaction
     *
     * @param transactionId Transaction ID to add tags for
     * @param tagApplyAllPairs Array of [TagApplyAllPair]
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun addTagsToTransaction(transactionId: Long, tagApplyAllPairs: Array<TagApplyAllPair>, completion: OnFrolloSDKCompletionListener<Result>) {
        if (tagApplyAllPairs.isEmpty()) {
            val error = DataError(type = DataErrorType.API, subType = DataErrorSubType.INVALID_DATA)
            Log.e("$TAG#addTagsToTransaction", "Empty Tags List")
            completion.invoke(Result.error(error))
            return
        }

        val tagNames = tagApplyAllPairs.map { it.first }.toTypedArray()

        val requestArray = tagApplyAllPairs.map {
            TransactionTagUpdateRequest(
                name = it.first,
                applyToAll = it.second
            )
        }.toTypedArray()

        aggregationAPI.createTags(transactionId, requestArray).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#addTagsToTransaction", resource.error?.localizedDescription)
                    completion.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleUpdateTagsResponse(tagNames = tagNames, isAdd = true, transactionId = transactionId, completion = completion)
                }
            }
        }
    }

    /**
     * Remove a tag or a list of tags from a transaction
     *
     * @param transactionId Transaction ID to remove tags from
     * @param tagApplyAllPairs Array of [TagApplyAllPair]
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun removeTagsFromTransaction(transactionId: Long, tagApplyAllPairs: Array<TagApplyAllPair>, completion: OnFrolloSDKCompletionListener<Result>) {
        if (tagApplyAllPairs.isEmpty()) {
            val error = DataError(type = DataErrorType.API, subType = DataErrorSubType.INVALID_DATA)
            Log.e("$TAG#removeTagsFromTransaction", "Empty Tags List")
            completion.invoke(Result.error(error))
            return
        }

        val tagNames = tagApplyAllPairs.map { it.first }.toTypedArray()

        val requestArray = tagApplyAllPairs.map {
            TransactionTagUpdateRequest(
                name = it.first,
                applyToAll = it.second
            )
        }.toTypedArray()

        aggregationAPI.deleteTags(transactionId, requestArray).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#removeTagsFromTransaction", resource.error?.localizedDescription)
                    completion.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleUpdateTagsResponse(tagNames = tagNames, isAdd = false, transactionId = transactionId, completion = completion)
                }
            }
        }
    }

    private fun handleUpdateTagsResponse(
        tagNames: Array<String>,
        isAdd: Boolean = true,
        transactionId: Long,
        completion: OnFrolloSDKCompletionListener<Result>
    ) {
        doAsync {
            val model = db.transactions().loadTransaction(transactionId)
            model?.let {
                val tags = if (isAdd)
                    it.userTags?.plus(tagNames)?.toSet()
                else
                    it.userTags?.minus(tagNames)?.toSet()
                it.userTags = tags?.toList()
                db.transactions().update(it)
            }

            uiThread { completion.invoke(Result.success()) }
        }
    }

    // Transaction User Tags

    /**
     * Fetch all user tags for transactions from cache. Tags can be filtered, sorted and ordered based on the parameters provided.
     *
     * @param searchTerm the search term to filter the tags on. (Optional)
     * @param sortBy Sort type for sorting the results. See [TagsSortType] for more details.(Optional)
     * @param orderBy Order type for ordering the results. See [OrderType] for more details.(Optional)
     * @return LiveData object of LiveData<Resource<List<TransactionTag>>> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactionUserTags(searchTerm: String? = null, sortBy: TagsSortType? = null, orderBy: OrderType? = null): LiveData<Resource<List<TransactionTag>>> {
        return Transformations.map(db.userTags().loadByQuery(sqlForUserTags(searchTerm, sortBy, orderBy))) { models ->
            Resource.success(models)
        }
    }

    /**
     * Advanced method to fetch transaction user tags by custom SQL query from the cache.
     *
     * @param query Custom query which fetches transaction user tags from the cache.
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of LiveData<Resource<List<TransactionTag>>> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactionUserTags(query: SimpleSQLiteQuery): LiveData<Resource<List<TransactionTag>>> {
        return Transformations.map(db.userTags().loadByQuery(query)) { models ->
            Resource.success(models)
        }
    }

    /**
     * Refresh all transaction user tags from the host
     *
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshTransactionUserTags(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchUserTags().enqueue { resource ->
            when (resource.status) {
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

    // Transaction Suggested Tags

    /**
     * Fetch all suggested tags for transactions from server. Tags can be filtered, sorted and ordered based on the parameters provided.
     *
     * @param searchTerm the search term to filter the tags on. (Optional)
     * @param sortBy Sort type for sorting the results. See [SuggestedTagsSortType] for more details.(Optional)
     * @param orderBy Order type for ordering the results. See [OrderType] for more details.(Optional)
     * @param completion Completion handler with optional error if the request fails and list of transaction tags if succeeds
     */
    fun fetchTransactionSuggestedTags(
        searchTerm: String? = null,
        sortBy: SuggestedTagsSortType? = SuggestedTagsSortType.NAME,
        orderBy: OrderType? = OrderType.ASC,
        completion: OnFrolloSDKCompletionListener<Resource<List<TransactionTag>>>
    ) {
        aggregationAPI.fetchSuggestedTags(searchTerm, sortBy.toString(), orderBy.toString()).enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    val tagsResource = resource.map { data -> data?.map { it.toTransactionTag() }?.toList() }
                    completion.invoke(tagsResource)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#fetchTransactionSuggestedTags", resource.error?.localizedDescription)
                    completion.invoke(Resource.error(resource.error))
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
     * @param defaultBudgetCategory Filter by the default budget category associated with the transaction category (Optional)
     * @param type Filter by type of category (Optional)
     *
     * @return LiveData object of Resource<List<TransactionCategory>> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactionCategories(
        defaultBudgetCategory: BudgetCategory? = null,
        type: TransactionCategoryType? = null
    ): LiveData<Resource<List<TransactionCategory>>> =
        Transformations.map(db.transactionCategories().loadByQuery(sqlForTransactionCategories(defaultBudgetCategory, type))) { models ->
            Resource.success(models)
        }

    /**
     * Advanced method to fetch transaction categories by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches transaction categories from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<TransactionCategory>> which can be observed using an Observer for future changes as well.
     */
    fun fetchTransactionCategories(query: SimpleSQLiteQuery): LiveData<Resource<List<TransactionCategory>>> =
        Transformations.map(db.transactionCategories().loadByQuery(query)) { model ->
            Resource.success(model)
        }

    /**
     * Refresh all transaction categories from the host
     *
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshTransactionCategories(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchTransactionCategories().enqueue { resource ->
            when (resource.status) {
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
     * @param type Filter merchants by the type (Optional)
     *
     * @return LiveData object of Resource<List<Merchant>> which can be observed using an Observer for future changes as well.
     */
    fun fetchMerchants(type: MerchantType? = null): LiveData<Resource<List<Merchant>>> =
        Transformations.map(db.merchants().loadByQuery(sqlForMerchants(type))) { models ->
            Resource.success(models)
        }

    /**
     * Advanced method to fetch merchants by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches merchants from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<Merchant>> which can be observed using an Observer for future changes as well.
     */
    fun fetchMerchants(query: SimpleSQLiteQuery): LiveData<Resource<List<Merchant>>> =
        Transformations.map(db.merchants().loadByQuery(query)) { model ->
            Resource.success(model)
        }

    /**
     * Refresh a specific merchant by ID from the host
     *
     * @param merchantId ID of the merchant to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshMerchant(merchantId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        aggregationAPI.fetchMerchant(merchantId).enqueue { resource ->
            when (resource.status) {
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
     * Note: SDK takes care of the pagination
     *
     * @param merchantIds IDs of the merchants to fetch
     * @param batchSize Batch size of merchants to returned by API (optional); Defaults to 500
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshMerchantsByIds(
        merchantIds: LongArray,
        batchSize: Long? = MERCHANT_BATCH_SIZE.toLong(),
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        if (merchantIds.isEmpty()) {
            completion?.invoke(Result.success())
            return
        }

        doAsync {
            refreshNextMerchantsByIds(merchantIds = merchantIds, batchSize = batchSize, completion = completion)
        }
    }

    private fun refreshNextMerchantsByIds(
        merchantIds: LongArray,
        after: Long? = null,
        batchSize: Long? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        aggregationAPI.fetchMerchants(merchantIds = merchantIds, after = after, size = batchSize).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshMerchantsByIds", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    val response = resource.data
                    response?.let {
                        handleMerchantsResponseByIds(response.data)

                        response.paging.cursors?.after?.let { newAfter ->
                            refreshNextMerchantsByIds(
                                merchantIds = merchantIds,
                                after = newAfter.toLong(),
                                batchSize = batchSize,
                                completion = completion
                            )
                        } ?: run {
                            // Invoke completion callback when after is returned null
                            // which indicats that we have completed paginating
                            completion?.invoke(Result.success())
                        }
                    } ?: run {
                        // Explicitly invoke completion callback if response is null.
                        completion?.invoke(Result.success())
                    }
                }
            }
        }
    }

    /**
     * Refresh all merchants by IDs from the host with pagination
     *
     * @param merchantIds IDs of the merchants to fetch
     * @param before Merchant ID to fetch before this merchant (optional)
     * @param after Merchant ID to fetch upto this merchant (optional)
     * @param batchSize Batch size of merchants to returned by API (optional); Defaults to 500
     * @param completion Optional completion handler with optional error if the request fails and pagination cursors if success
     */
    fun refreshMerchantsByIdsWithPagination(
        merchantIds: LongArray,
        before: Long? = null,
        after: Long? = null,
        batchSize: Long? = MERCHANT_BATCH_SIZE.toLong(),
        completion: OnFrolloSDKCompletionListener<PaginatedResult<PaginationInfo>>? = null
    ) {
        if (merchantIds.isEmpty()) {
            completion?.invoke(PaginatedResult.Success())
            return
        }

        aggregationAPI.fetchMerchants(merchantIds = merchantIds, before = before, after = after, size = batchSize).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshMerchantsByIdsWithPagination", resource.error?.localizedDescription)
                    completion?.invoke(PaginatedResult.Error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    resource.data?.let { response ->
                        doAsync {
                            handleMerchantsResponseByIds(response = response.data)

                            uiThread {
                                val paginationInfo = PaginationInfo(
                                    before = response.paging.cursors?.before?.toLong(),
                                    after = response.paging.cursors?.after?.toLong()
                                )
                                completion?.invoke(
                                    PaginatedResult.Success(paginationInfo)
                                )
                            }
                        }
                    } ?: run {
                        // Explicitly invoke completion callback if response is null.
                        completion?.invoke(PaginatedResult.Success())
                    }
                }
            }
        }
    }

    /**
     * Refresh all merchants from the host with pagination
     *
     * @param before Merchant ID to fetch before this merchant (optional)
     * @param after Merchant ID to fetch upto this merchant (optional)
     * @param batchSize Batch size of merchants to returned by API (optional); Defaults to 500
     * @param completion Optional completion handler with optional error if the request fails and pagination cursors if success
     */
    fun refreshMerchantsWithPagination(
        before: Long? = null,
        after: Long? = null,
        batchSize: Long? = MERCHANT_BATCH_SIZE.toLong(),
        completion: OnFrolloSDKCompletionListener<PaginatedResult<PaginationInfo>>? = null
    ) {

        aggregationAPI.fetchMerchants(before = before, after = after, size = batchSize).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshMerchantsWithPagination", resource.error?.localizedDescription)
                    completion?.invoke(PaginatedResult.Error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    val response = resource.data
                    handleMerchantsResponse(
                        response = response?.data,
                        before = response?.paging?.cursors?.before?.toLong(),
                        after = response?.paging?.cursors?.after?.toLong(),
                        completion = completion
                    )
                }
            }
        }
    }

    /**
     * Refresh merchant data for all cached merchants from the host
     *
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshCachedMerchants(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        val totalMerchantsCount = db.merchants().getMerchantsCount()
        if (totalMerchantsCount == 0L) {
            completion?.invoke(Result.success())
            return
        }

        refreshCachedMerchants(totalMerchantsCount, 0, completion)
    }

    private fun refreshCachedMerchants(merchantsCount: Long, offset: Int, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        val ids = db.merchants().getIdsByOffset(limit = MERCHANT_BATCH_SIZE, offset = offset)

        refreshMerchantsByIds(ids.toLongArray()) { result ->
            when (result.status) {
                Result.Status.SUCCESS -> {
                    val nextOffset = offset + MERCHANT_BATCH_SIZE

                    if (nextOffset < merchantsCount) {
                        refreshCachedMerchants(merchantsCount, nextOffset, completion)
                    } else {
                        completion?.invoke(result)
                    }
                }
                Result.Status.ERROR -> {
                    completion?.invoke(result)
                }
            }
        }
    }

    // WARNING: Do not call this method on the main thread
    private fun handleMerchantsResponseByIds(response: List<MerchantResponse>) {
        // Insert all merchants from API response
        val models = response.map { it.toMerchant() }
        db.merchants().insertAll(*models.toTypedArray())

        // Fetch IDs from API response
        val apiIds = response.map { it.merchantId }.toHashSet().sorted()

        // Remove IDs that are recently refreshed from the global tracking list of Merchants IDs being refreshed
        refreshingMerchantIDs = refreshingMerchantIDs.minus(apiIds)
    }

    private fun handleMerchantsResponse(
        response: List<MerchantResponse>?,
        before: Long?,
        after: Long?,
        completion: OnFrolloSDKCompletionListener<PaginatedResult<PaginationInfo>>? = null
    ) {
        response?.let {
            doAsync {
                // Insert all merchants from API response
                val models = response.map { it.toMerchant() }
                db.merchants().insertAll(*models.toTypedArray())

                // Fetch IDs from API response
                val apiIds = response.map { it.merchantId }.toHashSet()

                // Remove IDs that are recently refreshed from the global tracking list of Mercahnts IDs being refreshed
                refreshingMerchantIDs = refreshingMerchantIDs.minus(apiIds)

                // Get IDs from database
                val merchantIds = db.merchants().getIds(sqlForMerchantsIds(before = before, after = after)).toHashSet()

                // Get stale IDs that are not present in the API response
                val staleIds = merchantIds.minus(apiIds)

                // Delete the entries for these stale IDs from database if they exist
                if (staleIds.isNotEmpty()) {
                    db.merchants().deleteMany(staleIds.toLongArray())
                }

                uiThread {
                    val paginationInfo = PaginationInfo(before = before, after = after)
                    completion?.invoke(PaginatedResult.Success(paginationInfo))
                }
            }
        } ?: run { completion?.invoke(PaginatedResult.Success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleMerchantResponse(response: MerchantResponse?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                db.merchants().insert(response.toMerchant())

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    // Consents

    /**
     * Fetch consent by ID from the cache
     *
     * @param consentId Unique consent ID to fetch
     *
     * @return LiveData object of Resource<Consent> which can be observed using an Observer for future changes as well.
     */
    fun fetchConsent(consentId: Long): LiveData<Consent?> {
        return db.consents().load(consentId)
    }

    /**
     * Fetch consents from the cache
     *
     * @param providerId Filter by associated provider ID of the consent (optional)
     * @param providerAccountId Filter by associated provider account ID of the consent (optional)
     * @param status Filter by the status of the consent (optional)
     *
     * @return LiveData object of Resource<List<Consent> which can be observed using an Observer for future changes as well.
     */
    fun fetchConsents(
        providerId: Long? = null,
        providerAccountId: Long? = null,
        status: ConsentStatus? = null
    ): LiveData<List<Consent>> {
        return db.consents().loadByQuery(sqlForConsents(providerId = providerId, providerAccountId = providerAccountId, status = status))
    }

    /**
     * Advanced method to fetch consents by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches consents from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<Consent>> which can be observed using an Observer for future changes as well.
     */
    fun fetchConsents(query: SimpleSQLiteQuery): LiveData<List<Consent>> {
        return db.consents().loadByQuery(query)
    }

    /**
     * Fetch consent by ID from the cache along with other associated data.
     *
     * @param consentId Unique consent ID to fetch
     *
     * @return LiveData object of Resource<ConsentRelation> which can be observed using an Observer for future changes as well.
     */
    fun fetchConsentWithRelation(consentId: Long): LiveData<ConsentRelation?> {
        return db.consents().loadWithRelation(consentId)
    }

    /**
     * Fetch consents from the cache along with other associated data.
     *
     * @param providerId Filter by associated provider ID of the consent (optional)
     * @param providerAccountId Filter by associated provider account ID of the consent (optional)
     * @param status Filter by the status of the consent (optional)
     *
     * @return LiveData object of Resource<List<ConsentRelation> which can be observed using an Observer for future changes as well.
     */
    fun fetchConsentsWithRelation(
        providerId: Long? = null,
        providerAccountId: Long? = null,
        status: ConsentStatus? = null
    ): LiveData<List<ConsentRelation>> {
        return db.consents().loadByQueryWithRelation(
            sqlForConsents(providerId = providerId, providerAccountId = providerAccountId, status = status)
        )
    }

    /**
     * Advanced method to fetch consents by SQL query from the cache along with other associated data.
     *
     * @param query SimpleSQLiteQuery: Select query which fetches consents from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<ConsentRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchConsentsWithRelation(query: SimpleSQLiteQuery): LiveData<List<ConsentRelation>> {
        return db.consents().loadByQueryWithRelation(query)
    }

    /**
     * Refresh all available consents from the host.
     *
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshConsents(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        cdrAPI.fetchConsents().enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshConsents", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleConsentsResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Refresh a specific consent by ID from the host
     *
     * @param consentId ID of the consent to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshConsent(consentId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        cdrAPI.fetchConsent(consentId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshConsent", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleConsentResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Submits consent form for a specific provider
     *
     * NOTE: Use the same method to update sharing duration for a CDR Consent
     *
     * @param consentForm The form that will be submitted
     * @param completion Optional completion handler with optional error if the request fails else ID of the Consent created if success
     */
    fun submitConsent(consentForm: ConsentCreateForm, completion: OnFrolloSDKCompletionListener<Resource<Long>>? = null) {
        val request = consentForm.toConsentCreateRequest()
        cdrAPI.submitConsent(request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#submitConsent", resource.error?.localizedDescription)
                    completion?.invoke(Resource.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    // Since submitting a consent might affect other consents for the user, we need to refresh all of them
                    refreshConsents { // This call will anyways add the newly created consent to the cache. Hence no need to call handleConsentResponse.
                        when (it.status) {
                            Result.Status.SUCCESS -> {
                                completion?.invoke(Resource.success(resource.data?.consentId))
                            }
                            Result.Status.ERROR -> {
                                completion?.invoke(Resource.error(it.error))
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates consent form for a specific provider
     *
     * NOTE: Do not use this method to update sharing duration for a CDR Consent. Use [Aggregation.submitConsent] instead by passing the existing consent ID.
     *
     * @param consentId ID of the consent to be updated
     * @param consentForm The form that will be updated
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun updateConsent(consentId: Long, consentForm: ConsentUpdateForm, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        val request = consentForm.toConsentUpdateRequest()
        cdrAPI.updateConsent(consentId, request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateConsent", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleConsentResponse(resource.data, completion)
                }
            }
        }
    }

    /**
     * Withdraws a consent deleting all its data
     *
     * @param consentId ID of the consent to be withdrawn
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun withdrawConsent(consentId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        val consentForm = ConsentUpdateForm(status = ConsentUpdateForm.ConsentUpdateStatus.WITHDRAWN)
        updateConsent(consentId, consentForm, completion)
    }

    /**
     * Updates a consent sharing period for YODLEE provider ONLY
     *
     * @param consentId ID of the consent to be updated
     * @param sharingDuration sharingDuration (in seconds) of the consent that will be updated. This duration will be added to the existing value by host.
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun updateConsentSharingPeriod(
        consentId: Long,
        sharingDuration: Long,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        val consentForm = ConsentUpdateForm(sharingDuration = sharingDuration)
        updateConsent(consentId, consentForm, completion)
    }

    private fun handleConsentsResponse(response: List<ConsentResponse>?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                val models = response.map { it.toConsent() }
                db.consents().insertAll(*models.toTypedArray())

                val apiIds = models.map { it.consentId }.toList()
                val staleIds = db.consents().getStaleIds(apiIds.toLongArray())

                if (staleIds.isNotEmpty()) {
                    db.consents().deleteMany(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleConsentResponse(response: ConsentResponse?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                val model = response.toConsent()

                db.consents().insert(model)

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    // CDR Configuration

    /**
     * Fetch CDR Configuration from the cache
     *
     * @return LiveData object of Resource<CDRConfiguration> which can be observed using an Observer for future changes as well.
     */
    fun fetchCDRConfiguration(): LiveData<CDRConfiguration?> {
        return db.cdrConfiguration().load()
    }

    /**
     * Refresh CDR Configuration from the host.
     *
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshCDRConfiguration(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        cdrAPI.fetchCDRConfig().enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshCDRConfiguration", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleCDRConfigurationResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    private fun handleCDRConfigurationResponse(response: CDRConfigurationResponse?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                db.cdrConfiguration().insert(response.toCDRConfiguration())
                db.cdrConfiguration().deleteStaleIds(response.adrId)

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    // CDR Products

    /**
     * Fetch CDR products from server. CDR products can be filtered, sorted and ordered based on the parameters provided.
     *
     * @param providerId the ID of the provider to filter the products on. (Optional)
     * @param providerAccountId the ID of the provider account to filter the products on. (Optional)
     * @param accountId the ID of the account to filter the products on. (Optional)
     * @param productCategory Product Category to filter the products on. See [CDRProductCategory] for more details. (Optional)
     * @param productName Name of the product to filter the products on. (Optional)
     * @param completion Completion handler with optional error if the request fails and list of CDR Products if succeeds
     */
    fun fetchCDRProducts(
        providerId: Long? = null,
        providerAccountId: Long? = null,
        accountId: Long? = null,
        productCategory: CDRProductCategory? = null,
        productName: String? = null,
        completion: OnFrolloSDKCompletionListener<Resource<List<CDRProduct>>>
    ) {
        cdrAPI.fetchProducts(
            providerId = providerId,
            providerAccountId = providerAccountId,
            accountId = accountId,
            productCategory = productCategory,
            productName = productName
        ).enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    completion.invoke(resource)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#fetchCDRProducts", resource.error?.localizedDescription)
                    completion.invoke(resource)
                }
            }
        }
    }

    /**
     * Fetch CDR product by ID from the server
     *
     * @param productId the ID of the product to filter the products on. (Optional)
     * @param completion Completion handler with optional error if the request fails and CDR Product if succeeds
     */
    fun fetchCDRProduct(productId: Long, completion: OnFrolloSDKCompletionListener<Resource<CDRProduct>>) {
        cdrAPI.fetchProduct(productId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    completion.invoke(resource)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#fetchCDRProduct", resource.error?.localizedDescription)
                    completion.invoke(resource)
                }
            }
        }
    }

    // Internal methods

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
                refreshMerchantsByIds(missingMerchantIds.toLongArray())
            }
        }
    }

    // WARNING: Do not call this method on the main thread
    private fun removeCachedProviders(providerIds: LongArray) {
        if (providerIds.isNotEmpty()) {
            db.providers().deleteMany(providerIds)

            // Manually delete other data linked to this provider
            // as we are not using ForeignKeys because ForeignKey constraints
            // do not allow to insert data into child table prior to parent table
            val providerAccountIds = db.providerAccounts().getIdsByProviderIds(providerIds)
            removeCachedProviderAccounts(providerAccountIds)
        }
    }

    // WARNING: Do not call this method on the main thread
    private fun removeCachedProviderAccounts(providerAccountIds: LongArray) {
        if (providerAccountIds.isNotEmpty()) {
            db.providerAccounts().deleteMany(providerAccountIds)

            // Manually delete other data linked to this provider account
            // as we are not using ForeignKeys because ForeignKey constraints
            // do not allow to insert data into child table prior to parent table
            val accountIds = db.accounts().getIdsByProviderAccountIds(providerAccountIds)
            removeCachedAccounts(accountIds)
        }
    }

    // WARNING: Do not call this method on the main thread
    private fun removeCachedAccounts(accountIds: LongArray) {
        if (accountIds.isNotEmpty()) {
            db.accounts().deleteMany(accountIds)

            // Manually delete other data linked to this account
            // as we are not using ForeignKeys because ForeignKey constraints
            // do not allow to insert data into child table prior to parent table
            val transactionIds = db.transactions().getIdsByAccountIds(accountIds)
            removeCachedTransactions(transactionIds)

            val goalIds = db.goals().getIdsByAccountIds(accountIds)
            removeCachedGoals(goalIds)

            val cardIds = db.cards().getIdsByAccountIds(accountIds)
            removeCachedCards(cardIds)
        }
    }

    // WARNING: Do not call this method on the main thread
    private fun removeCachedTransactions(transactionIds: LongArray) {
        val chunked = transactionIds.toList().chunked(SQLITE_MAX_VARIABLE_NUMBER)
        chunked.forEach { ids ->
            if (ids.isNotEmpty()) {
                db.transactions().deleteMany(ids.toLongArray())
            }
        }
    }

    // WARNING: Do not call this method on the main thread
    private fun removeCachedGoals(goalIds: LongArray) {
        if (goalIds.isNotEmpty()) {
            db.goals().deleteMany(goalIds)

            // Manually delete goal periods associated to this goal
            // as we are not using ForeignKeys because ForeignKey constraints
            // do not allow to insert data into child table prior to parent table
            val goalPeriodIds = db.goalPeriods().getIdsByGoalIds(goalIds)
            removeCachedGoalPeriods(goalPeriodIds)
        }
    }

    // WARNING: Do not call this method on the main thread
    private fun removeCachedGoalPeriods(goalPeriodIds: LongArray) {
        if (goalPeriodIds.isNotEmpty()) {
            db.goalPeriods().deleteMany(goalPeriodIds)
        }
    }

    // WARNING: Do not call this method on the main thread
    private fun removeCachedCards(cardIds: LongArray) {
        if (cardIds.isNotEmpty()) {
            db.cards().deleteMany(cardIds)
        }
    }
}
