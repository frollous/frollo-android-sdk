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

package us.frollo.frollosdk.network.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap
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
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionsSummaryResponse
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionUpdateRequest

internal interface AggregationAPI {
    companion object {
        // Provider URLs
        const val URL_PROVIDERS = "aggregation/providers"
        const val URL_PROVIDER = "aggregation/providers/{provider_id}"

        // Provider Account URLs
        const val URL_PROVIDER_ACCOUNTS = "aggregation/provideraccounts"
        const val URL_PROVIDER_ACCOUNT = "aggregation/provideraccounts/{provider_account_id}"

        // Account URLs
        const val URL_ACCOUNTS = "aggregation/accounts"
        const val URL_ACCOUNT = "aggregation/accounts/{account_id}"

        // Transaction URLs
        const val URL_TRANSACTIONS = "aggregation/transactions"
        const val URL_TRANSACTION = "aggregation/transactions/{transaction_id}"
        const val URL_TRANSACTIONS_SEARCH = "aggregation/transactions/search"
        const val URL_TRANSACTIONS_SUMMARY = "aggregation/transactions/summary"

        // Tags URLs
        const val URL_TRANSACTION_TAGS = "$URL_TRANSACTIONS/{transaction_id}/tags"
        const val URL_USER_TAGS = "$URL_TRANSACTIONS/tags/user"
        const val URL_SUGGESTED_TAGS = "$URL_TRANSACTIONS/tags/suggested"

        // Transaction Category URLs
        const val URL_TRANSACTION_CATEGORIES = "aggregation/transactions/categories"

        // Merchant URLs
        const val URL_MERCHANTS = "aggregation/merchants"
        const val URL_MERCHANT = "aggregation/merchants/{merchant_id}"
    }

    // Provider API

    @GET(URL_PROVIDERS)
    fun fetchProviders(): Call<List<ProviderResponse>>

    @GET(URL_PROVIDER)
    fun fetchProvider(@Path("provider_id") id: Long): Call<ProviderResponse>

    // Provider Account API

    @GET(URL_PROVIDER_ACCOUNTS)
    fun fetchProviderAccounts(): Call<List<ProviderAccountResponse>>

    @GET(URL_PROVIDER_ACCOUNT)
    fun fetchProviderAccount(@Path("provider_account_id") providerAccId: Long): Call<ProviderAccountResponse>

    @POST(URL_PROVIDER_ACCOUNTS)
    fun createProviderAccount(@Body request: ProviderAccountCreateRequest): Call<ProviderAccountResponse>

    @PUT(URL_PROVIDER_ACCOUNT)
    fun updateProviderAccount(@Path("provider_account_id") providerAccountId: Long, @Body request: ProviderAccountUpdateRequest): Call<ProviderAccountResponse>

    @DELETE(URL_PROVIDER_ACCOUNT)
    fun deleteProviderAccount(@Path("provider_account_id") providerAccountId: Long): Call<Void>

    // Account API

    @GET(URL_ACCOUNTS)
    fun fetchAccounts(): Call<List<AccountResponse>>

    @GET(URL_ACCOUNT)
    fun fetchAccount(@Path("account_id") accountId: Long): Call<AccountResponse>

    @PUT(URL_ACCOUNT)
    fun updateAccount(@Path("account_id") accountId: Long, @Body request: AccountUpdateRequest): Call<AccountResponse>

    // Transaction API

    // Query parameters: {transaction_ids, account_ids, from_date, to_date, account_included, transaction_included, skip, count}
    @GET(URL_TRANSACTIONS)
    fun fetchTransactions(@QueryMap queryParams: Map<String, String>): Call<List<TransactionResponse>>

    @GET(URL_TRANSACTION)
    fun fetchTransaction(@Path("transaction_id") transactionId: Long): Call<TransactionResponse>

    @PUT(URL_TRANSACTION)
    fun updateTransaction(@Path("transaction_id") transactionId: Long, @Body request: TransactionUpdateRequest): Call<TransactionResponse>

    // Query parameters: {search_term, from_date, to_date, transaction_ids, account_ids, account_included, transaction_included, skip, count}
    @GET(URL_TRANSACTIONS_SEARCH)
    fun transactionSearch(@QueryMap queryParams: Map<String, String>): Call<List<TransactionResponse>>

    // Query parameters: {transaction_ids, account_ids, from_date, to_date, account_included, transaction_included}
    @GET(URL_TRANSACTIONS_SUMMARY)
    fun fetchTransactionsSummary(@QueryMap queryParams: Map<String, String>): Call<TransactionsSummaryResponse>

    // Transaction Category API

    @GET(URL_TRANSACTION_CATEGORIES)
    fun fetchTransactionCategories(): Call<List<TransactionCategoryResponse>>

    // Merchant API

    @GET(URL_MERCHANTS)
    fun fetchMerchants(): Call<List<MerchantResponse>>

    @GET(URL_MERCHANT)
    fun fetchMerchant(@Path("merchant_id") merchantId: Long): Call<MerchantResponse>

    // Query parameters: {merchant_ids}
    @GET(URL_MERCHANTS)
    fun fetchMerchantsByIds(@QueryMap queryParams: Map<String, String>): Call<List<MerchantResponse>>

    // Tags

    @GET(URL_TRANSACTION_TAGS)
    fun fetchTags(@Path("transaction_id") transactionId: Long): Call<List<TransactionTagResponse>>

    @POST(URL_TRANSACTION_TAGS)
    fun createTags(@Path("transaction_id") transactionId: Long, @Body requestArray: Array<TransactionTagUpdateRequest>): Call<Void>

    // Workaround with HTTP instead of DELETE as DELETE does not support a body
    @HTTP(method = "DELETE", path = URL_TRANSACTION_TAGS, hasBody = true)
    fun deleteTags(@Path("transaction_id") transactionId: Long, @Body requestArray: Array<TransactionTagUpdateRequest>): Call<Void>

    @GET(URL_USER_TAGS)
    fun fetchUserTags(@QueryMap queryParams: Map<String, String>): Call<List<TransactionTagResponse>>

    @GET(URL_SUGGESTED_TAGS)
    fun fetchSuggestedTags(@QueryMap queryParams: Map<String, String>): Call<List<TransactionTagResponse>>
}