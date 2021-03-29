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
 * A value that represents either a success (with before and after cursors for pagination and data) or a failure, including an associated error on failure.
 */
sealed class PaginatedResultWithData<out PI, out D> {

    /**
     * Indicates data fetched successfully with pagination info
     *
     * @param paginationInfo: Pagination info usually with cursors and total count (optional)
     * @param data: Fetched data (optional)
     */
    class Success<PI, D>(
        val paginationInfo: PI? = null,
        val data: D? = null,
    ) : PaginatedResultWithData<PI, D>()

    /**
     * Indicates error while fetching data
     *
     * @param error: Error details
     */
    class Error(
        val error: FrolloSDKError? = null
    ) : PaginatedResultWithData<Nothing, Nothing>()
}
