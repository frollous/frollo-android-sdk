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

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.test.platform.app.InstrumentationRegistry
import com.jraska.livedata.test
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.aggregation.Aggregation
import us.frollo.frollosdk.authentication.OAuth
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.testSDKConfig
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.model.testReportTransactionCurrentData
import us.frollo.frollosdk.model.testReportTransactionHistoryData
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.AggregationAPI
import us.frollo.frollosdk.network.api.ReportsAPI
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import us.frollo.frollosdk.testutils.wait
import java.math.BigDecimal

class ReportsTest {
    @get:Rule
    val testRule = InstantTaskExecutorRule()
    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private lateinit var mockServer: MockWebServer
    private lateinit var preferences: Preferences
    private lateinit var keystore: Keystore
    private lateinit var database: SDKDatabase
    private lateinit var network: NetworkService

    private lateinit var aggregation: Aggregation
    private lateinit var reports: Reports

    private fun initSetup() {
        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url("/")

        val config = testSDKConfig(serverUrl = baseUrl.toString())
        if (!FrolloSDK.isSetup) FrolloSDK.setup(app, config) {}

        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        database = SDKDatabase.getInstance(app)
        val oAuth = OAuth(config = config)
        network = NetworkService(oAuth = oAuth, keystore = keystore, pref = preferences)

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        aggregation = Aggregation(network, database, LocalBroadcastManager.getInstance(app))

        reports = Reports(network, database, aggregation)
    }

    private fun tearDown() {
        mockServer.shutdown()
        preferences.resetAll()
        database.clearAllTables()
    }

    // Current Report Tests

