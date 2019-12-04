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

import us.frollo.frollosdk.model.IAdapterModel
import java.math.BigDecimal

/** Data representation of transactions group report */
data class GroupReport(

    /** Unique ID of the related object. E.g. merchant or category */
    val linkedId: String,

    /** Name of the related object (Optional) */
    val name: String,

    /** Value of the report */
    val value: BigDecimal,

    /** Indicates if the [GroupReport.value] is income or expense */
    val isIncome: Boolean,

    /** Date of the report period. Check [GroupReport.DATE_FORMAT_PATTERN] for the date format. */
    val date: String, // yyyy-MM-dd

    /** Transaction ids related to the report */
    val transactionIds: List<Long>?,

    /** Period of the report */
    val period: TransactionReportPeriod,

    /** Grouping - how the report response has been broken down */
    val grouping: ReportGrouping

) : IAdapterModel {

    companion object {

        /** Date format for dates associated with Transaction Reports */
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    }
}