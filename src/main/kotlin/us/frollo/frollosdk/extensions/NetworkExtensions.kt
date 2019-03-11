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

package us.frollo.frollosdk.extensions

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.network.ApiResponse
import us.frollo.frollosdk.error.*
import us.frollo.frollosdk.mapping.toDataError

internal fun <T> Call<T>.enqueue(completion: (Resource<T>) -> Unit) {
    this.enqueue(object: Callback<T> {
        override fun onResponse(call: Call<T>?, response: Response<T>?) {
            val apiResponse = ApiResponse(response)
            if (apiResponse.isSuccessful) {
                completion.invoke(Resource.success(data = apiResponse.body))
            } else {
                handleFailure(apiResponse, null, completion)
            }
        }

        override fun onFailure(call: Call<T>?, t: Throwable?) {
            val errorResponse = ApiResponse<T>(t)
            handleFailure(errorResponse, t, completion)
        }
    })
}

internal fun <T> handleFailure(errorResponse: ApiResponse<T>,  t: Throwable? = null, completion: (Resource<T>) -> Unit) {
    val code = errorResponse.code
    val errorMsg = errorResponse.errorMessage

    val dataError = errorMsg?.toDataError()
    if (dataError != null)
        completion.invoke(Resource.error(error = DataError(dataError.type, dataError.subType))) // Re-create new DataError as the json converter does not has the context object
    else if (code != null)
        completion.invoke(Resource.error(error = APIError(code, errorMsg)))
    else if (t != null)
        completion.invoke(Resource.error(error = NetworkError(t)))
    else
        completion.invoke(Resource.error(error = FrolloSDKError(errorMsg)))
}