package us.frollo.frollosdk.base

import us.frollo.frollosdk.error.FrolloSDKError

/**
 * A value that represents either a success or a failure, including an associated error on failure.
 */
class Result private constructor(
        /**
         * Status of the fetch result
         */
        val status: Status,
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
        ERROR
    }

    companion object {
        internal fun success(): Result = Result(Status.SUCCESS)
        internal fun error(error: FrolloSDKError?): Result = Result(Status.ERROR, error)
    }
}