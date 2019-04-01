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

package us.frollo.frollosdk.model.display.reports

import us.frollo.frollosdk.model.coredata.reports.ReportTransactionCurrent
import us.frollo.frollosdk.model.coredata.reports.ReportTransactionCurrentRelation

data class ReportTransactionCurrentDisplay(
        val overallReports: List<ReportTransactionCurrent>,
        val groupReports: List<ReportTransactionCurrentRelation>)

fun List<ReportTransactionCurrentRelation>.toDisplay(): ReportTransactionCurrentDisplay {
    val reports = this.filter { it.report?.linkedId == null }.mapNotNull { it.report }.toList()
    val groups = this.filter { it.report?.linkedId != null }

    return ReportTransactionCurrentDisplay(overallReports = reports, groupReports = groups)
}