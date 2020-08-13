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

import androidx.sqlite.db.SimpleSQLiteQuery
import io.reactivex.Observable
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.extensions.sqlForAccounts
import us.frollo.frollosdk.extensions.sqlForMerchants
import us.frollo.frollosdk.extensions.sqlForProviderAccounts
import us.frollo.frollosdk.extensions.sqlForProviders
import us.frollo.frollosdk.extensions.sqlForTransactionCategories
import us.frollo.frollosdk.extensions.sqlForTransactions
import us.frollo.frollosdk.extensions.sqlForUserTags
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
import us.frollo.frollosdk.model.coredata.aggregation.providers.Provider
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderRelation
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderStatus
import us.frollo.frollosdk.model.coredata.aggregation.tags.TagsSortType
import us.frollo.frollosdk.model.coredata.aggregation.tags.TransactionTag
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategory
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategoryType
import us.frollo.frollosdk.model.coredata.aggregation.transactions.Transaction
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionFilter
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionRelation
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.model.coredata.shared.OrderType

// Provider

/**
 * Fetch provider by ID from the cache
 *
 * @param providerId Unique provider ID to fetch
 *
 * @return Rx Observable object of Provider which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchProviderRx(providerId: Long): Observable<Provider?> {
    return db.providers().loadRx(providerId)
}

/**
 * Fetch providers from the cache
 *
 * @param status Filter by status of the provider support (Optional)
 *
 * @return Rx Observable object of List<Provider> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchProvidersRx(status: ProviderStatus? = null): Observable<List<Provider>> {
    return db.providers().loadByQueryRx(sqlForProviders(status = status))
}

/**
 * Advanced method to fetch providers by SQL query from the cache
 *
 * @param query SimpleSQLiteQuery: Select query which fetches providers from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<Provider> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchProvidersRx(query: SimpleSQLiteQuery): Observable<List<Provider>> {
    return db.providers().loadByQueryRx(query)
}

/**
 * Fetch provider by ID from the cache along with other associated data.
 *
 * @param providerId Unique provider ID to fetch
 *
 * @return Rx Observable object of ProviderRelation which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchProviderWithRelationRx(providerId: Long): Observable<ProviderRelation?> {
    return db.providers().loadWithRelationRx(providerId)
}

/**
 * Fetch providers from the cache along with other associated data.
 *
 * @param status Filter by status of the provider support (Optional)
 *
 * @return Rx Observable object of List<ProviderRelation> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchProvidersWithRelationRx(status: ProviderStatus? = null): Observable<List<ProviderRelation>> {
    return db.providers().loadByQueryWithRelationRx(sqlForProviders(status = status))
}

/**
 * Advanced method to fetch providers by SQL query from the cache along with other associated data.
 *
 * @param query SimpleSQLiteQuery: Select query which fetches providers from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<ProviderRelation> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchProvidersWithRelationRx(query: SimpleSQLiteQuery): Observable<List<ProviderRelation>> {
    return db.providers().loadByQueryWithRelationRx(query)
}

// Provider Account

/**
 * Fetch provider account by ID from the cache
 *
 * @param providerAccountId Unique ID of the provider account to fetch
 *
 * @return Rx Observable object of ProviderAccount which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchProviderAccountRx(providerAccountId: Long): Observable<ProviderAccount?> {
    return db.providerAccounts().loadRx(providerAccountId)
}

/**
 * Fetch provider accounts from the cache
 *
 * @param providerId Provider ID of the provider accounts to fetch (Optional)
 * @param refreshStatus Filter by the current refresh status of the provider account (Optional)
 * @param externalId External aggregator ID of the provider accounts to fetch (Optional)
 *
 * @return Rx Observable object of List<ProviderAccount> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchProviderAccountsRx(providerId: Long? = null, refreshStatus: AccountRefreshStatus? = null, externalId: String? = null): Observable<List<ProviderAccount>> {
    return db.providerAccounts().loadByQueryRx(sqlForProviderAccounts(providerId, refreshStatus, externalId))
}
/**
 * Advanced method to fetch provider accounts by SQL query from the cache
 *
 * @param query SimpleSQLiteQuery: Select query which fetches provider accounts from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<ProviderAccount> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchProviderAccountsRx(query: SimpleSQLiteQuery): Observable<List<ProviderAccount>> {
    return db.providerAccounts().loadByQueryRx(query)
}

/**
 * Fetch provider account by ID from the cache along with other associated data.
 *
 * @param providerAccountId Unique provider account ID to fetch
 *
 * @return Rx Observable object of ProviderAccountRelation which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchProviderAccountWithRelationRx(providerAccountId: Long): Observable<ProviderAccountRelation?> {
    return db.providerAccounts().loadWithRelationRx(providerAccountId)
}

/**
 * Fetch provider accounts from the cache along with other associated data.
 *
 * @param providerId Provider ID of the provider accounts to fetch (Optional)
 * @param refreshStatus Filter by the current refresh status of the provider account (Optional)
 * @param externalId External aggregator ID of the provider accounts to fetch (Optional)
 *
 * @return Rx Observable object of List<ProviderAccountRelation> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchProviderAccountsWithRelationRx(providerId: Long? = null, refreshStatus: AccountRefreshStatus? = null, externalId: String? = null): Observable<List<ProviderAccountRelation>> {
    return db.providerAccounts().loadByQueryWithRelationRx(sqlForProviderAccounts(providerId, refreshStatus, externalId))
}

/**
 * Advanced method to fetch provider accounts by SQL query from the cache along with other associated data.
 *
 * @param query SimpleSQLiteQuery: Select query which fetches provider accounts from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<ProviderAccountRelation> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchProviderAccountsWithRelationRx(query: SimpleSQLiteQuery): Observable<List<ProviderAccountRelation>> {
    return db.providerAccounts().loadByQueryWithRelationRx(query)
}

// Account

/**
 * Fetch account by ID from the cache
 *
 * @param accountId Unique ID of the account to fetch
 *
 * @return Rx Observable object of Account which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchAccountRx(accountId: Long): Observable<Account?> {
    return db.accounts().loadRx(accountId)
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
 * @return Rx Observable object of List<Account> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchAccountsRx(
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
): Observable<List<Account>> {
    return db.accounts().loadByQueryRx(
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
}
/**
 * Advanced method to fetch accounts by SQL query from the cache
 *
 * @param query SimpleSQLiteQuery: Select query which fetches accounts from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<Account> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchAccountsRx(query: SimpleSQLiteQuery): Observable<List<Account>> {
    return db.accounts().loadByQueryRx(query)
}

/**
 * Fetch account by ID from the cache along with other associated data.
 *
 * @param accountId Unique provider account ID to fetch
 *
 * @return Rx Observable object of AccountRelation which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchAccountWithRelationRx(accountId: Long): Observable<AccountRelation?> {
    return db.accounts().loadWithRelationRx(accountId)
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
 * @return Rx Observable object of List<AccountRelation> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchAccountsWithRelationRx(
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
): Observable<List<AccountRelation>> {
    return db.accounts().loadByQueryWithRelationRx(
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
}

/**
 * Advanced method to fetch accounts by SQL query from the cache along with other associated data.
 *
 * @param query SimpleSQLiteQuery: Select query which fetches accounts from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<AccountRelation> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchAccountsWithRelationRx(query: SimpleSQLiteQuery): Observable<List<AccountRelation>> {
    return db.accounts().loadByQueryWithRelationRx(query)
}

// Transaction

/**
 * Fetch transaction by ID from the cache
 *
 * @param transactionId Unique ID of the transaction to fetch
 *
 * @return Rx Observable object of Transaction which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchTransactionRx(transactionId: Long): Observable<Transaction?> {
    return db.transactions().loadRx(transactionId)
}

/**
 * Fetch transactions from the cache
 *
 * @param transactionFilter [TransactionFilter] object to apply filters (Optional)
 *
 * @return Rx Observable object of List<Transaction> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchTransactionsRx(transactionFilter: TransactionFilter? = null): Observable<List<Transaction>> {
    return db.transactions().loadByQueryRx(sqlForTransactions(transactionFilter))
}

/**
 * Advanced method to fetch transactions by SQL query from the cache
 *
 * @param query SimpleSQLiteQuery: Select query which fetches transactions from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<Transaction> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchTransactionsRx(query: SimpleSQLiteQuery): Observable<List<Transaction>> {
    return db.transactions().loadByQueryRx(query)
}

/**
 * Fetch transaction by ID from the cache along with other associated data.
 *
 * @param transactionId Unique transaction ID to fetch
 *
 * @return Rx Observable object of TransactionRelation which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchTransactionWithRelationRx(transactionId: Long): Observable<TransactionRelation?> {
    return db.transactions().loadWithRelationRx(transactionId)
}

/**
 * Fetch transactions from the cache along with other associated data
 *
 * @param transactionFilter [TransactionFilter] object to apply filters (Optional)
 *
 * @return Rx Observable object of List<Transaction> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchTransactionsWithRelationRx(transactionFilter: TransactionFilter? = null): Observable<List<TransactionRelation>> {
    return db.transactions().loadByQueryWithRelationRx(sqlForTransactions(transactionFilter))
}

/**
 * Advanced method to fetch transactions by SQL query from the cache with other associated data.
 *
 * @param query SimpleSQLiteQuery: Select query which fetches transactions from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<TransactionRelation> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchTransactionsWithRelationRx(query: SimpleSQLiteQuery): Observable<List<TransactionRelation>> {
    return db.transactions().loadByQueryWithRelationRx(query)
}

// Transaction User Tags

/**
 * Fetch all user tags for transactions from cache. Tags can be filtered, sorted and ordered based on the parameters provided.
 *
 * @param searchTerm the search term to filter the tags on. (Optional)
 * @param sortBy Sort type for sorting the results. See [TagsSortType] for more details.(Optional)
 * @param orderBy Order type for ordering the results. See [OrderType] for more details.(Optional)
 *
 * @return Rx Observable object of List<TransactionTag> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchTransactionUserTagsRx(searchTerm: String? = null, sortBy: TagsSortType? = null, orderBy: OrderType? = null): Observable<List<TransactionTag>> {
    return db.userTags().loadByQueryRx(sqlForUserTags(searchTerm, sortBy, orderBy))
}

/**
 * Advanced method to fetch transaction user tags by custom SQL query from the cache.
 *
 * @param query Custom query which fetches transaction user tags from the cache.
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<TransactionTag> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchTransactionUserTagsRx(query: SimpleSQLiteQuery): Observable<List<TransactionTag>> {
    return db.userTags().loadByQueryRx(query)
}

// Transaction Category

/**
 * Fetch transaction category by ID from the cache
 *
 * @param transactionCategoryId Unique ID of the transaction category to fetch
 *
 * @return Rx Observable object of TransactionCategory which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchTransactionCategoryRx(transactionCategoryId: Long): Observable<TransactionCategory?> {
    return db.transactionCategories().loadRx(transactionCategoryId)
}

/**
 * Fetch transaction categories from the cache
 *
 * @param defaultBudgetCategory Filter by the default budget category associated with the transaction category (Optional)
 * @param type Filter by type of category (Optional)
 *
 * @return Rx Observable object of List<TransactionCategory> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchTransactionCategoriesRx(defaultBudgetCategory: BudgetCategory? = null, type: TransactionCategoryType? = null): Observable<List<TransactionCategory>> {
    return db.transactionCategories().loadByQueryRx(sqlForTransactionCategories(defaultBudgetCategory, type))
}

/**
 * Advanced method to fetch transaction categories by SQL query from the cache
 *
 * @param query SimpleSQLiteQuery: Select query which fetches transaction categories from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<TransactionCategory> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchTransactionCategoriesRx(query: SimpleSQLiteQuery): Observable<List<TransactionCategory>> {
    return db.transactionCategories().loadByQueryRx(query)
}

// Merchant

/**
 * Fetch merchant by ID from the cache
 *
 * @param merchantId Unique ID of the merchant to fetch
 *
 * @return Rx Observable object of Merchant which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchMerchantRx(merchantId: Long): Observable<Merchant?> {
    return db.merchants().loadRx(merchantId)
}

/**
 * Fetch merchants from the cache
 *
 * @param type Filter merchants by the type (Optional)
 *
 * @return Rx Observable object of List<Merchant> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchMerchantsRx(type: MerchantType? = null): Observable<List<Merchant>> {
    return db.merchants().loadByQueryRx(sqlForMerchants(type))
}

/**
 * Advanced method to fetch merchants by SQL query from the cache
 *
 * @param query SimpleSQLiteQuery: Select query which fetches merchants from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<Merchant> which can be observed using an Observer for future changes as well.
 */
fun Aggregation.fetchMerchantsRx(query: SimpleSQLiteQuery): Observable<List<Merchant>> {
    return db.merchants().loadByQueryRx(query)
}
