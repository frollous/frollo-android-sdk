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

package us.frollo.frollosdk.reports

import androidx.sqlite.db.SimpleSQLiteQuery
import io.reactivex.Observable
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.extensions.sqlForFetchingAccountBalanceReports
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountType
import us.frollo.frollosdk.model.coredata.reports.ReportAccountBalanceRelation
import us.frollo.frollosdk.model.coredata.reports.ReportDateFormat
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod

// Account Balance Reports

/**
 * Fetch account balance reports from the cache
 *
 * @param fromDate Start date in the format yyyy-MM-dd to fetch reports from (inclusive). See [ReportDateFormat.DATE_PATTERN_FOR_REQUEST]
 * @param toDate End date in the format yyyy-MM-dd to fetch reports up to (inclusive). See [ReportDateFormat.DATE_PATTERN_FOR_REQUEST]
 * @param period Period that reports should be broken down by
 * @param accountId Fetch reports for a specific account ID (optional)
 * @param accountType Fetch reports for a specific account type (optional)
 *
 * @return Rx Observable object of List<ReportAccountBalanceRelation> which can be observed using an Observer for future changes as well.
 */
fun Reports.fetchAccountBalanceReportsRx(
    fromDate: String,
    toDate: String,
    period: ReportPeriod,
    accountId: Long? = null,
    accountType: AccountType? = null
): Observable<List<ReportAccountBalanceRelation>> {
    val from = fromDate.toReportDateFormat(period)
    val to = toDate.toReportDateFormat(period)

    return db.reportsAccountBalance().loadWithRelationRx(sqlForFetchingAccountBalanceReports(from, to, period, accountId, accountType))
}

/**
 * Advanced method to fetch account balance reports by SQL query from the cache
 *
 * @param query SimpleSQLiteQuery: Select query which fetches account balance reports from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<ReportAccountBalanceRelation> which can be observed using an Observer for future changes as well.
 */
fun Reports.fetchAccountBalanceReportsRx(query: SimpleSQLiteQuery): Observable<List<ReportAccountBalanceRelation>> {
    return db.reportsAccountBalance().loadWithRelationRx(query)
}
