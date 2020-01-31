/*
 * Copyright 2020 Frollo
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

package us.frollo.frollosdk.aggregation

import com.jraska.livedata.test
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.BaseAndroidTest
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.mapping.toTransaction
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionBaseType
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionDescription
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionStatus
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.model.testTransactionResponseData
import us.frollo.frollosdk.network.api.AggregationAPI
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AggregationTest2 : BaseAndroidTest() {

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
    }

    @Test
    fun testRefreshTransactionsSameDayPage1() {
        initSetup()

        val data1 = testTransactionResponseData(transactionId = 1, transactionDate = "2008-08-01")
        val data2 = testTransactionResponseData(transactionId = 2, transactionDate = "2008-08-01")
        val data3 = testTransactionResponseData(transactionId = 9, transactionDate = "2008-08-01")
        val data4 = testTransactionResponseData(transactionId = 10, transactionDate = "2008-08-01")
        val list = mutableListOf(data1, data2, data3, data4)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val signal = CountDownLatch(1)
        val body = readStringFromJson(app, R.raw.transactions_same_day_pagination_1)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS}") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransactionsWithPagination { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)
            assertNotNull(resource.data)
            val testObserver = aggregation.fetchTransactionsNew().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            val ids = models?.map { it.transactionId }
            assertEquals(3, models?.size)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshTransactionsSameDayPage2() {
        initSetup()

        val data1 = testTransactionResponseData(transactionId = 6, transactionDate = "2008-08-01")
        val data2 = testTransactionResponseData(transactionId = 7, transactionDate = "2008-08-01")
        val data3 = testTransactionResponseData(transactionId = 8, transactionDate = "2008-08-01")
        val data4 = testTransactionResponseData(transactionId = 9, transactionDate = "2008-08-01")
        val data5 = testTransactionResponseData(transactionId = 10, transactionDate = "2008-08-01")
        val list = mutableListOf(data1, data2, data3, data4, data5)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val signal = CountDownLatch(1)
        val body2 = readStringFromJson(app, R.raw.transactions_same_day_pagination_2)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS}?after=1217548800000_7") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body2)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransactionsWithPagination(after = "1217548800000_7") { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)
            assertNotNull(resource.data)
            val testObserver = aggregation.fetchTransactionsNew().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            val ids = models?.map { it.transactionId }
            assertEquals(6, models?.size)

            signal.countDown()
        }

        signal.await(30, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshTransactionsSameDayPage3() {
        initSetup()

        val data0 = testTransactionResponseData(transactionId = 5, transactionDate = "2008-08-01")
        val data1 = testTransactionResponseData(transactionId = 6, transactionDate = "2008-08-01")
        val data2 = testTransactionResponseData(transactionId = 7, transactionDate = "2008-08-01")
        val data3 = testTransactionResponseData(transactionId = 8, transactionDate = "2008-08-01")
        val data4 = testTransactionResponseData(transactionId = 9, transactionDate = "2008-08-01")
        val data5 = testTransactionResponseData(transactionId = 10, transactionDate = "2008-08-01")
        val list = mutableListOf(data0, data1, data2, data3, data4, data5)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val signal = CountDownLatch(1)
        val body2 = readStringFromJson(app, R.raw.transactions_same_day_pagination_3)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS}?after=1217548800000_4") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body2)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransactionsWithPagination(after = "1217548800000_4") { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)
            assertNotNull(resource.data)
            val testObserver = aggregation.fetchTransactionsNew().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            val ids = models?.map { it.transactionId }
            assertEquals(10, models?.size)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)
        tearDown()
    }

    @Test
    fun testFetchTransactionsWithFilterNew1() {
        initSetup()

        val data0 = testTransactionResponseData(transactionId = 5, transactionDate = "2008-08-01")
        val data1 = testTransactionResponseData(transactionId = 6, transactionDate = "2008-08-01")
        val data2 = testTransactionResponseData(transactionId = 7, transactionDate = "2008-08-01")
        val data3 = testTransactionResponseData(transactionId = 8, transactionDate = "2008-08-01")
        val data4 = testTransactionResponseData(transactionId = 9, transactionDate = "2008-08-01")
        val data5 = testTransactionResponseData(transactionId = 10, transactionDate = "2008-08-01")

        val data6 = testTransactionResponseData(amount = BigDecimal(200))
        val data7 = testTransactionResponseData(amount = BigDecimal(565))

        val data8 = testTransactionResponseData(description = TransactionDescription("hello", "hi", "hey"))
        val data9 = testTransactionResponseData(description = TransactionDescription("jnkjk", "hkjnjki", "nice work man"))

        val data10 = testTransactionResponseData(merchantId = 1)
        val data11 = testTransactionResponseData(merchantId = 2)

        val data12 = testTransactionResponseData(accountId = 1)
        val data13 = testTransactionResponseData(accountId = 2)

        val data14 = testTransactionResponseData(categoryId = 1)
        val data15 = testTransactionResponseData(categoryId = 2)

        val data16 = testTransactionResponseData(budgetCategory = BudgetCategory.LIVING)
        val data17 = testTransactionResponseData(baseType = TransactionBaseType.DEBIT)
        val data18 = testTransactionResponseData(status = TransactionStatus.POSTED)
        val data19 = testTransactionResponseData(userTags = listOf("hi", "hello", "how are you"))
        val data20 = testTransactionResponseData(included = true)

        val list = mutableListOf(data0, data1, data2, data3, data4, data5, data6, data7, data8, data9, data10, data11, data12, data13, data14, data15, data16, data17, data18, data19, data20)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val signal = CountDownLatch(1)

        var testObserver = aggregation.fetchTransactionsNew(transactionIds = listOf(7, 8, 9)).test()
        testObserver.awaitValue()
        var models = testObserver.value().data
        assertEquals(3, models?.size)

        testObserver = aggregation.fetchTransactionsNew(fromDate = "2008-07-31", toDate = "2008-08-02", transactionIds = listOf(5, 6, 7)).test()
        testObserver.awaitValue()
        models = testObserver.value().data
        assertEquals(3, models?.size)

        testObserver = aggregation.fetchTransactionsNew(fromDate = "2008-07-31", toDate = "2008-08-02", transactionIds = listOf(5, 6, 7)).test()
        testObserver.awaitValue()
        models = testObserver.value().data
        assertEquals(3, models?.size)

        signal.countDown()

        signal.await(3, TimeUnit.SECONDS)
        tearDown()
    }

    @Test
    fun testFetchTransactionsWithFilterNew2() {
        initSetup()
        database.clearAllTables()

        val data0 = testTransactionResponseData(transactionId = 5, transactionDate = "2008-08-01")
        val data1 = testTransactionResponseData(transactionId = 6, transactionDate = "2008-08-01")
        val data2 = testTransactionResponseData(transactionId = 7, transactionDate = "2008-08-01")
        val data3 = testTransactionResponseData(transactionId = 8, transactionDate = "2008-08-01")
        val data4 = testTransactionResponseData(transactionId = 9, transactionDate = "2008-08-01")
        val data5 = testTransactionResponseData(transactionId = 10, transactionDate = "2008-08-01")

        val data6 = testTransactionResponseData(amount = BigDecimal(200))
        val data7 = testTransactionResponseData(amount = BigDecimal(565))

        val data8 = testTransactionResponseData(description = TransactionDescription("hello", "hi", "hey"))
        val data9 = testTransactionResponseData(description = TransactionDescription("jnkjk", "hkjnjki", "nice work man"))

        val data10 = testTransactionResponseData(merchantId = 1)
        val data11 = testTransactionResponseData(merchantId = 2)

        val data12 = testTransactionResponseData(accountId = 1)
        val data13 = testTransactionResponseData(accountId = 2)

        val data14 = testTransactionResponseData(categoryId = 1)
        val data15 = testTransactionResponseData(categoryId = 2)

        val data16 = testTransactionResponseData(budgetCategory = BudgetCategory.LIVING)
        val data17 = testTransactionResponseData(baseType = TransactionBaseType.DEBIT)
        val data18 = testTransactionResponseData(status = TransactionStatus.POSTED)
        val data19 = testTransactionResponseData(userTags = listOf("hi", "hello", "how are you"))
        val data20 = testTransactionResponseData(included = true)

        val list = mutableListOf(data0, data1, data2, data3, data4, data5, data6, data7, data8, data9, data10, data11, data12, data13, data14, data15, data16, data17, data18, data19, data20)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val signal = CountDownLatch(1)

        var testObserver = aggregation.fetchTransactionsNew(budgetCategory = BudgetCategory.LIVING).test()
        testObserver.awaitValue()
        var models = testObserver.value().data
        assertEquals(1, models?.size)

        testObserver = aggregation.fetchTransactionsNew(baseType = TransactionBaseType.DEBIT).test()
        testObserver.awaitValue()
        models = testObserver.value().data
        assertEquals(1, models?.size)

        testObserver = aggregation.fetchTransactionsNew(status = TransactionStatus.POSTED).test()
        testObserver.awaitValue()
        models = testObserver.value().data
        assertEquals(1, models?.size)

        testObserver = aggregation.fetchTransactionsNew(tags = listOf("hello")).test()
        testObserver.awaitValue()
        models = testObserver.value().data
        assertEquals(1, models?.size)

        testObserver = aggregation.fetchTransactionsNew(transactionIncluded = true).test()
        testObserver.awaitValue()
        models = testObserver.value().data
        assertEquals(1, models?.size)

        signal.countDown()

        signal.await(3, TimeUnit.SECONDS)
        tearDown()
    }

    @Test
    fun testFetchTransactionsWithFilterNew3() {
        initSetup()
        database.clearAllTables()

        val data0 = testTransactionResponseData(transactionId = 5, transactionDate = "2008-08-01")
        val data1 = testTransactionResponseData(transactionId = 6, transactionDate = "2008-08-01")
        val data2 = testTransactionResponseData(transactionId = 7, transactionDate = "2008-08-01")
        val data3 = testTransactionResponseData(transactionId = 8, transactionDate = "2008-08-01")
        val data4 = testTransactionResponseData(transactionId = 9, transactionDate = "2008-08-01")
        val data5 = testTransactionResponseData(transactionId = 10, transactionDate = "2008-08-01")

        val data6 = testTransactionResponseData(amount = BigDecimal(200))
        val data7 = testTransactionResponseData(amount = BigDecimal(565))

        val data8 = testTransactionResponseData(description = TransactionDescription("hello", "hi", "hey"))
        val data9 = testTransactionResponseData(description = TransactionDescription("jnkjk", "hkjnjki", "nice work man"))

        val data10 = testTransactionResponseData(merchantId = 1)
        val data11 = testTransactionResponseData(merchantId = 2)

        val data12 = testTransactionResponseData(accountId = 1)
        val data13 = testTransactionResponseData(accountId = 2)

        val data14 = testTransactionResponseData(categoryId = 1)
        val data15 = testTransactionResponseData(categoryId = 2)

        val data16 = testTransactionResponseData(budgetCategory = BudgetCategory.LIVING)
        val data17 = testTransactionResponseData(baseType = TransactionBaseType.DEBIT)
        val data18 = testTransactionResponseData(status = TransactionStatus.POSTED)
        val data19 = testTransactionResponseData(userTags = listOf("hi", "hello", "how are you"))
        val data20 = testTransactionResponseData(included = true)

        val list = mutableListOf(data0, data1, data2, data3, data4, data5, data6, data7, data8, data9, data10, data11, data12, data13, data14, data15, data16, data17, data18, data19, data20)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val signal = CountDownLatch(1)

        var testObserver = aggregation.fetchTransactionsNew(minAmount = 500, maxAmount = 1000).test()
        testObserver.awaitValue()
        var models = testObserver.value().data
        assertEquals(1, models?.size)

        testObserver = aggregation.fetchTransactionsNew(searchTerm = " work ").test()
        testObserver.awaitValue()
        models = testObserver.value().data
        assertEquals(1, models?.size)

        testObserver = aggregation.fetchTransactionsNew(merchantIds = listOf(1, 2)).test()
        testObserver.awaitValue()
        models = testObserver.value().data
        assertEquals(2, models?.size)

        testObserver = aggregation.fetchTransactionsNew(accountIds = listOf(1, 2)).test()
        testObserver.awaitValue()
        models = testObserver.value().data
        assertEquals(2, models?.size)

        testObserver = aggregation.fetchTransactionsNew(transactionCategoryIds = listOf(1, 2)).test()
        testObserver.awaitValue()
        models = testObserver.value().data
        assertEquals(2, models?.size)

        signal.countDown()

        signal.await(3, TimeUnit.SECONDS)
        tearDown()
    }

    @Test
    fun testRefreshTransactionsTwoDaysPage1() {
        initSetup()

        val data1 = testTransactionResponseData(transactionId = 12, transactionDate = "2008-08-03")
        val data2 = testTransactionResponseData(transactionId = 15, transactionDate = "2008-08-03")
        val data3 = testTransactionResponseData(transactionId = 14, transactionDate = "2008-08-03")
        val data4 = testTransactionResponseData(transactionId = 13, transactionDate = "2008-08-02")
        val list = mutableListOf(data1, data2, data3, data4)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val signal = CountDownLatch(1)
        val body = readStringFromJson(app, R.raw.transactions_two_days_pagination_1)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS}") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransactionsWithPagination { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)
            assertNotNull(resource.data)
            val testObserver = aggregation.fetchTransactionsNew().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            val ids = models?.map { it.transactionId }
            assertEquals(3, models?.size)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshTransactionsTwoDaysPage2() {
        initSetup()

        val data2 = testTransactionResponseData(transactionId = 15, transactionDate = "2008-08-03")
        val data3 = testTransactionResponseData(transactionId = 14, transactionDate = "2008-08-03")

        val data1 = testTransactionResponseData(transactionId = 12, transactionDate = "2008-08-02")
        val data4 = testTransactionResponseData(transactionId = 10, transactionDate = "2008-08-02")

        val list = mutableListOf(data1, data2, data3, data4)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val signal = CountDownLatch(1)
        val body = readStringFromJson(app, R.raw.transactions_two_days_pagination_2)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS}?after=1217548800000_7") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransactionsWithPagination(after = "1217548800000_7") { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)
            assertNotNull(resource.data)
            val testObserver = aggregation.fetchTransactionsNew().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            val ids = models?.map { it.transactionId }
            assertEquals(6, models?.size)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshTransactionsTwoDaysPage3() {
        initSetup()

        val data2 = testTransactionResponseData(transactionId = 15, transactionDate = "2008-08-03")
        val data3 = testTransactionResponseData(transactionId = 14, transactionDate = "2008-08-03")

        val data1 = testTransactionResponseData(transactionId = 12, transactionDate = "2008-08-02")
        val data4 = testTransactionResponseData(transactionId = 11, transactionDate = "2008-08-02")
        val data5 = testTransactionResponseData(transactionId = 9, transactionDate = "2008-08-02")
        val data6 = testTransactionResponseData(transactionId = 8, transactionDate = "2008-08-01")

        val data7 = testTransactionResponseData(transactionId = 5, transactionDate = "2008-08-01")

        val list = mutableListOf(data1, data2, data3, data4, data5, data6, data7)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val signal = CountDownLatch(1)
        val body = readStringFromJson(app, R.raw.transactions_two_days_pagination_3)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS}?after=1217548800000_7") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransactionsWithPagination(after = "1217548800000_7") { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)
            assertNotNull(resource.data)
            val testObserver = aggregation.fetchTransactionsNew().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            val ids = models?.map { it.transactionId }
            assertEquals(10, models?.size)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshTransactionsNDaysPage1() {
        initSetup()

        val data1 = testTransactionResponseData(transactionId = 151, transactionDate = "2018-08-31")
        val data2 = testTransactionResponseData(transactionId = 132, transactionDate = "2018-08-29")
        val data3 = testTransactionResponseData(transactionId = 131, transactionDate = "2018-08-28")
        val list = mutableListOf(data1, data2, data3)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val signal = CountDownLatch(1)
        val body = readStringFromJson(app, R.raw.transactions_n_days_pagination_1)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS}") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransactionsWithPagination { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)
            assertNotNull(resource.data)
            val testObserver = aggregation.fetchTransactionsNew().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            val ids = models?.map { it.transactionId }
            assertEquals(3, models?.size)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshTransactionsNDaysPage2() {
        initSetup()

        val data1 = testTransactionResponseData(transactionId = 150, transactionDate = "2018-08-30")
        val data2 = testTransactionResponseData(transactionId = 149, transactionDate = "2018-08-29")
        val data3 = testTransactionResponseData(transactionId = 130, transactionDate = "2018-08-28")

        val data4 = testTransactionResponseData(transactionId = 118, transactionDate = "2018-08-20")
        val data5 = testTransactionResponseData(transactionId = 121, transactionDate = "2018-08-22")
        val data6 = testTransactionResponseData(transactionId = 118, transactionDate = "2018-08-20")
        val data7 = testTransactionResponseData(transactionId = 128, transactionDate = "2018-08-25")

        val list = mutableListOf(data1, data2, data3, data4, data5, data6, data7)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val signal = CountDownLatch(1)
        val body = readStringFromJson(app, R.raw.transactions_n_days_pagination_2)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS}?after=1217548800000_7") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransactionsWithPagination(after = "1217548800000_7") { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)
            assertNotNull(resource.data)
            val testObserver = aggregation.fetchTransactionsNew().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            val ids = models?.map { it.transactionId }
            assertEquals(6, models?.size)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshTransactionsNDaysPage3() {
        initSetup()

        val data1 = testTransactionResponseData(transactionId = 150, transactionDate = "2018-08-30")
        val data2 = testTransactionResponseData(transactionId = 149, transactionDate = "2018-08-29")
        val data3 = testTransactionResponseData(transactionId = 130, transactionDate = "2018-08-28")

        val data4 = testTransactionResponseData(transactionId = 129, transactionDate = "2018-08-25")
        val data5 = testTransactionResponseData(transactionId = 119, transactionDate = "2018-08-22")
        val data6 = testTransactionResponseData(transactionId = 117, transactionDate = "2018-08-20")

        val data7 = testTransactionResponseData(transactionId = 128, transactionDate = "2018-08-25")

        val data8 = testTransactionResponseData(transactionId = 103, transactionDate = "2018-08-01")
        val data9 = testTransactionResponseData(transactionId = 104, transactionDate = "2018-08-10")
        val data10 = testTransactionResponseData(transactionId = 114, transactionDate = "2018-08-18")

        val list = mutableListOf(data1, data2, data3, data4, data5, data6, data7, data8, data9, data10)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val signal = CountDownLatch(1)
        val body = readStringFromJson(app, R.raw.transactions_n_days_pagination_3)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS}?after=1217548800000_7") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransactionsWithPagination(after = "1217548800000_7") { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)
            assertNotNull(resource.data)
            val testObserver = aggregation.fetchTransactionsNew().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            val ids = models?.map { it.transactionId }
            assertEquals(11, models?.size)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }
}