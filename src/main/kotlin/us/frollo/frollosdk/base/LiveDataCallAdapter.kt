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

package us.frollo.frollosdk.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import us.frollo.frollosdk.network.ApiResponse
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