    @Test
    fun testFetchCurrentTransactionReports() {
        initSetup()

        val data1 = testReportTransactionCurrentData(grouping = ReportGrouping.BUDGET_CATEGORY, day = 1)
        val data2 = testReportTransactionCurrentData(grouping = ReportGrouping.MERCHANT, day = 4)
        val data3 = testReportTransactionCurrentData(grouping = ReportGrouping.BUDGET_CATEGORY, day = 7)
        val data4 = testReportTransactionCurrentData(grouping = ReportGrouping.TRANSACTION_CATEGORY, day =25)
        val data5 = testReportTransactionCurrentData(grouping = ReportGrouping.BUDGET_CATEGORY, day = 30, budgetCategory = BudgetCategory.LIVING)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        database.reportsTransactionCurrent().insertAll(*list.toTypedArray())

        val testObserver = reports.currentTransactionReports(grouping = ReportGrouping.BUDGET_CATEGORY).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchingCurrentReportsByBudgetCategory() {
        initSetup()

        val requestPath = "${ReportsAPI.URL_REPORT_TRANSACTIONS_CURRENT}?grouping=${ReportGrouping.BUDGET_CATEGORY}"

        val body = readStringFromJson(app, R.raw.transaction_reports_current_budget_category)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshTransactionCurrentReports(grouping = ReportGrouping.BUDGET_CATEGORY) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = reports.currentTransactionReports(grouping = ReportGrouping.BUDGET_CATEGORY).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)

            // Check for overall reports
            val overallReports = models?.filter { it.report?.linkedId == null }
            assertEquals(31, overallReports?.size)
            val first = overallReports?.first()?.report
            assertEquals(1, first?.day)
            assertEquals(BigDecimal("-187.80"), first?.amount)
            assertEquals(BigDecimal("387.10"), first?.budget)
            assertEquals(BigDecimal("-120.53"), first?.average)
            assertEquals(BigDecimal("-215.80"), first?.previous)
            assertNull(first?.filteredBudgetCategory)
            assertNull(first?.linkedId)
            assertNull(first?.name)

            // Check for group reports
            val groupReports = models?.filter { it.report?.day == 3 && it.report?.linkedId != null }?.sortedBy { it.report?.linkedId }
            assertEquals(5, groupReports?.size)
            val third = groupReports?.get(2)?.report
            assertEquals(3, third?.day)
            assertEquals(BigDecimal("-89.95"), third?.amount)
            assertEquals(BigDecimal("64.52"), third?.budget)
            assertEquals(BigDecimal("-55.33"), third?.average)
            assertEquals(BigDecimal("-92.00"), third?.previous)
            assertNull(third?.filteredBudgetCategory)
            assertEquals(2L, third?.linkedId)
            assertEquals("lifestyle", third?.name)
            assertEquals(BudgetCategory.LIFESTYLE, groupReports?.get(2)?.budgetCategory)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchingCurrentReportsByMerchant() {
        initSetup()

        val requestPath = "${ReportsAPI.URL_REPORT_TRANSACTIONS_CURRENT}?grouping=${ReportGrouping.MERCHANT}"

        val body = readStringFromJson(app, R.raw.transaction_reports_current_merchant)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshTransactionCurrentReports(grouping = ReportGrouping.MERCHANT) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = reports.currentTransactionReports(grouping = ReportGrouping.MERCHANT).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)

            // Check for overall reports
            val overallReports = models?.filter { it.report?.linkedId == null }
            assertEquals(31, overallReports?.size)
            val first = overallReports?.first()?.report
            assertEquals(1, first?.day)
            assertEquals(BigDecimal("-187.80"), first?.amount)
            assertNull(first?.budget)
            assertEquals(BigDecimal("-120.53"), first?.average)
            assertEquals(BigDecimal("-215.80"), first?.previous)
            assertNull(first?.filteredBudgetCategory)
            assertNull(first?.linkedId)
            assertNull(first?.name)

            // Check for group reports
            val groupReports = models?.filter { it.report?.day == 3 && it.report?.linkedId != null }?.sortedBy { it.report?.linkedId }
            assertEquals(24, groupReports?.size)
            val second = groupReports?.get(1)?.report
            assertEquals(3, second?.day)
            assertEquals(BigDecimal("-159.41"), second?.amount)
            assertNull(second?.budget)
            assertEquals(BigDecimal("-106.27"), second?.average)
            assertEquals(BigDecimal("-159.41"), second?.previous)
            assertNull(second?.filteredBudgetCategory)
            assertEquals(2L, second?.linkedId)
            assertEquals("Woolworths", second?.name)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchingCurrentReportsByTransactionCategory() {
        initSetup()

        val requestPath = "${ReportsAPI.URL_REPORT_TRANSACTIONS_CURRENT}?grouping=${ReportGrouping.TRANSACTION_CATEGORY}"

        val body = readStringFromJson(app, R.raw.transaction_reports_current_txn_category)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshTransactionCurrentReports(grouping = ReportGrouping.TRANSACTION_CATEGORY) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = reports.currentTransactionReports(grouping = ReportGrouping.TRANSACTION_CATEGORY).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)

            // Check for overall reports
            val overallReports = models?.filter { it.report?.linkedId == null }
            assertEquals(31, overallReports?.size)
            val first = overallReports?.first()?.report
            assertEquals(1, first?.day)
            assertEquals(BigDecimal("-187.80"), first?.amount)
            assertNull(first?.budget)
            assertEquals(BigDecimal("-120.53"), first?.average)
            assertEquals(BigDecimal("-215.80"), first?.previous)
            assertNull(first?.filteredBudgetCategory)
            assertNull(first?.linkedId)
            assertNull(first?.name)

            // Check for group reports
            val groupReports = models?.filter { it.report?.day == 3 && it.report?.linkedId != null }?.sortedBy { it.report?.linkedId }
            assertEquals(17, groupReports?.size)
            val third = groupReports?.get(2)?.report
            assertEquals(3, third?.day)
            assertEquals(BigDecimal("-159.41"), third?.amount)
            assertNull(third?.budget)
            assertEquals(BigDecimal("-106.27"), third?.average)
            assertEquals(BigDecimal("-159.41"), third?.previous)
            assertNull(third?.filteredBudgetCategory)
            assertEquals(66L, third?.linkedId)
            assertEquals("Groceries", third?.name)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchingCurrentReportsFilteredByBudgetCategory() {
        initSetup()

        val requestPath = "${ReportsAPI.URL_REPORT_TRANSACTIONS_CURRENT}?grouping=${ReportGrouping.TRANSACTION_CATEGORY}&budget_category=${BudgetCategory.LIFESTYLE}"

        val body = readStringFromJson(app, R.raw.transaction_reports_current_txn_category_lifestyle)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshTransactionCurrentReports(grouping = ReportGrouping.TRANSACTION_CATEGORY, budgetCategory = BudgetCategory.LIFESTYLE) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = reports.currentTransactionReports(grouping = ReportGrouping.TRANSACTION_CATEGORY, budgetCategory = BudgetCategory.LIFESTYLE).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)

            // Check for overall reports
            val overallReports = models?.filter { it.report?.linkedId == null }
            assertEquals(31, overallReports?.size)
            val first = overallReports?.first()?.report
            assertEquals(1, first?.day)
            assertEquals(BigDecimal("-108.00"), first?.amount)
            assertNull(first?.budget)
            assertEquals(BigDecimal("-27.33"), first?.average)
            assertEquals(BigDecimal("-66.00"), first?.previous)
            assertEquals(BudgetCategory.LIFESTYLE, first?.filteredBudgetCategory)
            assertNull(first?.linkedId)
            assertNull(first?.name)

            // Check for group reports
            val groupReports = models?.filter { it.report?.day == 6 && it.report?.linkedId != null }?.sortedBy { it.report?.linkedId }
            assertEquals(11, groupReports?.size)
            val first2 = groupReports?.get(0)?.report
            assertEquals(6, first2?.day)
            assertEquals(BigDecimal("-11.99"), first2?.amount)
            assertNull(first2?.budget)
            assertEquals(BigDecimal("-7.99"), first2?.average)
            assertEquals(BigDecimal("-11.99"), first2?.previous)
            assertEquals(BudgetCategory.LIFESTYLE, first2?.filteredBudgetCategory)
            assertEquals(64L, first2?.linkedId)
            assertEquals("Entertainment/Recreation", first2?.name)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchingCurrentReportsRemovesExisting() {
        initSetup()

        val requestPath = "${ReportsAPI.URL_REPORT_TRANSACTIONS_CURRENT}?grouping=${ReportGrouping.TRANSACTION_CATEGORY}"

        mockServer.setDispatcher(object: Dispatcher() {
            var count = 0

            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    if (count > 0) {
                        return MockResponse()
                                .setResponseCode(200)
                                .setBody(readStringFromJson(app, R.raw.transaction_reports_current_txn_category_3_days))
                    } else {
                        count++
                        return MockResponse()
                                .setResponseCode(200)
                                .setBody(readStringFromJson(app, R.raw.transaction_reports_current_txn_category))
                    }
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshTransactionCurrentReports(grouping = ReportGrouping.TRANSACTION_CATEGORY) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = reports.currentTransactionReports(grouping = ReportGrouping.TRANSACTION_CATEGORY).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)

            // Check for overall reports
            val overallReports = models?.filter { it.report?.day == 11 && it.report?.linkedId == null }
            assertEquals(1, overallReports?.size)
            val first = overallReports?.first()?.report
            assertEquals(11, first?.day)
            assertEquals(BigDecimal("-64.50"), first?.amount)
            assertNull(first?.budget)
            assertEquals(BigDecimal("-188.32"), first?.average)
            assertEquals(BigDecimal("-107.50"), first?.previous)
            assertNull(first?.filteredBudgetCategory)
            assertNull(first?.linkedId)
            assertNull(first?.name)

            // Check for group reports
            val groupReports = models?.filter { it.report?.day == 11 && it.report?.linkedId != null }?.sortedBy { it.report?.linkedId }
            assertEquals(17, groupReports?.size)
            val third = groupReports?.get(2)?.report
            assertEquals(11, third?.day)
            assertEquals(BigDecimal("-24.50"), third?.amount)
            assertNull(third?.budget)
            assertEquals(BigDecimal("-16.33"), third?.average)
            assertEquals(BigDecimal("-24.50"), third?.previous)
            assertNull(third?.filteredBudgetCategory)
            assertEquals(66L, third?.linkedId)
            assertEquals("Groceries", third?.name)
        }

        wait(3)

        reports.refreshTransactionCurrentReports(grouping = ReportGrouping.TRANSACTION_CATEGORY) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = reports.currentTransactionReports(grouping = ReportGrouping.TRANSACTION_CATEGORY).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)

            // Check for overall reports
            val overallReports = models?.filter { it.report?.day == 11 && it.report?.linkedId == null }
            assertEquals(1, overallReports?.size)
            val first = overallReports?.first()?.report
            assertEquals(11, first?.day)
            assertNull(first?.amount)
            assertNull(first?.budget)
            assertEquals(BigDecimal("-188.32"), first?.average)
            assertEquals(BigDecimal("-107.50"), first?.previous)
            assertNull(first?.filteredBudgetCategory)
            assertNull(first?.linkedId)
            assertNull(first?.name)

            // Check for group reports
            val groupReports = models?.filter { it.report?.day == 11 && it.report?.linkedId != null }?.sortedBy { it.report?.linkedId }
            assertEquals(15, groupReports?.size)
            val third = groupReports?.get(2)?.report
            assertEquals(11, third?.day)
            assertNull(third?.amount)
            assertNull(third?.budget)
            assertEquals(BigDecimal("0.00"), third?.average)
            assertEquals(BigDecimal("0.00"), third?.previous)
            assertNull(third?.filteredBudgetCategory)
            assertEquals(68L, third?.linkedId)
            assertEquals("Home Improvement", third?.name)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchingCurrentReportsCommingling() {
        initSetup()

        val requestPath1 = "${ReportsAPI.URL_REPORT_TRANSACTIONS_CURRENT}?grouping=${ReportGrouping.TRANSACTION_CATEGORY}"
        val requestPath2 = "${ReportsAPI.URL_REPORT_TRANSACTIONS_CURRENT}?grouping=${ReportGrouping.TRANSACTION_CATEGORY}&budget_category=${BudgetCategory.LIVING}"
        val requestPath3 = "${ReportsAPI.URL_REPORT_TRANSACTIONS_CURRENT}?grouping=${ReportGrouping.TRANSACTION_CATEGORY}&budget_category=${BudgetCategory.LIFESTYLE}"

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath1) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transaction_reports_current_txn_category))
                } else if (request?.trimmedPath == requestPath2) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transaction_reports_current_txn_category_living))
                } else if (request?.trimmedPath == requestPath3) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transaction_reports_current_txn_category_lifestyle))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshTransactionCurrentReports(grouping = ReportGrouping.TRANSACTION_CATEGORY, budgetCategory = BudgetCategory.LIFESTYLE) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        reports.refreshTransactionCurrentReports(grouping = ReportGrouping.TRANSACTION_CATEGORY, budgetCategory = BudgetCategory.LIVING) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        reports.refreshTransactionCurrentReports(grouping = ReportGrouping.TRANSACTION_CATEGORY) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(3)

        // budgetCategory: Lifestyle

        var testObserver = reports.currentTransactionReports(grouping = ReportGrouping.TRANSACTION_CATEGORY, budgetCategory = BudgetCategory.LIFESTYLE).test()
        testObserver.awaitValue()
        var models = testObserver.value().data
        assertNotNull(models)

        // Check for overall reports
        var overallReports = models?.filter { it.report?.linkedId == null }
        assertEquals(31, overallReports?.size)
        var first = overallReports?.first()?.report
        assertEquals(1, first?.day)
        assertEquals(BigDecimal("-108.00"), first?.amount)
        assertNull(first?.budget)
        assertEquals(BigDecimal("-27.33"), first?.average)
        assertEquals(BigDecimal("-66.00"), first?.previous)
        assertEquals(BudgetCategory.LIFESTYLE, first?.filteredBudgetCategory)
        assertNull(first?.linkedId)
        assertNull(first?.name)

        // Check for group reports
        var groupReports = models?.filter { it.report?.day == 6 && it.report?.linkedId != null }?.sortedBy { it.report?.linkedId }
        assertEquals(11, groupReports?.size)
        var report = groupReports?.get(0)?.report
        assertEquals(6, report?.day)
        assertEquals(BigDecimal("-11.99"), report?.amount)
        assertNull(report?.budget)
        assertEquals(BigDecimal("-7.99"), report?.average)
        assertEquals(BigDecimal("-11.99"), report?.previous)
        assertEquals(BudgetCategory.LIFESTYLE, report?.filteredBudgetCategory)
        assertEquals(64L, report?.linkedId)
        assertEquals("Entertainment/Recreation", report?.name)

        // budgetCategory: Living

        testObserver = reports.currentTransactionReports(grouping = ReportGrouping.TRANSACTION_CATEGORY, budgetCategory = BudgetCategory.LIVING).test()
        testObserver.awaitValue()
        models = testObserver.value().data
        assertNotNull(models)

        // Check for overall reports
        overallReports = models?.filter { it.report?.linkedId == null }
        assertEquals(31, overallReports?.size)
        first = overallReports?.first()?.report
        assertEquals(1, first?.day)
        assertEquals(BigDecimal("-79.80"), first?.amount)
        assertNull(first?.budget)
        assertEquals(BigDecimal("-53.20"), first?.average)
        assertEquals(BigDecimal("-79.80"), first?.previous)
        assertEquals(BudgetCategory.LIVING, first?.filteredBudgetCategory)
        assertNull(first?.linkedId)
        assertNull(first?.name)

        // Check for group reports
        groupReports = models?.filter { it.report?.day == 6 && it.report?.linkedId != null }?.sortedBy { it.report?.linkedId }
        assertEquals(6, groupReports?.size)
        report = groupReports?.get(0)?.report
        assertEquals(6, report?.day)
        assertEquals(BigDecimal("0.00"), report?.amount)
        assertNull(report?.budget)
        assertNull(report?.average)
        assertNull(report?.previous)
        assertEquals(BudgetCategory.LIVING, report?.filteredBudgetCategory)
        assertEquals(61L, report?.linkedId)
        assertEquals("Automotive/Fuel", report?.name)

        // budgetCategory: null

        testObserver = reports.currentTransactionReports(grouping = ReportGrouping.TRANSACTION_CATEGORY).test()
        testObserver.awaitValue()
        models = testObserver.value().data
        assertNotNull(models)

        // Check for overall reports
        overallReports = models?.filter { it.report?.linkedId == null }
        assertEquals(31, overallReports?.size)
        first = overallReports?.first()?.report
        assertEquals(1, first?.day)
        assertEquals(BigDecimal("-187.80"), first?.amount)
        assertNull(first?.budget)
        assertEquals(BigDecimal("-120.53"), first?.average)
        assertEquals(BigDecimal("-215.80"), first?.previous)
        assertNull(first?.filteredBudgetCategory)
        assertNull(first?.linkedId)
        assertNull(first?.name)

        // Check for group reports
        groupReports = models?.filter { it.report?.day == 6 && it.report?.linkedId != null }?.sortedBy { it.report?.linkedId }
        assertEquals(17, groupReports?.size)
        report = groupReports?.get(0)?.report
        assertEquals(6, report?.day)
        assertEquals(BigDecimal("0.00"), report?.amount)
        assertNull(report?.budget)
        assertNull(report?.average)
        assertNull(report?.previous)
        assertNull(report?.filteredBudgetCategory)
        assertEquals(61L, report?.linkedId)
        assertEquals("Automotive/Fuel", report?.name)

        tearDown()
    }

    @Test
    fun testCurrentReportsLinkToMerchants() {
        initSetup()

        val requestPath = "${ReportsAPI.URL_REPORT_TRANSACTIONS_CURRENT}?grouping=${ReportGrouping.MERCHANT}"

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transaction_reports_current_merchant))
                } else if (request?.trimmedPath?.contains(AggregationAPI.URL_MERCHANTS) == true) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.merchants_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshMerchants { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        reports.refreshTransactionCurrentReports(grouping = ReportGrouping.MERCHANT) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(3)

        val testObserver = reports.currentTransactionReports(grouping = ReportGrouping.MERCHANT).test()
        testObserver.awaitValue()
        val models = testObserver.value().data
        assertNotNull(models)

        val reports = models?.filter { it.report?.linkedId == 81L }
        assertEquals(31, reports?.size)
        val first = reports?.first()
        assertNotNull(first?.merchant)
        assertNull(first?.transactionCategory)
        assertEquals(first?.report?.linkedId, first?.merchant?.merchantId)

        tearDown()
    }

    @Test
    fun testCurrentReportsLinkToTransactionCategories() {
        initSetup()

        val requestPath = "${ReportsAPI.URL_REPORT_TRANSACTIONS_CURRENT}?grouping=${ReportGrouping.TRANSACTION_CATEGORY}"

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transaction_reports_current_txn_category))
                } else if (request?.trimmedPath == AggregationAPI.URL_TRANSACTION_CATEGORIES) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transaction_categories_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransactionCategories { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        reports.refreshTransactionCurrentReports(grouping = ReportGrouping.TRANSACTION_CATEGORY) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(3)

        val testObserver = reports.currentTransactionReports(grouping = ReportGrouping.TRANSACTION_CATEGORY).test()
        testObserver.awaitValue()
        val models = testObserver.value().data
        assertNotNull(models)

        val reports = models?.filter { it.report?.linkedId == 79L }
        assertEquals(31, reports?.size)
        val first = reports?.first()
        assertNotNull(first?.transactionCategory)
        assertNull(first?.merchant)
        assertEquals(first?.report?.linkedId, first?.transactionCategory?.transactionCategoryId)

        tearDown()
    }

    // History Report Tests

    @Test
    fun testFetchHistoryTransactionReports() {
        initSetup()

        val data1 = testReportTransactionHistoryData(id = 100, date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT, budgetCategory = BudgetCategory.LIVING)
        val data2 = testReportTransactionHistoryData(id = 101, date = "2018-06", period = ReportPeriod.MONTH, grouping = ReportGrouping.MERCHANT)
        val data3 = testReportTransactionHistoryData(id = 102, date = "2018-06-01", period = ReportPeriod.DAY, grouping = ReportGrouping.MERCHANT)
        val data4 = testReportTransactionHistoryData(id = 103, date = "2018-06-02", period = ReportPeriod.DAY, grouping = ReportGrouping.TRANSACTION_CATEGORY)

        val list = mutableListOf(data1, data2, data3, data4)

        database.reportsTransactionHistory().insertAll(*list.toTypedArray())

        val testObserver = reports.historyTransactionReports(fromDate = "2018-05", toDate = "2018-06", grouping = ReportGrouping.MERCHANT, period = ReportPeriod.MONTH).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(1, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchingHistoryReportsByBudgetCategory() {
        initSetup()

        val fromDate = "2018-01"
        val toDate = "2018-12"
        val period = ReportPeriod.MONTH
        val grouping = ReportGrouping.BUDGET_CATEGORY
        val requestPath = "${ReportsAPI.URL_REPORT_TRANSACTIONS_HISTORY}?grouping=$grouping&period=$period&from_date=$fromDate&to_date=$toDate"

        val body = readStringFromJson(app, R.raw.transaction_reports_history_budget_category_monthly_2018_01_01_2018_12_31)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshTransactionHistoryReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = reports.historyTransactionReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)

            // Check for overall reports
            val overallReports = models?.sortedBy { it.report?.date }
            assertEquals(12, overallReports?.size)

            val first = overallReports?.first()
            assertEquals("2018-01", first?.report?.date)
            assertEquals(BigDecimal("744.37"), first?.report?.value)
            assertEquals(BigDecimal("11000.0"), first?.report?.budget)
            assertNull(first?.report?.filteredBudgetCategory)
            assertNotNull(first?.groups)
            assertEquals(4, first?.groups?.size)

            // Check for group reports
            val groupReports = overallReports?.filter { it.report?.date == "2018-03" }?.get(0)?.groups?.sortedBy { it.groupReport?.linkedId }
            assertEquals(4, groupReports?.size)

            val gr = groupReports?.first()
            assertEquals("2018-03", gr?.groupReport?.date)
            assertEquals(BigDecimal("3250.0"), gr?.groupReport?.value)
            assertEquals(BigDecimal("4050.0"), gr?.groupReport?.budget)
            assertNull(gr?.groupReport?.filteredBudgetCategory)
            assertNotNull(gr?.overall)
            assertEquals("2018-03", gr?.overall?.date)
            assertEquals(0L, gr?.groupReport?.linkedId)
            assertEquals("income", gr?.groupReport?.name)
            assertEquals(BudgetCategory.INCOME, gr?.budgetCategory)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchingHistoryReportsByMerchant() {
        initSetup()

        val fromDate = "2018-01"
        val toDate = "2018-12"
        val period = ReportPeriod.MONTH
        val grouping = ReportGrouping.MERCHANT
        val requestPath = "${ReportsAPI.URL_REPORT_TRANSACTIONS_HISTORY}?grouping=$grouping&period=$period&from_date=$fromDate&to_date=$toDate"

        val body = readStringFromJson(app, R.raw.transaction_reports_history_merchant_monthly_2018_01_01_2018_12_31)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshTransactionHistoryReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = reports.historyTransactionReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)

            // Check for overall reports
            val overallReports = models?.sortedBy { it.report?.date }
            assertEquals(12, overallReports?.size)

            val first = overallReports?.first()
            assertEquals("2018-01", first?.report?.date)
            assertEquals(BigDecimal("744.37"), first?.report?.value)
            assertNull(first?.report?.budget)
            assertNull(first?.report?.filteredBudgetCategory)
            assertNotNull(first?.groups)
            assertEquals(15, first?.groups?.size)

            // Check for group reports
            val groupReports = overallReports?.filter { it.report?.date == "2018-03" }?.get(0)?.groups?.sortedBy { it.groupReport?.linkedId }
            assertEquals(22, groupReports?.size)

            val gr = groupReports?.last()
            assertEquals("2018-03", gr?.groupReport?.date)
            assertEquals(BigDecimal("-127.0"), gr?.groupReport?.value)
            assertNull(gr?.groupReport?.budget)
            assertNull(gr?.groupReport?.filteredBudgetCategory)
            assertNotNull(gr?.overall)
            assertEquals("2018-03", gr?.overall?.date)
            assertEquals(292L, gr?.groupReport?.linkedId)
            assertEquals("SUSHI PTY. LTD.", gr?.groupReport?.name)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchingHistoryReportsByTransactionCategoryDaily() {
        initSetup()

        val fromDate = "2018-01-01"
        val toDate = "2018-12-31"
        val period = ReportPeriod.DAY
        val grouping = ReportGrouping.TRANSACTION_CATEGORY
        val requestPath = "${ReportsAPI.URL_REPORT_TRANSACTIONS_HISTORY}?grouping=$grouping&period=$period&from_date=$fromDate&to_date=$toDate"

        val body = readStringFromJson(app, R.raw.transaction_reports_history_txn_category_daily_2018_01_01_2018_12_31)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshTransactionHistoryReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = reports.historyTransactionReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)

            // Check for overall reports
            val overallReports = models?.sortedBy { it.report?.date }
            assertEquals(365, overallReports?.size)

            val last = overallReports?.last()
            assertEquals("2018-12-31", last?.report?.date)
            assertEquals(BigDecimal("-84.6"), last?.report?.value)
            assertNull(last?.report?.budget)
            assertNull(last?.report?.filteredBudgetCategory)
            assertNotNull(last?.groups)
            assertEquals(3, last?.groups?.size)

            // Check for group reports
            val groupReports = overallReports?.filter { it.report?.date == "2018-06-02" }?.get(0)?.groups?.sortedBy { it.groupReport?.linkedId }
            assertEquals(2, groupReports?.size)

            val gr = groupReports?.first()
            assertEquals("2018-06-02", gr?.groupReport?.date)
            assertEquals(BigDecimal("-12.6"), gr?.groupReport?.value)
            assertNull(gr?.groupReport?.budget)
            assertNull(gr?.groupReport?.filteredBudgetCategory)
            assertNotNull(gr?.overall)
            assertEquals("2018-06-02", gr?.overall?.date)
            assertEquals(66L, gr?.groupReport?.linkedId)
            assertEquals("Groceries", gr?.groupReport?.name)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchingHistoryReportsByTransactionCategoryMonthly() {
        initSetup()

        val fromDate = "2018-01"
        val toDate = "2018-12"
        val period = ReportPeriod.MONTH
        val grouping = ReportGrouping.TRANSACTION_CATEGORY
        val requestPath = "${ReportsAPI.URL_REPORT_TRANSACTIONS_HISTORY}?grouping=$grouping&period=$period&from_date=$fromDate&to_date=$toDate"

        val body = readStringFromJson(app, R.raw.transaction_reports_history_txn_category_monthly_2018_01_01_2018_12_31)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshTransactionHistoryReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = reports.historyTransactionReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)

            // Check for overall reports
            val overallReports = models?.sortedBy { it.report?.date }
            assertEquals(12, overallReports?.size)

            val third = overallReports?.get(2)
            assertEquals("2018-03", third?.report?.date)
            assertEquals(BigDecimal("563.17"), third?.report?.value)
            assertNull(third?.report?.budget)
            assertNull(third?.report?.filteredBudgetCategory)
            assertNotNull(third?.groups)
            assertEquals(15, third?.groups?.size)

            // Check for group reports
            val groupReports = overallReports?.filter { it.report?.date == "2018-05" }?.get(0)?.groups?.sortedBy { it.groupReport?.linkedId }
            assertEquals(15, groupReports?.size)

            val gr = groupReports?.first()
            assertEquals("2018-05", gr?.groupReport?.date)
            assertEquals(BigDecimal("-29.98"), gr?.groupReport?.value)
            assertNull(gr?.groupReport?.budget)
            assertNull(gr?.groupReport?.filteredBudgetCategory)
            assertNotNull(gr?.overall)
            assertEquals("2018-05", gr?.overall?.date)
            assertEquals(64L, gr?.groupReport?.linkedId)
            assertEquals("Entertainment/Recreation", gr?.groupReport?.name)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchingHistoryReportsByTransactionCategoryWeekly() {
        initSetup()

        val fromDate = "2018-01-1"
        val toDate = "2018-12-5"
        val period = ReportPeriod.WEEK
        val grouping = ReportGrouping.TRANSACTION_CATEGORY
        val requestPath = "${ReportsAPI.URL_REPORT_TRANSACTIONS_HISTORY}?grouping=$grouping&period=$period&from_date=$fromDate&to_date=$toDate"

        val body = readStringFromJson(app, R.raw.transaction_reports_history_txn_category_weekly_2018_01_01_2018_12_31)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshTransactionHistoryReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = reports.historyTransactionReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)

            // Check for overall reports
            val overallReports = models?.sortedBy { it.report?.date }
            assertEquals(59, overallReports?.size)

            val last = overallReports?.last()
            assertEquals("2018-12-5", last?.report?.date)
            assertEquals(BigDecimal("-577.6"), last?.report?.value)
            assertNull(last?.report?.budget)
            assertNull(last?.report?.filteredBudgetCategory)
            assertNotNull(last?.groups)
            assertEquals(6, last?.groups?.size)

            // Check for group reports
            val groupReports = overallReports?.filter { it.report?.date == "2018-12-5" }?.get(0)?.groups?.sortedBy { it.groupReport?.linkedId }
            assertEquals(6, groupReports?.size)

            val gr = groupReports?.first()
            assertEquals("2018-12-5", gr?.groupReport?.date)
            assertEquals(BigDecimal("-12.6"), gr?.groupReport?.value)
            assertNull(gr?.groupReport?.budget)
            assertNull(gr?.groupReport?.filteredBudgetCategory)
            assertNotNull(gr?.overall)
            assertEquals("2018-12-5", gr?.overall?.date)
            assertEquals(66L, gr?.groupReport?.linkedId)
            assertEquals("Groceries", gr?.groupReport?.name)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchingHistoryReportsFilteredByBudgetCategory() {
        initSetup()

        val fromDate = "2018-01"
        val toDate = "2018-12"
        val period = ReportPeriod.MONTH
        val grouping = ReportGrouping.TRANSACTION_CATEGORY
        val budgetCategory = BudgetCategory.LIFESTYLE
        val requestPath = "${ReportsAPI.URL_REPORT_TRANSACTIONS_HISTORY}?grouping=$grouping&period=$period&from_date=$fromDate&to_date=$toDate&budget_category=$budgetCategory"

        val body = readStringFromJson(app, R.raw.transaction_reports_history_txn_category_monthly_lifestyle_2018_01_01_2018_12_31)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshTransactionHistoryReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate, budgetCategory = budgetCategory) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = reports.historyTransactionReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate, budgetCategory = budgetCategory).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)

            // Check for overall reports
            val overallReports = models?.sortedBy { it.report?.date }
            assertEquals(12, overallReports?.size)

            val fifth = overallReports?.get(4)
            assertEquals("2018-05", fifth?.report?.date)
            assertEquals(BigDecimal("-778.93"), fifth?.report?.value)
            assertNull(fifth?.report?.budget)
            assertEquals(BudgetCategory.LIFESTYLE, fifth?.report?.filteredBudgetCategory)
            assertNotNull(fifth?.groups)
            assertEquals(7, fifth?.groups?.size)

            // Check for group reports
            val groupReports = overallReports?.filter { it.report?.date == "2018-05" }?.get(0)?.groups?.sortedBy { it.groupReport?.linkedId }
            assertEquals(7, groupReports?.size)

            val gr = groupReports?.last()
            assertEquals("2018-05", gr?.groupReport?.date)
            assertEquals(BigDecimal("-40.0"), gr?.groupReport?.value)
            assertNull(gr?.groupReport?.budget)
            assertEquals(BudgetCategory.LIFESTYLE, gr?.groupReport?.filteredBudgetCategory)
            assertNotNull(gr?.overall)
            assertEquals("2018-05", gr?.overall?.date)
            assertEquals(94L, gr?.groupReport?.linkedId)
            assertEquals("Electronics/General Merchandise", gr?.groupReport?.name)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchingHistoryReportsUpdatesExisting() {
        initSetup()

        val oldFromDate = "2018-01"
        val oldToDate = "2018-12"
        val newFromDate = "2018-03"
        val newToDate = "2019-03"
        val period = ReportPeriod.MONTH
        val grouping = ReportGrouping.TRANSACTION_CATEGORY
        val requestPath1 = "${ReportsAPI.URL_REPORT_TRANSACTIONS_HISTORY}?grouping=$grouping&period=$period&from_date=$oldFromDate&to_date=$oldToDate"
        val requestPath2 = "${ReportsAPI.URL_REPORT_TRANSACTIONS_HISTORY}?grouping=$grouping&period=$period&from_date=$newFromDate&to_date=$newToDate"

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath1) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transaction_reports_history_txn_category_monthly_2018_01_01_2018_12_31))
                } else if (request?.trimmedPath == requestPath2) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transaction_reports_history_txn_category_monthly_2018_03_01_2019_03_31))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshTransactionHistoryReports(grouping = grouping, period = period, fromDate = oldFromDate, toDate = oldToDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(5)

        // Old report
        var testObserver = reports.historyTransactionReports(grouping = grouping, period = period, fromDate = oldFromDate, toDate = oldToDate).test()
        testObserver.awaitValue()
        var models = testObserver.value().data
        assertNotNull(models)

        // Check for old overall report
        var overallReports = models?.sortedBy { it.report?.date }?.filter { it.report?.date == "2018-01" }
        assertEquals(1, overallReports?.size)

        var first = overallReports?.first()
        assertEquals("2018-01", first?.report?.date)
        assertNotNull(first?.groups)
        assertEquals(12, first?.groups?.size)

        // Check for old group report
        var groupReports = overallReports?.filter { it.report?.date == "2018-01" }?.get(0)?.groups?.sortedBy { it.groupReport?.linkedId }
        assertEquals(12, groupReports?.size)

        var gr = groupReports?.first()
        assertEquals("2018-01", gr?.groupReport?.date)
        assertNotNull(gr?.overall)
        assertEquals("2018-01", gr?.overall?.date)

        // New report
        testObserver = reports.historyTransactionReports(grouping = grouping, period = period, fromDate = newFromDate, toDate = newToDate).test()
        testObserver.awaitValue()
        models = testObserver.value().data

        // Check no new overall report is found
        overallReports = models?.sortedBy { it.report?.date }?.filter { it.report?.date == "2019-01" }
        assertEquals(0, overallReports?.size)

        // Fetch more recent reports
        reports.refreshTransactionHistoryReports(grouping = grouping, period = period, fromDate = newFromDate, toDate = newToDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(5)

        // Old report
        testObserver = reports.historyTransactionReports(grouping = grouping, period = period, fromDate = oldFromDate, toDate = oldToDate).test()
        testObserver.awaitValue()
        models = testObserver.value().data
        assertNotNull(models)

        // Check for old overall report
        overallReports = models?.sortedBy { it.report?.date }?.filter { it.report?.date == "2018-01" }
        assertEquals(1, overallReports?.size)

        first = overallReports?.first()
        assertEquals("2018-01", first?.report?.date)
        assertNotNull(first?.groups)
        assertEquals(12, first?.groups?.size)

        // Check for old group report
        groupReports = overallReports?.filter { it.report?.date == "2018-01" }?.get(0)?.groups?.sortedBy { it.groupReport?.linkedId }
        assertEquals(12, groupReports?.size)

        gr = groupReports?.first()
        assertEquals("2018-01", gr?.groupReport?.date)
        assertNotNull(gr?.overall)
        assertEquals("2018-01", gr?.overall?.date)

        // New report
        testObserver = reports.historyTransactionReports(grouping = grouping, period = period, fromDate = newFromDate, toDate = newToDate).test()
        testObserver.awaitValue()
        models = testObserver.value().data
        assertNotNull(models)

        // Check new overall report is found
        overallReports = models?.sortedBy { it.report?.date }?.filter { it.report?.date == "2019-01" }
        assertEquals(1, overallReports?.size)

        first = overallReports?.first()
        assertEquals("2019-01", first?.report?.date)
        assertNotNull(first?.groups)
        assertEquals(14, first?.groups?.size)

        // Check for new group report
        groupReports = overallReports?.filter { it.report?.date == "2019-01" }?.get(0)?.groups?.sortedBy { it.groupReport?.linkedId }
        assertEquals(14, groupReports?.size)

        gr = groupReports?.first()
        assertEquals("2019-01", gr?.groupReport?.date)
        assertNotNull(gr?.overall)
        assertEquals("2019-01", gr?.overall?.date)

        tearDown()
    }

    @Test
    fun testFetchingHistoryReportsCommingling() {
        initSetup()

        val fromDate = "2018-01"
        val toDate = "2018-12"
        val period = ReportPeriod.MONTH
        val grouping = ReportGrouping.TRANSACTION_CATEGORY
        val living = BudgetCategory.LIVING
        val lifestyle = BudgetCategory.LIFESTYLE
        val requestPath1 = "${ReportsAPI.URL_REPORT_TRANSACTIONS_HISTORY}?grouping=$grouping&period=$period&from_date=$fromDate&to_date=$toDate&budget_category=$living"
        val requestPath2 = "${ReportsAPI.URL_REPORT_TRANSACTIONS_HISTORY}?grouping=$grouping&period=$period&from_date=$fromDate&to_date=$toDate&budget_category=$lifestyle"
        val requestPath3 = "${ReportsAPI.URL_REPORT_TRANSACTIONS_HISTORY}?grouping=$grouping&period=$period&from_date=$fromDate&to_date=$toDate"

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath1) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transaction_reports_history_txn_category_monthly_living_2018_01_01_2018_12_31))
                } else if (request?.trimmedPath == requestPath2) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transaction_reports_history_txn_category_monthly_lifestyle_2018_01_01_2018_12_31))
                } else if (request?.trimmedPath == requestPath3) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transaction_reports_history_txn_category_monthly_2018_01_01_2018_12_31))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshTransactionHistoryReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate, budgetCategory = living) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        reports.refreshTransactionHistoryReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate, budgetCategory = lifestyle) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        reports.refreshTransactionHistoryReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(8)

        // Lifestyle reports
        var testObserver = reports.historyTransactionReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate, budgetCategory = lifestyle).test()
        testObserver.awaitValue()
        var models = testObserver.value().data
        assertNotNull(models)

        // Check for overall lifestyle reports
        var overallReports = models?.sortedBy { it.report?.date }
        assertEquals(12, overallReports?.size)

        var fifth = overallReports?.get(4)
        assertEquals("2018-05", fifth?.report?.date)
        assertEquals(BigDecimal("-778.93"), fifth?.report?.value)
        assertNull(fifth?.report?.budget)
        assertEquals(lifestyle, fifth?.report?.filteredBudgetCategory)
        assertNotNull(fifth?.groups)
        assertEquals(7, fifth?.groups?.size)

        // Check for overall lifestyle group reports
        var groupReports = overallReports?.filter { it.report?.date == "2018-05" }?.get(0)?.groups?.sortedBy { it.groupReport?.linkedId }
        assertEquals(7, groupReports?.size)

        var gr = groupReports?.last()
        assertEquals("2018-05", gr?.groupReport?.date)
        assertEquals(BigDecimal("-40.0"), gr?.groupReport?.value)
        assertNull(gr?.groupReport?.budget)
        assertEquals(lifestyle, gr?.groupReport?.filteredBudgetCategory)
        assertNotNull(gr?.overall)
        assertEquals("2018-05", gr?.overall?.date)
        assertEquals(94L, gr?.groupReport?.linkedId)
        assertEquals("Electronics/General Merchandise", gr?.groupReport?.name)

        // Living reports
        testObserver = reports.historyTransactionReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate, budgetCategory = living).test()
        testObserver.awaitValue()
        models = testObserver.value().data
        assertNotNull(models)

        // Check for overall living reports
        overallReports = models?.sortedBy { it.report?.date }
        assertEquals(12, overallReports?.size)

        fifth = overallReports?.get(4)
        assertEquals("2018-05", fifth?.report?.date)
        assertEquals(BigDecimal("-1569.45"), fifth?.report?.value)
        assertNull(fifth?.report?.budget)
        assertEquals(living, fifth?.report?.filteredBudgetCategory)
        assertNotNull(fifth?.groups)
        assertEquals(5, fifth?.groups?.size)

        // Check for overall living group reports
        groupReports = overallReports?.filter { it.report?.date == "2018-05" }?.get(0)?.groups?.sortedBy { it.groupReport?.linkedId }
        assertEquals(5, groupReports?.size)

        gr = groupReports?.first()
        assertEquals("2018-05", gr?.groupReport?.date)
        assertEquals(BigDecimal("-569.55"), gr?.groupReport?.value)
        assertNull(gr?.groupReport?.budget)
        assertEquals(living, gr?.groupReport?.filteredBudgetCategory)
        assertNotNull(gr?.overall)
        assertEquals("2018-05", gr?.overall?.date)
        assertEquals(66L, gr?.groupReport?.linkedId)
        assertEquals("Groceries", gr?.groupReport?.name)

        // General reports
        testObserver = reports.historyTransactionReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate).test()
        testObserver.awaitValue()
        models = testObserver.value().data
        assertNotNull(models)

        // Check for overall general reports
        overallReports = models?.sortedBy { it.report?.date }
        assertEquals(12, overallReports?.size)

        fifth = overallReports?.get(4)
        assertEquals("2018-05", fifth?.report?.date)
        assertEquals(BigDecimal("671.62"), fifth?.report?.value)
        assertNull(fifth?.report?.budget)
        assertNull(fifth?.report?.filteredBudgetCategory)
        assertNotNull(fifth?.groups)
        assertEquals(15, fifth?.groups?.size)

        // Check for overall general group reports
        groupReports = overallReports?.filter { it.report?.date == "2018-05" }?.get(0)?.groups?.sortedBy { it.groupReport?.linkedId }
        assertEquals(15, groupReports?.size)

        gr = groupReports?.first()
        assertEquals("2018-05", gr?.groupReport?.date)
        assertEquals(BigDecimal("-29.98"), gr?.groupReport?.value)
        assertNull(gr?.groupReport?.budget)
        assertNull(gr?.groupReport?.filteredBudgetCategory)
        assertNotNull(gr?.overall)
        assertEquals("2018-05", gr?.overall?.date)
        assertEquals(64L, gr?.groupReport?.linkedId)
        assertEquals("Entertainment/Recreation", gr?.groupReport?.name)
    }

    @Test
    fun testFetchingHistoryReportsDuplicating() {
        initSetup()

        val fromDate = "2018-01"
        val toDate = "2018-12"
        val period = ReportPeriod.MONTH
        val grouping = ReportGrouping.TRANSACTION_CATEGORY
        val requestPath = "${ReportsAPI.URL_REPORT_TRANSACTIONS_HISTORY}?grouping=$grouping&period=$period&from_date=$fromDate&to_date=$toDate"

        val body = readStringFromJson(app, R.raw.transaction_reports_history_txn_category_monthly_2018_01_01_2018_12_31)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        reports.refreshTransactionHistoryReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(5)

        var testObserver = reports.historyTransactionReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate).test()
        testObserver.awaitValue()
        var models = testObserver.value().data
        assertNotNull(models)

        // Check for overall reports
        var overallReports = models?.sortedBy { it.report?.date }
        assertEquals(12, overallReports?.size)

        var fifth = overallReports?.get(4)
        assertEquals("2018-05", fifth?.report?.date)
        assertEquals(BigDecimal("671.62"), fifth?.report?.value)
        assertNull(fifth?.report?.budget)
        assertNull(fifth?.report?.filteredBudgetCategory)
        assertNotNull(fifth?.groups)
        assertEquals(15, fifth?.groups?.size)

        // Check for group reports
        var groupReports = overallReports?.filter { it.report?.date == "2018-05" }?.get(0)?.groups?.sortedBy { it.groupReport?.linkedId }
        assertEquals(15, groupReports?.size)

        var gr = groupReports?.first()
        assertEquals("2018-05", gr?.groupReport?.date)
        assertEquals(BigDecimal("-29.98"), gr?.groupReport?.value)
        assertNull(gr?.groupReport?.budget)
        assertNull(gr?.groupReport?.filteredBudgetCategory)
        assertNotNull(gr?.overall)
        assertEquals("2018-05", gr?.overall?.date)
        assertEquals(64L, gr?.groupReport?.linkedId)
        assertEquals("Entertainment/Recreation", gr?.groupReport?.name)

        reports.refreshTransactionHistoryReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(5)

        testObserver = reports.historyTransactionReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate).test()
        testObserver.awaitValue()
        models = testObserver.value().data
        assertNotNull(models)

        // Check for overall reports
        overallReports = models?.sortedBy { it.report?.date }
        assertEquals(12, overallReports?.size)

        fifth = overallReports?.get(4)
        assertEquals("2018-05", fifth?.report?.date)
        assertEquals(BigDecimal("671.62"), fifth?.report?.value)
        assertNull(fifth?.report?.budget)
        assertNull(fifth?.report?.filteredBudgetCategory)
        assertNotNull(fifth?.groups)
        assertEquals(15, fifth?.groups?.size)

        // Check for group reports
        groupReports = overallReports?.filter { it.report?.date == "2018-05" }?.get(0)?.groups?.sortedBy { it.groupReport?.linkedId }
        assertEquals(15, groupReports?.size)

        gr = groupReports?.first()
        assertEquals("2018-05", gr?.groupReport?.date)
        assertEquals(BigDecimal("-29.98"), gr?.groupReport?.value)
        assertNull(gr?.groupReport?.budget)
        assertNull(gr?.groupReport?.filteredBudgetCategory)
        assertNotNull(gr?.overall)
        assertEquals("2018-05", gr?.overall?.date)
        assertEquals(64L, gr?.groupReport?.linkedId)
        assertEquals("Entertainment/Recreation", gr?.groupReport?.name)

        tearDown()
    }

    @Test
    fun testHistoryReportsLinkToMerchants() {
        initSetup()

        val fromDate = "2018-01"
        val toDate = "2018-12"
        val period = ReportPeriod.MONTH
        val grouping = ReportGrouping.MERCHANT
        val requestPath = "${ReportsAPI.URL_REPORT_TRANSACTIONS_HISTORY}?grouping=$grouping&period=$period&from_date=$fromDate&to_date=$toDate"

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transaction_reports_history_merchant_monthly_2018_01_01_2018_12_31))
                } else if (request?.trimmedPath?.contains(AggregationAPI.URL_MERCHANTS) == true) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.merchants_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshMerchants { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        reports.refreshTransactionHistoryReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(3)

        val testObserver = reports.historyTransactionReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate).test()
        testObserver.awaitValue()
        val models = testObserver.value().data
        assertNotNull(models)

        val groupReports = models?.filter { it.report?.date == "2018-02" }?.first()?.groups

        val first = groupReports?.filter { it.groupReport?.linkedId == 97L }?.first()
        assertNotNull(first?.merchant)
        assertNull(first?.transactionCategory)
        assertEquals(first?.groupReport?.linkedId, first?.merchant?.merchantId)

        tearDown()
    }

    @Test
    fun testHistoryReportsLinkToTransactionCategories() {
        initSetup()

        val fromDate = "2018-01"
        val toDate = "2018-12"
        val period = ReportPeriod.MONTH
        val grouping = ReportGrouping.TRANSACTION_CATEGORY
        val requestPath = "${ReportsAPI.URL_REPORT_TRANSACTIONS_HISTORY}?grouping=$grouping&period=$period&from_date=$fromDate&to_date=$toDate"

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transaction_reports_history_txn_category_monthly_2018_01_01_2018_12_31))
                } else if (request?.trimmedPath == AggregationAPI.URL_TRANSACTION_CATEGORIES) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transaction_categories_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransactionCategories { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        reports.refreshTransactionHistoryReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(3)

        val testObserver = reports.historyTransactionReports(grouping = grouping, period = period, fromDate = fromDate, toDate = toDate).test()
        testObserver.awaitValue()
        val models = testObserver.value().data
        assertNotNull(models)

        val groupReports = models?.filter { it.report?.date == "2018-02" }?.first()?.groups

        val first = groupReports?.filter { it.groupReport?.linkedId == 79L }?.first()
        assertNull(first?.merchant)
        assertNotNull(first?.transactionCategory)
        assertEquals(first?.groupReport?.linkedId, first?.transactionCategory?.transactionCategoryId)

        tearDown()
    }
}