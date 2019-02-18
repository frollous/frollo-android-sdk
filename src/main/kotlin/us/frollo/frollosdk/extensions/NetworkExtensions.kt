package us.frollo.frollosdk.extensions

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import us.frollo.frollosdk.data.remote.ApiResponse
import us.frollo.frollosdk.error.*
import us.frollo.frollosdk.mapping.toDataError

internal fun <T> Call<T>.enqueue(completion: (T?, FrolloSDKError?) -> Unit) {
    this.enqueue(object: Callback<T> {
        override fun onResponse(call: Call<T>?, response: Response<T>?) {
            val apiResponse = ApiResponse(response)
            if (apiResponse.isSuccessful) {
                completion.invoke(apiResponse.body, null)
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

internal fun <T> handleFailure(errorResponse: ApiResponse<T>,  t: Throwable? = null, completion: (T?, FrolloSDKError?) -> Unit) {
    val code = errorResponse.code
    val errorMsg = errorResponse.errorMessage

    val dataError = errorMsg?.toDataError()
    if (dataError != null)
        completion.invoke(null, DataError(dataError.type, dataError.subType)) // Re-create new DataError as the json converter does not has the context object
    else if (code != null)
        completion.invoke(null, APIError(code, errorMsg))
    else if (t != null)
        completion.invoke(null, NetworkError(t))
    else
        completion.invoke(null, FrolloSDKError(errorMsg))
}