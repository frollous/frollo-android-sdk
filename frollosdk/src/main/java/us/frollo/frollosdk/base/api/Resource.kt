package us.frollo.frollosdk.base.api

import us.frollo.frollosdk.data.remote.ApiResponse

class Resource<out T> private constructor(val status: Status, val data: T? = null, val message: String? = null) {
    enum class Status {
        SUCCESS, ERROR, LOADING
    }

    /**
     * Maps the [Resource] data into a new [Resource] object with new data, while copying the other properties
     */
    fun <Y> map(function: (T?) -> Y?): Resource<Y> = Resource(status, function(data), message)

    companion object {
        fun <T> success(data: T?): Resource<T> = Resource(Status.SUCCESS, data, null)
        fun <T> error(msg: String?, data: T? = null): Resource<T> = Resource(Status.ERROR, data, msg)
        fun <T> loading(data: T?): Resource<T> = Resource(Status.LOADING, data, null)

        fun <T> fromApiResponse(response: ApiResponse<T>): Resource<T> =
                if (response.isSuccessful) success(response.body)
                else error(response.errorMessage)
    }
}