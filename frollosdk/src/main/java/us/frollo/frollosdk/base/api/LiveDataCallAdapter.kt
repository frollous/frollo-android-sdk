package us.frollo.frollosdk.base.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import us.frollo.frollosdk.data.remote.ApiResponse
import java.lang.reflect.Type

/**
 * A Retrofit adapter that converts the [Call] into a [LiveData] of [ApiResponse].
 * @param <R>
 */
internal class LiveDataCallAdapter<R>(private val responseType: Type) : CallAdapter<R, LiveData<ApiResponse<R>>> {

    override fun adapt(call: Call<R>?): LiveData<ApiResponse<R>> {
        val liveData = MutableLiveData<ApiResponse<R>>()
        call?.enqueue(object : Callback<R> {
            override fun onResponse(call: Call<R>?, response: Response<R>?) {
                liveData.postValue(ApiResponse(response))
            }

            override fun onFailure(call: Call<R>?, t: Throwable?) {
                liveData.postValue(ApiResponse(t))
            }
        })
        return liveData
    }

    override fun responseType(): Type = responseType
}