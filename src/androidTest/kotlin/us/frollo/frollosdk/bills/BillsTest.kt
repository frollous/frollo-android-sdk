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

import com.jraska.livedata.test
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.BaseAndroidTest
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.extensions.toString
import us.frollo.frollosdk.mapping.toAccount
import us.frollo.frollosdk.mapping.toBill
import us.frollo.frollosdk.mapping.toBillPayment
import us.frollo.frollosdk.mapping.toMerchant
import us.frollo.frollosdk.mapping.toTransactionCategory
import us.frollo.frollosdk.model.coredata.bills.Bill
import us.frollo.frollosdk.model.coredata.bills.BillFrequency
import us.frollo.frollosdk.model.coredata.bills.BillPaymentStatus
import us.frollo.frollosdk.model.testAccountResponseData
import us.frollo.frollosdk.model.testBillPaymentResponseData
import us.frollo.frollosdk.model.testBillResponseData
import us.frollo.frollosdk.model.testMerchantResponseData
import us.frollo.frollosdk.model.testTransactionCategoryResponseData
import us.frollo.frollosdk.network.api.AggregationAPI
import us.frollo.frollosdk.network.api.BillsAPI
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class BillsTest : BaseAndroidTest() {

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
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

        val data1 = testBillResponseData(frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.DUE)
        val data2 = testBillResponseData(frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.OVERDUE)
        val data3 = testBillResponseData(frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.PAID)
        val data4 = testBillResponseData(frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.DUE)
        val data5 = testBillResponseData(frequency = BillFrequency.ANNUALLY, paymentStatus = BillPaymentStatus.DUE)
        val data6 = testBillResponseData(frequency = BillFrequency.BIANNUALLY, paymentStatus = BillPaymentStatus.DUE)
        val data7 = testBillResponseData(frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.DUE)
        val list = mutableListOf(data1, data2, data3, data4, data5, data6, data7)

        database.bills().insertAll(*list.map { it.toBill() }.toList().toTypedArray())

        val testObserver = bills.fetchBills(frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.DUE).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(3, testObserver.value().data?.size)

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
        database.bills().insert(testBillResponseData(billId = 123, accountId = 345, merchantId = 678, transactionCategoryId = 567, frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.DUE).toBill())
        database.bills().insert(testBillResponseData(billId = 124, accountId = 345, merchantId = 679, transactionCategoryId = 567, frequency = BillFrequency.BIANNUALLY, paymentStatus = BillPaymentStatus.DUE).toBill())
        database.bills().insert(testBillResponseData(billId = 125, accountId = 345, merchantId = 676, transactionCategoryId = 567, frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.DUE).toBill())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 456, billId = 123).toBillPayment())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 457, billId = 123).toBillPayment())

        val testObserver = bills.fetchBillsWithRelation(frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.DUE).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

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

        val signal = CountDownLatch(1)

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

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(BillsAPI.URL_BILLS, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testCreateBillFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        val date = LocalDate.now().plusDays(1).toString(Bill.DATE_FORMAT_PATTERN)

        bills.createBill(transactionId = 987, frequency = BillFrequency.MONTHLY, nextPaymentDate = date) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testCreateBillManually() {
        initSetup()

        val signal = CountDownLatch(1)

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

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(BillsAPI.URL_BILLS, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testDeleteBill() {
        initSetup()

        val signal = CountDownLatch(1)

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

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testDeleteBillFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        bills.deleteBill(12345) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshBills() {
        initSetup()

        val signal = CountDownLatch(1)

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

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(BillsAPI.URL_BILLS, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshBillsFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        bills.refreshBills { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshBillById() {
        initSetup()

        val signal = CountDownLatch(1)

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

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshBillByIdFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        bills.refreshBill(12345) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testUpdateBill() {
        initSetup()

        val signal = CountDownLatch(1)

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

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testUpdateBillByIdFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        bills.updateBill(testBillResponseData(billId = 12345).toBill()) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testBillsLinkToAccounts() {
        initSetup()

        val signal = CountDownLatch(2)

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

            signal.countDown()
        }

        bills.refreshBills { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

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

        val signal = CountDownLatch(2)

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

            signal.countDown()
        }

        bills.refreshBills { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

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

        val signal = CountDownLatch(2)

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

            signal.countDown()
        }

        bills.refreshBills { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

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

        val data1 = testBillPaymentResponseData(billPaymentId = 100, date = "2019-01-01", frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.DUE)
        val data2 = testBillPaymentResponseData(billPaymentId = 101, date = "2019-02-01", frequency = BillFrequency.WEEKLY, paymentStatus = BillPaymentStatus.DUE)
        val data3 = testBillPaymentResponseData(billPaymentId = 102, date = "2019-02-06", frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.DUE)
        val data4 = testBillPaymentResponseData(billPaymentId = 103, date = "2019-04-01", frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.PAID)
        val data5 = testBillPaymentResponseData(billPaymentId = 104, date = "2019-04-30", frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.DUE)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        database.billPayments().insertAll(*list.map { it.toBillPayment() }.toList().toTypedArray())

        val testObserver = bills.fetchBillPayments(
                fromDate = "2019-02-06",
                toDate = "2019-04-30",
                frequency = BillFrequency.MONTHLY,
                paymentStatus = BillPaymentStatus.DUE).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchBillPaymentsByBillIdDated() {
        initSetup()

        val data1 = testBillPaymentResponseData(billPaymentId = 100, billId = 200, date = "2019-01-01", frequency = BillFrequency.MONTHLY)
        val data2 = testBillPaymentResponseData(billPaymentId = 101, billId = 200, date = "2019-02-01", frequency = BillFrequency.ANNUALLY)
        val data3 = testBillPaymentResponseData(billPaymentId = 102, billId = 201, date = "2019-02-06", frequency = BillFrequency.MONTHLY)
        val data4 = testBillPaymentResponseData(billPaymentId = 103, billId = 200, date = "2019-04-01", frequency = BillFrequency.MONTHLY)
        val data5 = testBillPaymentResponseData(billPaymentId = 104, billId = 200, date = "2019-04-15", frequency = BillFrequency.MONTHLY)
        val data6 = testBillPaymentResponseData(billPaymentId = 105, billId = 201, date = "2019-04-30", frequency = BillFrequency.MONTHLY)
        val list = mutableListOf(data1, data2, data3, data4, data5, data6)

        database.billPayments().insertAll(*list.map { it.toBillPayment() }.toList().toTypedArray())

        val testObserver = bills.fetchBillPayments(billId = 200, fromDate = "2019-02-01", toDate = "2019-04-30", frequency = BillFrequency.MONTHLY).test()

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

        val testObserver = bills.fetchBillPayments(billId = 200).test()

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
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 456, billId = 123, date = "2019-01-01", frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.PAID).toBillPayment())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 457, billId = 123, date = "2019-02-01", frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.DUE).toBillPayment())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 458, billId = 123, date = "2019-03-01", frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.DUE).toBillPayment())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 459, billId = 123, date = "2019-04-01", frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.FUTURE).toBillPayment())

        val testObserver = bills.fetchBillPaymentsWithRelation(fromDate = "2019-01-01", toDate = "2019-04-30", frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.DUE).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchBillPaymentsByBillIdDatedWithRelation() {
        initSetup()

        database.bills().insert(testBillResponseData(billId = 123, accountId = 345, merchantId = 678, transactionCategoryId = 567).toBill())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 456, billId = 123, date = "2019-01-01").toBillPayment())
        database.billPayments().insert(testBillPaymentResponseData(billPaymentId = 457, billId = 123, date = "2019-02-01").toBillPayment())

        val testObserver = bills.fetchBillPaymentsWithRelation(billId = 123, fromDate = "2019-02-01", toDate = "2019-04-30").test()

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

        val testObserver = bills.fetchBillPaymentsWithRelation(billId = 123).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testDeleteBillPayment() {
        initSetup()

        val signal = CountDownLatch(1)

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

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testDeleteBillPaymentFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        bills.deleteBillPayment(12345) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshBillPayments() {
        initSetup()

        val signal = CountDownLatch(1)

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

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshBillPaymentsFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        bills.refreshBillPayments(fromDate = "2018-12-01", toDate = "2021-12-02") { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testUpdateBillPayment() {
        initSetup()

        val signal = CountDownLatch(1)

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

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testUpdateBillPaymentFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        bills.updateBillPayment(billPaymentId = 12345, paid = true) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testBillPaymentsLinkToBills() {
        initSetup()

        val signal = CountDownLatch(2)

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

            signal.countDown()
        }

        bills.refreshBillPayments(fromDate = fromDate, toDate = toDate) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

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

        val signal = CountDownLatch(1)

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

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }
}