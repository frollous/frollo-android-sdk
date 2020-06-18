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

package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.api.reports.AccountBalanceReportResponse
import us.frollo.frollosdk.model.api.reports.ReportsResponse
import us.frollo.frollosdk.model.api.reports.ReportsResponse.ReportResponse
import us.frollo.frollosdk.model.api.reports.ReportsResponse.ReportResponse.GroupReportResponse
import us.frollo.frollosdk.model.coredata.reports.ReportAccountBalance
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.testutils.randomBoolean
import us.frollo.frollosdk.testutils.randomNumber
import java.math.BigDecimal

internal fun testAccountBalanceReportResponseData(): AccountBalanceReportResponse {
    val accounts = listOf(
        AccountBalanceReportResponse.Report.BalanceReport(
            id = 1,
            currency = "AUD",
            value = randomNumber().toBigDecimal()
        )
    )

    val data = listOf(
        AccountBalanceReportResponse.Report(
            date = "2019-03",
            value = randomNumber().toBigDecimal(),
            accounts = accounts
        )
    )

    return AccountBalanceReportResponse(data = data)
}

internal fun testReportsResponseData(): ReportsResponse {
    return ReportsResponse(listOf(testReportResponseData()))
}

internal fun testReportResponseData(): ReportResponse {
    val groups = listOf(testGroupReportResponseData())
    return ReportResponse(
        date = "2019-03-01",
        value = randomNumber().toBigDecimal(),
        income = randomBoolean(),
        groups = groups
    )
}

internal fun testGroupReportResponseData(): GroupReportResponse {
    return GroupReportResponse(
        id = 1,
        name = "living",
        value = randomNumber().toBigDecimal(),
        income = randomBoolean(),
        transactionIds = listOf(1093435, 2959945)
    )
}

internal fun testReportAccountBalanceData(
    date: String,
    accountId: Long? = null,
    period: ReportPeriod,
    id: Long? = null,
    value: BigDecimal? = null
): ReportAccountBalance {
    val report = ReportAccountBalance(
        date = date,
        accountId = accountId ?: randomNumber().toLong(),
        currency = "AUD",
        value = value ?: BigDecimal(34.67),
        period = period
    )

    id?.let { report.reportId = it }

    return report
}
