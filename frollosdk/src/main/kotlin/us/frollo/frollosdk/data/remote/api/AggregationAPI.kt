package us.frollo.frollosdk.data.remote.api

import retrofit2.Call
import retrofit2.http.*
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.API_VERSION_PATH
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountCreateRequest
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountResponse
import us.frollo.frollosdk.model.api.aggregation.provideraccounts.ProviderAccountUpdateRequest
import us.frollo.frollosdk.model.api.aggregation.providers.ProviderResponse

internal interface AggregationAPI {
    companion object {
        // Provider URLs
        const val URL_PROVIDERS = "$API_VERSION_PATH/aggregation/providers/"
        const val URL_PROVIDER = "$API_VERSION_PATH/aggregation/providers/{provider_id}"

        // Provider Account URLs
        const val URL_PROVIDER_ACCOUNTS = "$API_VERSION_PATH/aggregation/provideraccounts/"
        const val URL_PROVIDER_ACCOUNT = "$API_VERSION_PATH/aggregation/provideraccounts/{provider_account_id}"
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
}