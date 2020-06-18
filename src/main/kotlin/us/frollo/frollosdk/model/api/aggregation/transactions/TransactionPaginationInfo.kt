/*
 * Copyright 2020 Frollo
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

package us.frollo.frollosdk.model.api.aggregation.transactions

import us.frollo.frollosdk.model.api.shared.Paging

/** Model with information of transaction Pagination cursors and total count, ids and dates **/
data class TransactionPaginationInfo(
    val paging: Paging,
    val beforeId: Long?,
    val afterId: Long?,
    val beforeDate: String?,
    val afterDate: String?,
    val before: String? = null,
    val after: String? = null,
    val total: Long? = null
)
