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
import us.frollo.frollosdk.model.testTransactionResponseData
import us.frollo.frollosdk.network.api.AggregationAPI
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
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
    fun testRefreshTransactionsWithPaginationPage1() {
        initSetup()

        val data1 = testTransactionResponseData(transactionId = 1, transactionDate = "2008-08-01")
        val data2 = testTransactionResponseData(transactionId = 2, transactionDate = "2008-08-01")
        val data3 = testTransactionResponseData(transactionId = 9, transactionDate = "2008-08-01")
        val data4 = testTransactionResponseData(transactionId = 10, transactionDate = "2008-08-01")
        val list = mutableListOf(data1, data2, data3, data4)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val signal = CountDownLatch(1)
        val body = readStringFromJson(app, R.raw.transactions_2018_08_01_pagination_valid)
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
            val testObserver = aggregation.fetchTransactions().test()
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
    fun testRefreshTransactionsWithPaginationPage2() {
        initSetup()

        val data1 = testTransactionResponseData(transactionId = 6, transactionDate = "2008-08-01")
        val data2 = testTransactionResponseData(transactionId = 7, transactionDate = "2008-08-01")
        val data3 = testTransactionResponseData(transactionId = 8, transactionDate = "2008-08-01")
        val data4 = testTransactionResponseData(transactionId = 9, transactionDate = "2008-08-01")
        val data5 = testTransactionResponseData(transactionId = 10, transactionDate = "2008-08-01")
        val list = mutableListOf(data1, data2, data3, data4, data5)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val signal = CountDownLatch(1)
        val body2 = readStringFromJson(app, R.raw.transactions_2018_08_01_pagination_2_valid)
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
            val testObserver = aggregation.fetchTransactions().test()
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
    fun testRefreshTransactionsWithPaginationPage3() {
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
        val body2 = readStringFromJson(app, R.raw.transactions_2018_08_01_pagination_3_valid)
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
            val testObserver = aggregation.fetchTransactions().test()
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
}