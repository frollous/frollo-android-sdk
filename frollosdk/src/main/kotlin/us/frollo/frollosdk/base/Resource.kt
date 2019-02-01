package us.frollo.frollosdk.base

import us.frollo.frollosdk.data.remote.ApiResponse
import us.frollo.frollosdk.error.APIError
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.mapping.toDataError

/**
 * Wrapper class around the data with different fetch states
 */
class Resource<out T> private constructor(
        /**
         * Status of the fetch result
         */
        val status: Status,
        /**
         * Fetched data. null if state is [Status.ERROR]
         */
        val data: T? = null,
        /**
         * Error details if state is [Status.ERROR]
         */
        val error: FrolloSDKError? = null) {

    /**
     * Enum of fetch result states
     */
    enum class Status {
        /**
         * Indicates data fetched successfully.
         */
        SUCCESS,
        /**
         * Indicates error while fetching data.
         */
        ERROR,
        /**
         * Indicates data fetch is still under progress
         */
        LOADING
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
                    val code = response.code
                    val errorMsg = response.errorMessage

                    val dataError = errorMsg?.toDataError()
                    if (dataError != null)
                        error(DataError(dataError.type, dataError.subType)) // Re-create new DataError as the json converter does not has the context object)
                    else if (code != null)
                        error(APIError(code, errorMsg))
                    else
                        error(FrolloSDKError(errorMsg))
                }
    }
}