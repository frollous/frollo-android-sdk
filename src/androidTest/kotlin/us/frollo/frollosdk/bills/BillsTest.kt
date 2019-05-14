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

package us.frollo.frollosdk.bills

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.test.platform.app.InstrumentationRegistry
import com.jraska.livedata.test
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.aggregation.Aggregation
import us.frollo.frollosdk.authentication.OAuth
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.testSDKConfig
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.extensions.toString
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.mapping.toAccount
import us.frollo.frollosdk.mapping.toBill
import us.frollo.frollosdk.mapping.toBillPayment
import us.frollo.frollosdk.mapping.toMerchant
import us.frollo.frollosdk.mapping.toTransactionCategory
import us.frollo.frollosdk.model.coredata.bills.Bill
import us.frollo.frollosdk.model.coredata.bills.BillFrequency
import us.frollo.frollosdk.model.testAccountResponseData
import us.frollo.frollosdk.model.testBillPaymentResponseData
import us.frollo.frollosdk.model.testBillResponseData
import us.frollo.frollosdk.model.testMerchantResponseData
import us.frollo.frollosdk.model.testTransactionCategoryResponseData
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.AggregationAPI
import us.frollo.frollosdk.network.api.BillsAPI
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import us.frollo.frollosdk.testutils.wait
import java.math.BigDecimal

class BillsTest {

    @get:Rule
    val testRule = InstantTaskExecutorRule()
    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private lateinit var mockServer: MockWebServer
    private lateinit var preferences: Preferences
    private lateinit var keystore: Keystore
    private lateinit var database: SDKDatabase
    private lateinit var network: NetworkService

    private lateinit var aggregation: Aggregation

