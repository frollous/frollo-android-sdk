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
    val error: FrolloSDKError? = null
) {

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
        /**
         * Instantiate Result with status Success
         */
        fun success(): Result = Result(Status.SUCCESS)

        /**
         * Instantiate Result with status Error
         *
         * @param error Associated error conforming [FrolloSDKError]
         */
        fun error(error: FrolloSDKError?): Result = Result(Status.ERROR, error)
    }
}
