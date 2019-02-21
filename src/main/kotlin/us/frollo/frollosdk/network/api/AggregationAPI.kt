package us.frollo.frollosdk.network.api

import retrofit2.Call
import retrofit2.http.*
import us.frollo.frollosdk.network.NetworkHelper.Companion.API_VERSION_PATH
import us.frollo.frollosdk.model.api.aggregation.accounts.AccountResponse
import us.frollo.frollosdk.model.api.aggregation.accounts.AccountUpdateRequest
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountCreateRequest
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountResponse
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountUpdateRequest
import us.frollo.frollosdk.model.api.aggregation.providers.ProviderResponse
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionResponse
import us.frollo.frollosdk.model.api.aggregation.transactions.TransactionUpdateRequest

internal interface AggregationAPI {
    companion object {
        // Provider URLs
        const val URL_PROVIDERS = "$API_VERSION_PATH/aggregation/providers/"
        const val URL_PROVIDER = "$API_VERSION_PATH/aggregation/providers/{provider_id}"

        // Provider Account URLs
        const val URL_PROVIDER_ACCOUNTS = "$API_VERSION_PATH/aggregation/provideraccounts/"
        const val URL_PROVIDER_ACCOUNT = "$API_VERSION_PATH/aggregation/provideraccounts/{provider_account_id}"

        // Account URLs
        const val URL_ACCOUNTS = "$API_VERSION_PATH/aggregation/accounts/"
        const val URL_ACCOUNT = "$API_VERSION_PATH/aggregation/accounts/{account_id}"

        // Transaction URLs
        const val URL_TRANSACTIONS = "$API_VERSION_PATH/aggregation/transactions/"
        const val URL_TRANSACTION = "$API_VERSION_PATH/aggregation/transactions/{transaction_id}"
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

    // Note: Query parameters for this call may also contain
    // {transaction_ids, account_ids, from_date, to_date, account_included, transaction_included, skip, count}
    @GET(URL_TRANSACTIONS)
    fun fetchTransactions(@QueryMap queryParams: Map<String, String>): Call<List<TransactionResponse>>

    @GET(URL_TRANSACTION)
    fun fetchTransaction(@Path("transaction_id") transactionId: Long): Call<TransactionResponse>

    @PUT(URL_TRANSACTION)
    fun updateTransaction(@Path("transaction_id") transactionId: Long, @Body request: TransactionUpdateRequest): Call<TransactionResponse>
}