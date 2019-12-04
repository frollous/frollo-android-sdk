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

/** Data representation of transactions by tag report */
data class ReportTag(

    /** Date of the report period. Check [ReportDateFormat] for the date formats. */
    val date: String, // daily yyyy-MM-dd, monthly yyyy-MM, weekly yyyy-MM-W

    /** Value of the report */
    val value: BigDecimal,

    /** Indicates if the [ReportTag.value] is income or expense */
    val isIncome: Boolean,

    /** Associated tag report groups */
    val groups: List<ReportGroupTag>,

    /** Period of the report */
    val period: ReportPeriod

) : Report()