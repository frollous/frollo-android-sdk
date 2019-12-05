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

import com.jraska.livedata.test
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.BaseAndroidTest
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountType
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.coredata.reports.TransactionReportPeriod
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.model.testReportAccountBalanceData
import us.frollo.frollosdk.network.api.AggregationAPI
import us.frollo.frollosdk.network.api.ReportsAPI
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import us.frollo.frollosdk.testutils.wait
import java.math.BigDecimal

class ReportsTest : BaseAndroidTest() {

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
    }

    // Account Balance Reports Tests

    @Test
    fun testFetchAccountBalanceReports() {
        initSetup()

        val data1 = testReportAccountBalanceData(date = "2018-02-01", period = ReportPeriod.DAY)
        val data2 = testReportAccountBalanceData(date = "2018-01", period = ReportPeriod.MONTH)
        val data3 = testReportAccountBalanceData(date = "2018-01-01", period = ReportPeriod.DAY)
        val data4 = testReportAccountBalanceData(date = "2018-01-01", period = ReportPeriod.DAY)
        val list = mutableListOf(data1, data2, data3, data4)

        database.reportsAccountBalance().insert(*list.toTypedArray())

        val testObserver = reports.fetchAccountBalanceReports(fromDate = "2017-06-01", toDate = "2018-01-31", period = ReportPeriod.DAY).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchingAccountBalanceReportsFailsDateFormat() {
        initSetup()

        try {
            reports.fetchAccountBalanceReports(fromDate = "2017-06", toDate = "2018-01", period = ReportPeriod.DAY)
        } catch (e: FrolloSDKError) {
            assertEquals("Invalid format for from/to date", e.localizedMessage)
        }

        try {
            reports.refreshAccountBalanceReports(fromDate = "2017-06", toDate = "2018-01", period = ReportPeriod.DAY)
        } catch (e: FrolloSDKError) {
            assertEquals("Invalid format for from/to date", e.localizedMessage)
        }

        tearDown()
    }

    @Test
    fun testFetchingAccountBalanceReportsFailsIfLoggedOut() {
        initSetup()

        clearLoggedInPreferences()

        reports.refreshAccountBalanceReports(period = ReportPeriod.DAY, fromDate = "2018-10-28", toDate = "2019-01-29") { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchingAccountBalanceReportsByDay() {
        initSetup()

        val fromDate = "2018-10-28"
        val toDate = "2019-01-29"
        val period = ReportPeriod.DAY
        val requestPath = "${ReportsAPI.URL_REPORT_ACCOUNT_BALANCE}?period=$period&from_date=$fromDate&to_date=$toDate"

        val body = readStringFromJson(app, R.raw.account_balance_reports_by_day_2018_10_29_2019_01_29)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshAccountBalanceReports(period = period, fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = reports.fetchAccountBalanceReports(period = period, fromDate = fromDate, toDate = toDate).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)

            // Check for overall reports
            val fetchedReports = models?.sortedBy { it.report?.accountId }
            assertEquals(661, fetchedReports?.size)

            val report = fetchedReports?.first()
            assertEquals("2018-10-28", report?.report?.date)
            assertEquals(542L, report?.report?.accountId)
            assertEquals("AUD", report?.report?.currency)
            assertEquals(period, report?.report?.period)
            assertEquals(BigDecimal("-1191.45"), report?.report?.value)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(5)

        tearDown()
    }

    @Test
    fun testFetchingAccountBalanceReportsByMonth() {
        initSetup()

        val fromDate = "2018-10-28"
        val toDate = "2019-01-29"
        val period = ReportPeriod.MONTH
        val requestPath = "${ReportsAPI.URL_REPORT_ACCOUNT_BALANCE}?period=$period&from_date=$fromDate&to_date=$toDate"

        val body = readStringFromJson(app, R.raw.account_balance_reports_by_month_2018_10_29_2019_01_29)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshAccountBalanceReports(period = period, fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = reports.fetchAccountBalanceReports(period = period, fromDate = fromDate, toDate = toDate).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)

            // Check for overall reports
            val fetchedReports = models?.sortedBy { it.report?.accountId }
            assertEquals(31, fetchedReports?.size)

            val report = fetchedReports?.first()
            assertEquals("2018-10", report?.report?.date)
            assertEquals(542L, report?.report?.accountId)
            assertEquals("AUD", report?.report?.currency)
            assertEquals(period, report?.report?.period)
            assertEquals(BigDecimal("208.55"), report?.report?.value)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchingAccountBalanceReportsByWeek() {
        initSetup()

        val fromDate = "2018-10-28"
        val toDate = "2019-01-29"
        val period = ReportPeriod.WEEK
        val requestPath = "${ReportsAPI.URL_REPORT_ACCOUNT_BALANCE}?period=$period&from_date=$fromDate&to_date=$toDate"

        val body = readStringFromJson(app, R.raw.account_balance_reports_by_week_2018_10_29_2019_01_29)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshAccountBalanceReports(period = period, fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = reports.fetchAccountBalanceReports(period = period, fromDate = fromDate, toDate = toDate).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)

            // Check for overall reports
            val fetchedReports = models?.sortedBy { it.report?.accountId }
            assertEquals(122, fetchedReports?.size)

            val report = fetchedReports?.first()
            assertEquals("2018-10-4", report?.report?.date)
            assertEquals(542L, report?.report?.accountId)
            assertEquals("AUD", report?.report?.currency)
            assertEquals(period, report?.report?.period)
            assertEquals(BigDecimal("-1191.45"), report?.report?.value)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(5)

        tearDown()
    }

    @Test
    fun testFetchingAccountBalanceReportsByAccountID() {
        initSetup()

        val fromDate = "2018-10-28"
        val toDate = "2019-01-29"
        val period = ReportPeriod.DAY
        val accountId: Long = 937
        val requestPath = "${ReportsAPI.URL_REPORT_ACCOUNT_BALANCE}?period=$period&from_date=$fromDate&to_date=$toDate&account_id=$accountId"

        val body = readStringFromJson(app, R.raw.account_balance_reports_by_day_account_id_937_2018_10_29_2019_01_29)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshAccountBalanceReports(period = period, fromDate = fromDate, toDate = toDate, accountId = accountId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = reports.fetchAccountBalanceReports(period = period, fromDate = fromDate, toDate = toDate, accountId = accountId).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)

            // Check for overall reports
            val fetchedReports = models?.sortedBy { it.report?.accountId }
            assertEquals(94, fetchedReports?.size)

            val report = fetchedReports?.first()
            assertEquals("2018-10-28", report?.report?.date)
            assertEquals(937L, report?.report?.accountId)
            assertEquals("AUD", report?.report?.currency)
            assertEquals(period, report?.report?.period)
            assertEquals(BigDecimal("-2641.45"), report?.report?.value)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchingAccountBalanceReportsByAccountType() {
        initSetup()

        val fromDate = "2018-10-28"
        val toDate = "2019-01-29"
        val period = ReportPeriod.DAY
        val accountType = AccountType.BANK
        val requestPath = "${ReportsAPI.URL_REPORT_ACCOUNT_BALANCE}?period=$period&from_date=$fromDate&to_date=$toDate&container=$accountType"

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.account_balance_reports_by_day_container_bank_2018_10_29_2019_01_29))
                } else if (request?.trimmedPath == AggregationAPI.URL_ACCOUNTS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.accounts_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshAccounts { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(3)

        reports.refreshAccountBalanceReports(period = period, fromDate = fromDate, toDate = toDate, accountType = accountType) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = reports.fetchAccountBalanceReports(period = period, fromDate = fromDate, toDate = toDate, accountType = accountType).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)

            // Check for overall reports
            val fetchedReports = models?.sortedBy { it.report?.accountId }
            assertEquals(376, fetchedReports?.size)

            val report = fetchedReports?.last()
            assertEquals("2019-01-29", report?.report?.date)
            assertEquals(938L, report?.report?.accountId)
            assertEquals("AUD", report?.report?.currency)
            assertEquals(period, report?.report?.period)
            assertEquals(BigDecimal("42000.00"), report?.report?.value)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchingAccountBalanceReportsUpdatesExisting() {
        initSetup()

        val oldFromDate = "2018-10-28"
        val oldToDate = "2018-11-29"
        val newFromDate = "2018-10-28"
        val newToDate = "2019-02-01"
        val period = ReportPeriod.MONTH
        val requestPath1 = "${ReportsAPI.URL_REPORT_ACCOUNT_BALANCE}?period=$period&from_date=$oldFromDate&to_date=$oldToDate"
        val requestPath2 = "${ReportsAPI.URL_REPORT_ACCOUNT_BALANCE}?period=$period&from_date=$newFromDate&to_date=$newToDate"

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath1) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.account_balance_reports_by_month_2018_10_29_2019_01_29))
                } else if (request?.trimmedPath == requestPath2) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.account_balance_reports_by_month_2018_11_01_2019_02_01))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        // Fetch Old reports
        reports.refreshAccountBalanceReports(period = period, fromDate = oldFromDate, toDate = oldToDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(3)

        var testObserver = reports.fetchAccountBalanceReports(period = period, fromDate = oldFromDate, toDate = oldToDate).test()
        testObserver.awaitValue()
        var models = testObserver.value().data
        assertNotNull(models)

        // Check old reports exist
        var fetchedReports = models?.sortedBy { it.report?.accountId }?.filter { it.report?.date == "2018-10" }
        assertEquals(8, fetchedReports?.size)

        var report = fetchedReports?.first()
        assertEquals("2018-10", report?.report?.date)
        assertEquals(542L, report?.report?.accountId)
        assertEquals("AUD", report?.report?.currency)
        assertEquals(period, report?.report?.period)
        assertEquals(BigDecimal("208.55"), report?.report?.value)

        // Check new reports don't exist
        testObserver = reports.fetchAccountBalanceReports(period = period, fromDate = newFromDate, toDate = newToDate).test()
        testObserver.awaitValue()
        models = testObserver.value().data
        fetchedReports = models?.sortedBy { it.report?.accountId }?.filter { it.report?.date == "2019-02" }
        assertEquals(0, fetchedReports?.size)

        // Fetch New reports
        reports.refreshAccountBalanceReports(period = period, fromDate = newFromDate, toDate = newToDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(3)

        testObserver = reports.fetchAccountBalanceReports(period = period, fromDate = oldFromDate, toDate = oldToDate).test()
        testObserver.awaitValue()
        models = testObserver.value().data

        // Check old reports exist
        fetchedReports = models?.sortedBy { it.report?.accountId }?.filter { it.report?.date == "2018-10" }
        assertEquals(8, fetchedReports?.size)

        report = fetchedReports?.first()
        assertEquals("2018-10", report?.report?.date)
        assertEquals(542L, report?.report?.accountId)
        assertEquals("AUD", report?.report?.currency)
        assertEquals(period, report?.report?.period)
        assertEquals(BigDecimal("-1191.45"), report?.report?.value)

        testObserver = reports.fetchAccountBalanceReports(period = period, fromDate = newFromDate, toDate = newToDate).test()
        testObserver.awaitValue()
        models = testObserver.value().data

        // Check new reports exist
        fetchedReports = models?.sortedBy { it.report?.accountId }?.filter { it.report?.date == "2019-02" }
        assertEquals(7, fetchedReports?.size)

        report = fetchedReports?.first()
        assertEquals("2019-02", report?.report?.date)
        assertEquals(542L, report?.report?.accountId)
        assertEquals("AUD", report?.report?.currency)
        assertEquals(period, report?.report?.period)
        assertEquals(BigDecimal("1823.85"), report?.report?.value)

        tearDown()
    }

    @Test
    fun testFetchingAccountBalanceReportsCommingling() {
        initSetup()

        val fromDate = "2018-10-28"
        val toDate = "2019-01-29"
        val period1 = ReportPeriod.DAY
        val period2 = ReportPeriod.WEEK
        val period3 = ReportPeriod.MONTH
        val requestPath1 = "${ReportsAPI.URL_REPORT_ACCOUNT_BALANCE}?period=$period1&from_date=$fromDate&to_date=$toDate"
        val requestPath2 = "${ReportsAPI.URL_REPORT_ACCOUNT_BALANCE}?period=$period2&from_date=$fromDate&to_date=$toDate"
        val requestPath3 = "${ReportsAPI.URL_REPORT_ACCOUNT_BALANCE}?period=$period3&from_date=$fromDate&to_date=$toDate"

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath1) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.account_balance_reports_by_day_2018_10_29_2019_01_29))
                } else if (request?.trimmedPath == requestPath2) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.account_balance_reports_by_week_2018_10_29_2019_01_29))
                } else if (request?.trimmedPath == requestPath3) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.account_balance_reports_by_month_2018_10_29_2019_01_29))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        // Fetch Old reports
        reports.refreshAccountBalanceReports(period = period1, fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        reports.refreshAccountBalanceReports(period = period2, fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        reports.refreshAccountBalanceReports(period = period3, fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(5)

        // Check for day reports
        var testObserver = reports.fetchAccountBalanceReports(period = period1, fromDate = fromDate, toDate = toDate).test()
        testObserver.awaitValue()
        var models = testObserver.value().data

        var fetchedReports = models?.sortedBy { it.report?.accountId }
        assertEquals(661, fetchedReports?.size)

        var report = fetchedReports?.first()
        assertEquals("2018-10-28", report?.report?.date)
        assertEquals(542L, report?.report?.accountId)
        assertEquals("AUD", report?.report?.currency)
        assertEquals(period1, report?.report?.period)
        assertEquals(BigDecimal("-1191.45"), report?.report?.value)

        // Check for week report
        testObserver = reports.fetchAccountBalanceReports(period = period2, fromDate = fromDate, toDate = toDate).test()
        testObserver.awaitValue()
        models = testObserver.value().data

        fetchedReports = models?.sortedBy { it.report?.accountId }
        assertEquals(122, fetchedReports?.size)

        report = fetchedReports?.first()
        assertEquals("2018-10-4", report?.report?.date)
        assertEquals(542L, report?.report?.accountId)
        assertEquals("AUD", report?.report?.currency)
        assertEquals(period2, report?.report?.period)
        assertEquals(BigDecimal("-1191.45"), report?.report?.value)

        // Check for month reports
        testObserver = reports.fetchAccountBalanceReports(period = period3, fromDate = fromDate, toDate = toDate).test()
        testObserver.awaitValue()
        models = testObserver.value().data

        fetchedReports = models?.sortedBy { it.report?.accountId }
        assertEquals(31, fetchedReports?.size)

        report = fetchedReports?.first()
        assertEquals("2018-10", report?.report?.date)
        assertEquals(542L, report?.report?.accountId)
        assertEquals("AUD", report?.report?.currency)
        assertEquals(period3, report?.report?.period)
        assertEquals(BigDecimal("208.55"), report?.report?.value)

        tearDown()
    }

    // History Report Tests

    @Test
    fun testFetchMerchantReports() {
        initSetup()

        val fromDate = "2019-01-01"
        val toDate = "2019-12-31"
        val period = TransactionReportPeriod.MONTHLY
        val grouping = ReportGrouping.MERCHANT
        val requestPath = "${ReportsAPI.URL_REPORTS_MERCHANTS}?grouping=$grouping&period=$period&from_date=$fromDate&to_date=$toDate"

        val body = readStringFromJson(app, R.raw.transaction_reports_merchant_monthly_2019_01_01_2019_12_31)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.fetchMerchantReports(period = period, fromDate = fromDate, toDate = toDate) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val models = resource.data
            assertNotNull(models)

            // Check for overall reports
            val overallReports = models?.sortedBy { it.date }
            assertEquals(12, overallReports?.size)

            val report = overallReports?.get(5)!!
            assertEquals("2019-06-01", report.date)
            assertEquals(BigDecimal("721.07"), report.value)
            assertTrue(report.isIncome)
            assertNotNull(report.groups)
            assertEquals(22, report.groups.size)

            // Check for group reports
            val groupReports = overallReports.filter { it.date == "2019-07-01" }[0].groups.sortedBy { it.linkedId }
            assertEquals(22, groupReports.size)

            val groupReport = groupReports.last()
            assertEquals("2019-07-01", groupReport.date)
            assertEquals(BigDecimal("127.0"), groupReport.value)
            assertFalse(groupReport.isIncome)
            assertEquals(292L, groupReport.linkedId)
            assertEquals("SUSHI PTY. LTD.", groupReport.name)
            assertEquals(2, groupReport.transactionIds?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchBudgetCategoryReports() {
        initSetup()

        val fromDate = "2019-01-01"
        val toDate = "2019-12-31"
        val period = TransactionReportPeriod.MONTHLY
        val grouping = ReportGrouping.BUDGET_CATEGORY
        val requestPath = "${ReportsAPI.URL_REPORTS_BUDGET_CATEGORIES}?grouping=$grouping&period=$period&from_date=$fromDate&to_date=$toDate"

        val body = readStringFromJson(app, R.raw.transaction_reports_budget_category_monthly_2019_01_01_2019_12_31)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.fetchBudgetCategoryReports(period = period, fromDate = fromDate, toDate = toDate) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val models = resource.data
            assertNotNull(models)

            // Check for overall reports
            val overallReports = models?.sortedBy { it.date }
            assertEquals(12, overallReports?.size)

            val report = overallReports?.get(5)!!
            assertEquals("2019-06-01", report.date)
            assertEquals(BigDecimal("721.07"), report.value)
            assertTrue(report.isIncome)
            assertNotNull(report.groups)
            assertEquals(5, report.groups.size)

            // Check for group reports
            val groupReports = overallReports.filter { it.date == "2019-07-01" }[0].groups.sortedBy { it.linkedId }
            assertEquals(5, groupReports.size)

            val groupReport = groupReports[1]
            assertEquals("2019-07-01", groupReport.date)
            assertEquals(BigDecimal("1635.82"), groupReport.value)
            assertFalse(groupReport.isIncome)
            assertEquals(1L, groupReport.linkedId)
            assertEquals("living", groupReport.name)
            assertEquals(28, groupReport.transactionIds?.size)
            assertEquals(BudgetCategory.LIVING, BudgetCategory.getById(groupReport.linkedId))
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchTransactionCategoryReportsMonthly() {
        initSetup()

        val fromDate = "2019-01-01"
        val toDate = "2019-12-31"
        val period = TransactionReportPeriod.MONTHLY
        val grouping = ReportGrouping.TRANSACTION_CATEGORY
        val requestPath = "${ReportsAPI.URL_REPORTS_CATEGORIES}?grouping=$grouping&period=$period&from_date=$fromDate&to_date=$toDate"

        val body = readStringFromJson(app, R.raw.transaction_reports_txn_category_monthly_2019_01_01_2019_12_31)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.fetchTransactionCategoryReports(period = period, fromDate = fromDate, toDate = toDate) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val models = resource.data
            assertNotNull(models)

            // Check for overall reports
            val overallReports = models?.sortedBy { it.date }
            assertEquals(12, overallReports?.size)

            val report = overallReports?.get(5)!!
            assertEquals("2019-06-01", report.date)
            assertEquals(BigDecimal("721.07"), report.value)
            assertTrue(report.isIncome)
            assertNotNull(report.groups)
            assertEquals(15, report.groups.size)

            // Check for group reports
            val groupReports = overallReports.filter { it.date == "2019-07-01" }[0].groups.sortedBy { it.linkedId }
            assertEquals(18, groupReports.size)

            val groupReport = groupReports.last()
            assertEquals("2019-07-01", groupReport.date)
            assertEquals(BigDecimal("40.0"), groupReport.value)
            assertFalse(groupReport.isIncome)
            assertEquals(94L, groupReport.linkedId)
            assertEquals("Electronics/General Merchandise", groupReport.name)
            assertEquals(1, groupReport.transactionIds?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchTransactionCategoryReportsDaily() {
        initSetup()

        val fromDate = "2019-10-01"
        val toDate = "2019-12-31"
        val period = TransactionReportPeriod.DAILY
        val grouping = ReportGrouping.TRANSACTION_CATEGORY
        val requestPath = "${ReportsAPI.URL_REPORTS_CATEGORIES}?grouping=$grouping&period=$period&from_date=$fromDate&to_date=$toDate"

        val body = readStringFromJson(app, R.raw.transaction_reports_txn_category_daily_2019_10_01_2019_12_31)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.fetchTransactionCategoryReports(period = period, fromDate = fromDate, toDate = toDate) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val models = resource.data
            assertNotNull(models)

            // Check for overall reports
            val overallReports = models?.sortedBy { it.date }
            assertEquals(92, overallReports?.size)

            val report = overallReports?.first()!!
            assertEquals("2019-10-01", report.date)
            assertEquals(BigDecimal("44.5"), report.value)
            assertFalse(report.isIncome)
            assertNotNull(report.groups)
            assertEquals(2, report.groups.size)

            // Check for group reports
            val groupReports = overallReports.filter { it.date == "2019-10-03" }[0].groups.sortedBy { it.linkedId }
            assertEquals(3, groupReports.size)

            val groupReport = groupReports.last()
            assertEquals("2019-10-03", groupReport.date)
            assertEquals(BigDecimal("128.0"), groupReport.value)
            assertFalse(groupReport.isIncome)
            assertEquals(77L, groupReport.linkedId)
            assertEquals("Restaurants", groupReport.name)
            assertEquals(2, groupReport.transactionIds?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchTransactionCategoryReportsWeekly() {
        initSetup()

        val fromDate = "2019-01-01"
        val toDate = "2019-12-31"
        val period = TransactionReportPeriod.WEEKLY
        val grouping = ReportGrouping.TRANSACTION_CATEGORY
        val requestPath = "${ReportsAPI.URL_REPORTS_CATEGORIES}?grouping=$grouping&period=$period&from_date=$fromDate&to_date=$toDate"

        val body = readStringFromJson(app, R.raw.transaction_reports_txn_category_weekly_2019_01_01_2019_12_31)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.fetchTransactionCategoryReports(period = period, fromDate = fromDate, toDate = toDate) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val models = resource.data
            assertNotNull(models)

            // Check for overall reports
            val overallReports = models?.sortedBy { it.date }
            assertEquals(52, overallReports?.size)

            val report = overallReports?.filter { it.date == "2019-06-04" }!![0]
            assertEquals("2019-06-04", report.date)
            assertEquals(BigDecimal("472.64"), report.value)
            assertFalse(report.isIncome)
            assertNotNull(report.groups)
            assertEquals(7, report.groups.size)

            // Check for group reports
            val groupReports = overallReports.filter { it.date == "2019-06-11" }[0].groups.sortedBy { it.linkedId }
            assertEquals(8, groupReports.size)

            val groupReport = groupReports.last()
            assertEquals("2019-06-11", groupReport.date)
            assertEquals(BigDecimal("3250.0"), groupReport.value)
            assertTrue(groupReport.isIncome)
            assertEquals(84L, groupReport.linkedId)
            assertEquals("Salary/Regular Income", groupReport.name)
            assertEquals(1, groupReport.transactionIds?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchBudgetCategoryReportsGroupedByTransactionCategory() {
        // TODO: to be implemented
    }

    @Test
    fun testFetchTagReports() {
        // TODO: to be implemented
    }
}