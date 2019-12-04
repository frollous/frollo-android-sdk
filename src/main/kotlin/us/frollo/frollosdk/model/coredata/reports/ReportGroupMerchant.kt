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

package us.frollo.frollosdk.model.coredata.reports

import java.math.BigDecimal

/** Data representation of transactions by merchant report group */
data class ReportGroupMerchant(

    /** Unique ID of the related merchant */
    val merchantId: Long,

    /** Name of the merchant */
    val merchantName: String,

    /** Value of the report */
    val value: BigDecimal,

    /** Indicates if the [ReportGroupMerchant.value] is income or expense */
    val isIncome: Boolean,

    /** Date of the report period. Check [ReportDateFormat] for the date formats. */
    val date: String, // daily yyyy-MM-dd, monthly yyyy-MM, weekly yyyy-MM-W

    /** Transaction ids related to the report */
    val transactionIds: List<Long>?,

    /** Period of the report */
    val period: ReportPeriod

) : ReportGroup()