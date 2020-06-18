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
 * A value that represents either a success (with before and after cursors for pagination) or a failure, including an associated error on failure.
 */
sealed class PaginatedResult {

    /**
     * Indicates data fetched successfully
     *
     * @param before: Before cursor (usually ID) for pagination (Optional)
     * @param after: After cursor (usually ID) for pagination (Optional)
     */
    class Success(
        val before: Long? = null,
        val after: Long? = null
    ) : PaginatedResult()

    /**
     * Indicates error while fetching data
     *
     * @param error: Error details
     */
    class Error(
        val error: FrolloSDKError? = null
    ) : PaginatedResult()
}
