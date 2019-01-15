package us.frollo.frollosdk.extensions

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import us.frollo.frollosdk.data.remote.ApiResponse
import us.frollo.frollosdk.error.APIError
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.mapping.toAPIErrorResponse
import us.frollo.frollosdk.mapping.toDataError

internal fun <T> Call<T>.enqueue(completion: (T?, FrolloSDKError?) -> Unit) {
    this.enqueue(object: Callback<T> {
        override fun onResponse(call: Call<T>?, response: Response<T>?) {
            val apiResponse = ApiResponse(response)
            if (apiResponse.isSuccessful) {
                completion.invoke(apiResponse.body, null)
            } else {
                handleFailure(apiResponse, completion)
            }
        }

        override fun onFailure(call: Call<T>?, t: Throwable?) {
            val errorResponse = ApiResponse<T>(t)
            handleFailure(errorResponse, completion)
        }
    })
}

internal fun <T> handleFailure(errorResponse: ApiResponse<T>, completion: (T?, FrolloSDKError?) -> Unit) {
    val msg = errorResponse.errorMessage

    if (msg != null) {
        val dataError = msg.toDataError()

        if (dataError != null)
            completion.invoke(null, DataError(dataError.type, dataError.subType)) // Re-create new DataError as the json converter does has the context object
        else if (msg.toAPIErrorResponse() != null)
            completion.invoke(null, APIError(errorResponse.code, msg))
        else
            completion.invoke(null, FrolloSDKError(errorResponse.errorMessage))
    } else {
        completion.invoke(null, FrolloSDKError(null))
    }
}