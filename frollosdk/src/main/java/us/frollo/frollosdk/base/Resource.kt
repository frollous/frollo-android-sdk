package us.frollo.frollosdk.base

import us.frollo.frollosdk.data.remote.ApiResponse
import us.frollo.frollosdk.error.APIError
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.mapping.toAPIErrorResponse
import us.frollo.frollosdk.mapping.toDataError

class Resource<out T> private constructor(val status: Status, val data: T? = null, val error: FrolloSDKError? = null) {
    enum class Status {
        SUCCESS, ERROR, LOADING
    }

    /**
     * Maps the [Resource] data into a new [Resource] object with new data, while copying the other properties
     */
    fun <Y> map(function: (T?) -> Y?): Resource<Y> = Resource(status, function(data), error)

    companion object {
        internal fun <T> success(data: T?): Resource<T> = Resource(Status.SUCCESS, data, null)
        internal fun <T> error(error: FrolloSDKError?, data: T? = null): Resource<T> = Resource(Status.ERROR, data, error)
        internal fun <T> loading(data: T?): Resource<T> = Resource(Status.LOADING, data, null)

        internal fun <T> fromApiResponse(response: ApiResponse<T>): Resource<T> =
                if (response.isSuccessful) success(response.body)
                else {
                    val errorMsg = response.errorMessage

                    if (errorMsg != null) {
                        val dataError = errorMsg.toDataError()

                        if (dataError != null)
                            error(DataError(dataError.type, dataError.subType)) // Re-create new DataError as the json converter does not has the context object)
                        else
                            error(APIError(response.code, errorMsg))
                    } else {
                        error(FrolloSDKError(null))
                    }
                }
    }
}