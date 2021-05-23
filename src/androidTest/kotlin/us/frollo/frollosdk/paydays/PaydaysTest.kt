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

package us.frollo.frollosdk.paydays

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
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.mapping.toPayday
import us.frollo.frollosdk.model.coredata.payday.PaydayFrequency
import us.frollo.frollosdk.model.coredata.payday.PaydayStatus
import us.frollo.frollosdk.model.testPaydayResponseData
import us.frollo.frollosdk.network.api.PaydayAPI
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PaydaysTest : BaseAndroidTest() {

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
    }

    @Test
    fun testFetchPayday() {
        initSetup()

        database.payday().insert(
            testPaydayResponseData(
                status = PaydayStatus.CONFIRMED,
                frequency = PaydayFrequency.MONTHLY
            ).toPayday()
        )

        val testObserver2 = paydays.fetchPayday().test()
        testObserver2.awaitValue()
        val model = testObserver2.value()
        assertNotNull(model)
        assertEquals(PaydayStatus.CONFIRMED, model?.status)
        assertEquals(PaydayFrequency.MONTHLY, model?.frequency)
        assertEquals("2021-01-31", model?.nextDate)
        assertEquals("2020-12-31", model?.previousDate)

        tearDown()
    }

    @Test
    fun testRefreshPayday() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.payday)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == PaydayAPI.URL_PAYDAY) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        paydays.refreshPayday { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = paydays.fetchPayday().test()
            testObserver.awaitValue()
            val model = testObserver.value()
            assertNotNull(model)

            assertEquals(PaydayStatus.ESTIMATED, model?.status)
            assertEquals(PaydayFrequency.MONTHLY, model?.frequency)
            assertEquals("2021-01-31", model?.nextDate)
            assertEquals("2020-12-31", model?.previousDate)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(PaydayAPI.URL_PAYDAY, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshPaydayFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        paydays.refreshPayday() { result ->
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
    fun testUpdatePayday() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.payday)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == PaydayAPI.URL_PAYDAY) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val data = testPaydayResponseData(frequency = PaydayFrequency.WEEKLY).toPayday()

        database.payday().insert(data)

        paydays.updatePayday(frequency = PaydayFrequency.MONTHLY) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = paydays.fetchPayday().test()

            testObserver.awaitValue()
            val model = testObserver.value()
            assertNotNull(model)
            assertEquals(PaydayFrequency.MONTHLY, model?.frequency)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(PaydayAPI.URL_PAYDAY, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testUpdatePaydayFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        paydays.updatePayday(frequency = PaydayFrequency.MONTHLY) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }
}
