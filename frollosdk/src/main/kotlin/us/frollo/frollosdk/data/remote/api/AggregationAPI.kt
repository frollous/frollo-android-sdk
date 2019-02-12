package us.frollo.frollosdk.data.remote.api

import retrofit2.Call
import retrofit2.http.*
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.API_VERSION_PATH
import us.frollo.frollosdk.model.api.aggregation.providers.ProviderResponse

internal interface AggregationAPI {
    companion object {
        const val URL_PROVIDERS = "$API_VERSION_PATH/aggregation/providers/"
        const val URL_PROVIDER = "$API_VERSION_PATH/aggregation/providers/{provider_id}"
    }

    @GET(URL_PROVIDERS)
    fun fetchProviders(): Call<List<ProviderResponse>>

    @GET(URL_PROVIDER)
    fun fetchProvider(@Path("provider_id") id: Long): Call<ProviderResponse>
}