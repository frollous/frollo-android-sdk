package us.frollo.frollosdk.data.remote

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.base.api.LiveDataCallAdapterFactory
import us.frollo.frollosdk.core.SystemInfo
import java.util.concurrent.TimeUnit

class NetworkService(si: SystemInfo) : IApiProvider {
    //Have a local instance instead of DI because this class should be used exclusively within NetworkService
    //and should have a single instance per service instance
    private val serviceHelper = NetworkServiceHelper(si)
    private val retrofit = createRetrofit()

    private fun createRetrofit(): Retrofit {
        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .enableComplexMapKeySerialization()
                .create()

        val httpClient = OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .addInterceptor(NetworkInterceptor(serviceHelper))
                .build()

        val builder = Retrofit.Builder()
                .client(httpClient)
                .baseUrl(FrolloSDK.serverUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(LiveDataCallAdapterFactory)

        return builder.build()
    }

    override fun <T> create(service: Class<T>): T = retrofit.create(service)
}