    private lateinit var bills: Bills

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
        bills = Bills(network, database, aggregation)
    }

    private fun tearDown() {
        mockServer.shutdown()
        preferences.resetAll()
        database.clearAllTables()
    }

    // Bill Tests

    @Test
    fun testFetchBillByID() {
        initSetup()

        val data = testBillResponseData()
        val list = mutableListOf(testBillResponseData(), data, testBillResponseData())
        database.bills().insertAll(*list.map { it.toBill() }.toList().toTypedArray())

        val testObserver = bills.fetchBill(data.billId).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(data.billId, testObserver.value().data?.billId)

        tearDown()
    }

    @Test
    fun testFetchBills() {
        initSetup()

        val data1 = testBillResponseData()
        val data2 = testBillResponseData()
        val data3 = testBillResponseData()
        val data4 = testBillResponseData()
        val list = mutableListOf(data1, data2, data3, data4)

        database.bills().insertAll(*list.map { it.toBill() }.toList().toTypedArray())

        val testObserver = bills.fetchBills().test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(4, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchBillByIDWithRelation() {
        initSetup()

        database.transactionCategories().insert(testTransactionCategoryResponseData(transactionCategoryId = 567).toTransactionCategory())
        database.merchants().insert(testMerchantResponseData(merchantId = 678).toMerchant())
        database.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        database.bills().insert(testBillResponseData(billId = 123, accountId = 345, merchantId = 678, transactionCategoryId = 567).toBill())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 456, billId = 123).toBillPayment())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 457, billId = 123).toBillPayment())

        val testObserver = bills.fetchBillWithRelation(billId = 123).test()

        testObserver.awaitValue()
        val model = testObserver.value().data

        assertEquals(123L, model?.bill?.billId)
        assertEquals(345L, model?.account?.account?.accountId)
        assertEquals(567L, model?.transactionCategory?.transactionCategoryId)
        assertEquals(678L, model?.merchant?.merchantId)
        assertEquals(2, model?.payments?.size)
        assertEquals(456L, model?.payments?.get(0)?.billPaymentId)
        assertEquals(457L, model?.payments?.get(1)?.billPaymentId)

        tearDown()
    }

    @Test
    fun testFetchBillsWithRelation() {
        initSetup()

        database.transactionCategories().insert(testTransactionCategoryResponseData(transactionCategoryId = 567).toTransactionCategory())
        database.merchants().insert(testMerchantResponseData(merchantId = 678).toMerchant())
        database.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        database.bills().insert(testBillResponseData(billId = 123, accountId = 345, merchantId = 678, transactionCategoryId = 567).toBill())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 456, billId = 123).toBillPayment())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 457, billId = 123).toBillPayment())

        val testObserver = bills.fetchBillsWithRelation().test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(1, testObserver.value().data?.size)

        val model = testObserver.value().data?.get(0)

        assertEquals(123L, model?.bill?.billId)
        assertEquals(345L, model?.account?.account?.accountId)
        assertEquals(567L, model?.transactionCategory?.transactionCategoryId)
        assertEquals(678L, model?.merchant?.merchantId)
        assertEquals(2, model?.payments?.size)
        assertEquals(456L, model?.payments?.get(0)?.billPaymentId)
        assertEquals(457L, model?.payments?.get(1)?.billPaymentId)

        tearDown()
    }

    @Test
    fun testCreateBillWithTransaction() {
        initSetup()

        val body = readStringFromJson(app, R.raw.bill_id_12345)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == BillsAPI.URL_BILLS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val date = LocalDate.now().plusDays(1).toString(Bill.DATE_FORMAT_PATTERN)

        bills.createBill(transactionId = 987, frequency = BillFrequency.MONTHLY, nextPaymentDate = date) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = bills.fetchBill(billId = 12345).test()

            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(12345L, models?.billId)
            assertEquals("Netflix", models?.name)
        }

        val request = mockServer.takeRequest()
        assertEquals(BillsAPI.URL_BILLS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testCreateBillManually() {
        initSetup()

        val body = readStringFromJson(app, R.raw.bill_id_12345)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == BillsAPI.URL_BILLS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val date = LocalDate.now().plusDays(1).toString(Bill.DATE_FORMAT_PATTERN)

        bills.createBill(dueAmount = BigDecimal("50.0"), frequency = BillFrequency.MONTHLY, nextPaymentDate = date, name = "Stan", notes = "Cancel this") { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = bills.fetchBill(billId = 12345).test()

            testObserver.awaitValue()
            val model = testObserver.value().data
            assertNotNull(model)
            assertEquals(12345L, model?.billId)
            assertEquals("Netflix", model?.name)
        }

        val request = mockServer.takeRequest()
        assertEquals(BillsAPI.URL_BILLS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testDeleteBill() {
        initSetup()

        val billId: Long = 12345

        val requestPath = "bills/$billId"

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        database.bills().insert(testBillResponseData(billId).toBill())

        var testObserver = bills.fetchBill(billId).test()

        testObserver.awaitValue()
        val model = testObserver.value().data
        assertNotNull(model)
        assertEquals(billId, model?.billId)

        bills.deleteBill(billId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            testObserver = bills.fetchBill(billId).test()

            testObserver.awaitValue()
            assertNull(testObserver.value().data)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshBills() {
        initSetup()

        val body = readStringFromJson(app, R.raw.bills_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == BillsAPI.URL_BILLS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        bills.refreshBills { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = bills.fetchBills().test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)
            assertEquals(7, testObserver.value().data?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals(BillsAPI.URL_BILLS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshBillById() {
        initSetup()

        val billId: Long = 12345

        val requestPath = "bills/$billId"

        val body = readStringFromJson(app, R.raw.bill_id_12345)
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

        bills.refreshBill(billId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = bills.fetchBill(billId = 12345).test()

            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(12345L, models?.billId)
            assertEquals("Netflix", models?.name)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateBill() {
        initSetup()

        val billId: Long = 12345

        val requestPath = "bills/$billId"

        val body = readStringFromJson(app, R.raw.bill_id_12345)
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

        val bill = testBillResponseData(billId = 12345).toBill()

        database.bills().insert(bill)

        bills.updateBill(bill) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = bills.fetchBill(billId = 12345).test()

            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(12345L, models?.billId)
            assertEquals("Netflix", models?.name)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testBillsLinkToAccounts() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == BillsAPI.URL_BILLS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.bills_valid))
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

        bills.refreshBills { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(3)

        val testObserver = bills.fetchBillWithRelation(billId = 1249).test()

        testObserver.awaitValue()
        val model = testObserver.value().data
        assertNotNull(model)
        assertEquals(1249L, model?.bill?.billId)
        assertEquals(model?.bill?.accountId, model?.account?.account?.accountId)

        tearDown()
    }

    @Test
    fun testBillsLinkToMerchants() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == BillsAPI.URL_BILLS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.bills_valid))
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

        bills.refreshBills { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(3)

        val testObserver = bills.fetchBillWithRelation(billId = 1249).test()

        testObserver.awaitValue()
        val model = testObserver.value().data
        assertNotNull(model)
        assertEquals(1249L, model?.bill?.billId)
        assertEquals(model?.bill?.merchantId, model?.merchant?.merchantId)

        tearDown()
    }

    @Test
    fun testBillsLinkToTransactionCategories() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == BillsAPI.URL_BILLS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.bills_valid))
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

        bills.refreshBills { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(3)

        val testObserver = bills.fetchBillWithRelation(billId = 1249).test()

        testObserver.awaitValue()
        val model = testObserver.value().data
        assertNotNull(model)
        assertEquals(1249L, model?.bill?.billId)
        assertEquals(model?.bill?.categoryId, model?.transactionCategory?.transactionCategoryId)

        tearDown()
    }

    // Bill Payment Tests

    @Test
    fun testFetchBillPaymentByID() {
        initSetup()

        val data1 = testBillPaymentResponseData(billPaymentId = 100, date = "2019-01-01")
        val data2 = testBillPaymentResponseData(billPaymentId = 101, date = "2019-02-01")
        val data3 = testBillPaymentResponseData(billPaymentId = 102, date = "2019-02-06")
        val data4 = testBillPaymentResponseData(billPaymentId = 103, date = "2019-04-01")
        val data5 = testBillPaymentResponseData(billPaymentId = 104, date = "2019-04-30")
        val list = mutableListOf(data1, data2, data3, data4, data5)

        database.billPayments().insertAll(*list.map { it.toBillPayment() }.toList().toTypedArray())

        val testObserver = bills.fetchBillPayment(data3.billPaymentId).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(data3.billPaymentId, testObserver.value().data?.billPaymentId)

        tearDown()
    }

    @Test
    fun testFetchBillPayments() {
        initSetup()

        val data1 = testBillPaymentResponseData(billPaymentId = 100, date = "2019-01-01")
        val data2 = testBillPaymentResponseData(billPaymentId = 101, date = "2019-02-01")
        val data3 = testBillPaymentResponseData(billPaymentId = 102, date = "2019-02-06")
        val data4 = testBillPaymentResponseData(billPaymentId = 103, date = "2019-04-01")
        val data5 = testBillPaymentResponseData(billPaymentId = 104, date = "2019-04-30")
        val list = mutableListOf(data1, data2, data3, data4, data5)

        database.billPayments().insertAll(*list.map { it.toBillPayment() }.toList().toTypedArray())

        val testObserver = bills.fetchBillPayments(fromDate = "2019-02-06", toDate = "2019-04-30").test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(3, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchBillPaymentsByBillIdDated() {
        initSetup()

        val data1 = testBillPaymentResponseData(billPaymentId = 100, billId = 200, date = "2019-01-01")
        val data2 = testBillPaymentResponseData(billPaymentId = 101, billId = 200, date = "2019-02-01")
        val data3 = testBillPaymentResponseData(billPaymentId = 102, billId = 201, date = "2019-02-06")
        val data4 = testBillPaymentResponseData(billPaymentId = 103, billId = 200, date = "2019-04-01")
        val data5 = testBillPaymentResponseData(billPaymentId = 104, billId = 201, date = "2019-04-30")
        val list = mutableListOf(data1, data2, data3, data4, data5)

        database.billPayments().insertAll(*list.map { it.toBillPayment() }.toList().toTypedArray())

        val testObserver = bills.fetchBillPaymentsByBillId(billId = 200, fromDate = "2019-02-01", toDate = "2019-04-30").test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchBillPaymentsByBillId() {
        initSetup()

        val data1 = testBillPaymentResponseData(billPaymentId = 100, billId = 200, date = "2019-01-01")
        val data2 = testBillPaymentResponseData(billPaymentId = 101, billId = 200, date = "2019-02-01")
        val data3 = testBillPaymentResponseData(billPaymentId = 102, billId = 201, date = "2019-02-06")
        val data4 = testBillPaymentResponseData(billPaymentId = 103, billId = 200, date = "2019-04-01")
        val data5 = testBillPaymentResponseData(billPaymentId = 104, billId = 201, date = "2019-04-30")
        val list = mutableListOf(data1, data2, data3, data4, data5)

        database.billPayments().insertAll(*list.map { it.toBillPayment() }.toList().toTypedArray())

        val testObserver = bills.fetchBillPaymentsByBillId(billId = 200).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(3, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchBillPaymentByIDWithRelation() {
        initSetup()

        database.bills().insert(testBillResponseData(billId = 123, accountId = 345, merchantId = 678, transactionCategoryId = 567).toBill())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 456, billId = 123, date = "2019-01-01").toBillPayment())

        val testObserver = bills.fetchBillPaymentWithRelation(billPaymentId = 456).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(456L, testObserver.value().data?.billPayment?.billPaymentId)

        tearDown()
    }

    @Test
    fun testFetchBillPaymentsWithRelation() {
        initSetup()

        database.bills().insert(testBillResponseData(billId = 123, accountId = 345, merchantId = 678, transactionCategoryId = 567).toBill())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 456, billId = 123, date = "2019-01-01").toBillPayment())

        val testObserver = bills.fetchBillPaymentsWithRelation(fromDate = "2019-01-01", toDate = "2019-04-30").test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(1, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchBillPaymentsByBillIdDatedWithRelation() {
        initSetup()

        database.bills().insert(testBillResponseData(billId = 123, accountId = 345, merchantId = 678, transactionCategoryId = 567).toBill())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 456, billId = 123, date = "2019-01-01").toBillPayment())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 457, billId = 123, date = "2019-02-01").toBillPayment())

        val testObserver = bills.fetchBillPaymentsByBillIdWithRelation(billId = 123, fromDate = "2019-02-01", toDate = "2019-04-30").test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(1, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchBillPaymentsByBillIdWithRelation() {
        initSetup()

        database.bills().insert(testBillResponseData(billId = 123, accountId = 345, merchantId = 678, transactionCategoryId = 567).toBill())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 456, billId = 123, date = "2019-01-01").toBillPayment())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 457, billId = 123, date = "2019-02-01").toBillPayment())

        val testObserver = bills.fetchBillPaymentsByBillIdWithRelation(billId = 123).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testDeleteBillPayment() {
        initSetup()

        val billPaymentId: Long = 12345

        val requestPath = "bills/payments/$billPaymentId"

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = billPaymentId).toBillPayment())

        var testObserver = bills.fetchBillPayment(billPaymentId).test()

        testObserver.awaitValue()
        val model = testObserver.value().data
        assertNotNull(model)
        assertEquals(billPaymentId, model?.billPaymentId)

        bills.deleteBillPayment(billPaymentId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            testObserver = bills.fetchBillPayment(billPaymentId).test()

            testObserver.awaitValue()
            assertNull(testObserver.value().data)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshBillPayments() {
        initSetup()

        val fromDate = "2018-12-01"
        val toDate = "2021-12-02"
        val requestPath = "${BillsAPI.URL_BILL_PAYMENTS}?from_date=$fromDate&to_date=$toDate"

        val body = readStringFromJson(app, R.raw.bill_payments_2018_12_01_valid)
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

        bills.refreshBillPayments(fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = bills.fetchBillPayments(fromDate = fromDate, toDate = toDate).test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)
            assertEquals(7, testObserver.value().data?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateBillPayment() {
        initSetup()

        val billPaymentId: Long = 12345

        val requestPath = "bills/payments/$billPaymentId"

        val body = readStringFromJson(app, R.raw.bill_payment_id_12345)
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

        val billPayment = testBillPaymentResponseData(billPaymentId = 12345).toBillPayment()

        database.billPayments().insert(billPayment)

        bills.updateBillPayment(billPaymentId = billPaymentId, paid = true) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = bills.fetchBillPayment(billPaymentId = 12345).test()

            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(12345L, models?.billPaymentId)
            assertEquals("Optus Internet", models?.name)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testBillsLinkToBills() {
        initSetup()

        val fromDate = "2018-12-01"
        val toDate = "2021-12-02"
        val requestPath = "${BillsAPI.URL_BILL_PAYMENTS}?from_date=$fromDate&to_date=$toDate"

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.bill_payments_2018_12_01_valid))
                } else if (request?.trimmedPath == BillsAPI.URL_BILLS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.bills_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        bills.refreshBills { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        bills.refreshBillPayments(fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)
        }

        wait(3)

        val testObserver = bills.fetchBillPaymentWithRelation(billPaymentId = 7991).test()

        testObserver.awaitValue()
        val model = testObserver.value().data
        assertNotNull(model)
        assertEquals(7991L, model?.billPayment?.billPaymentId)
        assertEquals(model?.billPayment?.billId, model?.bill?.bill?.billId)

        tearDown()
    }

    @Test
    fun testLinkingRemoveCachedCascade() {
        initSetup()

        val body = readStringFromJson(app, R.raw.bills_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == BillsAPI.URL_BILLS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        database.bills().insert(testBillResponseData(billId = 123, accountId = 345, merchantId = 678, transactionCategoryId = 567).toBill())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 456, billId = 123, date = "2019-01-01").toBillPayment())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 457, billId = 123, date = "2019-02-01").toBillPayment())

        bills.fetchBill(billId = 123).test().apply {
            awaitValue()

            assertEquals(123L, value().data?.billId)
        }

        bills.fetchBillPayments(fromDate = "2019-01-01", toDate = "2019-04-01").test().apply {
            awaitValue()

            assertEquals(2, value().data?.size)
            assertEquals(456L, value().data?.get(0)?.billPaymentId)
            assertEquals(457L, value().data?.get(1)?.billPaymentId)
        }

        bills.refreshBills { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            bills.fetchBills().test().apply {
                awaitValue()

                assertNotNull(value().data)
                assertEquals(7, value().data?.size)
            }

            bills.fetchBill(billId = 123).test().apply {
                awaitValue()

                assertNull(value().data)
            }

            bills.fetchBillPayment(billPaymentId = 456).test().apply {
                awaitValue()

                assertNull(value().data)
            }

            bills.fetchBillPayment(billPaymentId = 457).test().apply {
                awaitValue()

                assertNull(value().data)
            }
        }

        wait(3)

        tearDown()
    }
}