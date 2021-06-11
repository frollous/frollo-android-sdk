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

package us.frollo.frollosdk.payments

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.BaseAndroidTest
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.model.coredata.contacts.PayIDType
import us.frollo.frollosdk.network.api.PaymentsAPI
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PaymentsTest : BaseAndroidTest() {

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
    }

    // Make Payment Tests

    @Test
    fun testPayAnyone() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.payment_pay_anyone_response)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == PaymentsAPI.URL_PAY_ANYONE) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        payments.payAnyone(
            accountHolder = "Joe Blow",
            accountNumber = "98765432",
            amount = BigDecimal("542.37"),
            bsb = "123456",
            sourceAccountId = 42
        ) { resource ->

            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val response = resource.data
            assertNotNull(response)

            assertEquals(BigDecimal("542.37"), response?.amount)
            assertEquals("Joe Blow", response?.destinationAccountHolder)
            assertEquals("123456", response?.destinationBSB)
            assertEquals(34L, response?.transactionId)
            assertEquals("XXX", response?.transactionReference)
            assertEquals("confirmed", response?.status)
            assertEquals("2020-12-25", response?.paymentDate)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(PaymentsAPI.URL_PAY_ANYONE, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testPayAnyoneFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        payments.payAnyone(
            accountHolder = "Joe Blow",
            accountNumber = "98765432",
            amount = BigDecimal("542.37"),
            bsb = "123456",
            sourceAccountId = 42
        ) { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (resource.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testTransferPayment() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.payment_transfer_response)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == PaymentsAPI.URL_TRANSFER) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        payments.transferPayment(
            amount = BigDecimal("542.37"),
            description = "Visible to both sides",
            destinationAccountId = 43,
            sourceAccountId = 42,
            paymentDate = "2020-12-25"
        ) { resource ->

            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val response = resource.data
            assertNotNull(response)

            assertEquals(BigDecimal("542.37"), response?.amount)
            assertEquals(43L, response?.destinationAccountId)
            assertEquals("Everyday Txn", response?.destinationAccountHolder)
            assertEquals(42L, response?.sourceAccountId)
            assertEquals(34L, response?.transactionId)
            assertEquals("XXX", response?.transactionReference)
            assertEquals("scheduled", response?.status)
            assertEquals("2020-12-25", response?.paymentDate)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(PaymentsAPI.URL_TRANSFER, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testTransferPaymentLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        payments.transferPayment(
            amount = BigDecimal("542.37"),
            description = "Visible to both sides",
            destinationAccountId = 43,
            sourceAccountId = 42,
            paymentDate = "2020-12-25"
        ) { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (resource.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testBPayPayment() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.payment_bpay_response)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == PaymentsAPI.URL_BPAY) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        payments.bpayPayment(
            amount = BigDecimal("542.37"),
            billerCode = "123456",
            crn = "98765432122232",
            reference = "Visible to customer",
            sourceAccountId = 42,
            paymentDate = "2020-12-25"
        ) { resource ->

            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val response = resource.data
            assertNotNull(response)

            assertEquals(BigDecimal("542.37"), response?.amount)
            assertEquals("123456", response?.billerCode)
            assertEquals("ACME Inc.", response?.billerName)
            assertEquals("98765432122232", response?.crn)
            assertEquals(42L, response?.sourceAccountId)
            assertEquals(34L, response?.transactionId)
            assertEquals("XXX", response?.transactionReference)
            assertEquals("pending", response?.status)
            assertEquals("2020-12-25", response?.paymentDate)
            assertEquals("Visible to customer", response?.reference)
            assertEquals("Everyday Txn", response?.sourceAccountName)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(PaymentsAPI.URL_BPAY, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testBPayPaymentLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        payments.bpayPayment(
            amount = BigDecimal("542.37"),
            billerCode = "123456",
            crn = "98765432122232",
            reference = "Visible to customer",
            sourceAccountId = 42,
            paymentDate = "2020-12-25"
        ) { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (resource.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testPayIdPayment() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.payment_npp_response)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == PaymentsAPI.URL_PAY_ID) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        payments.payIdPayment(
            payId = "user@example.com",
            type = PayIDType.EMAIL,
            payIDName = "Example Name",
            amount = BigDecimal("542.37"),
            description = "Test",
            reference = "ABC123",
            sourceAccountId = 42,
            paymentDate = "2020-12-25"
        ) { resource ->

            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val response = resource.data
            assertNotNull(response)

            assertEquals(BigDecimal("542.37"), response?.amount)
            assertEquals("Joe Blow", response?.destinationAccountHolder)
            assertEquals("Visible to customer", response?.description)
            assertEquals(42L, response?.sourceAccountId)
            assertEquals("Everyday Txn", response?.sourceAccountName)
            assertEquals("VLLTAU22XXXN20210202000000000770820", response?.transactionReference)
            assertEquals("confirmed", response?.status)
            assertEquals("2020-12-25", response?.paymentDate)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(PaymentsAPI.URL_PAY_ID, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testPayIdPaymentLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        payments.payIdPayment(
            payId = "user@example.com",
            type = PayIDType.EMAIL,
            payIDName = "Example Name",
            amount = BigDecimal("542.37"),
            description = "Test",
            reference = "ABC123",
            sourceAccountId = 42,
            paymentDate = "2020-12-25"
        ) { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (resource.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testNppPayment() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.payment_npp_response)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == PaymentsAPI.URL_NPP) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        payments.payAnyoneNppPayment(
            accountHolder = "Jow Blow",
            accountNumber = "98765432",
            bsb = "123456",
            amount = BigDecimal("542.37"),
            sourceAccountId = 42
        ) { resource ->

            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val response = resource.data
            assertNotNull(response)

            assertEquals(BigDecimal("542.37"), response?.amount)
            assertEquals("Joe Blow", response?.destinationAccountHolder)
            assertEquals("Visible to customer", response?.description)
            assertEquals(42L, response?.sourceAccountId)
            assertEquals("Everyday Txn", response?.sourceAccountName)
            assertEquals("VLLTAU22XXXN20210202000000000770820", response?.transactionReference)
            assertEquals("confirmed", response?.status)
            assertEquals("2020-12-25", response?.paymentDate)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(PaymentsAPI.URL_NPP, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testNppPaymentLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        payments.payAnyoneNppPayment(
            accountHolder = "Jow Blow",
            accountNumber = "98765432",
            bsb = "123456",
            amount = BigDecimal("542.37"),
            sourceAccountId = 42
        ) { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (resource.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    // Verify Methods Tests

    @Test
    fun testVerifyPayAnyoneValid() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.payment_verify_pay_anyone_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == PaymentsAPI.URL_VERIFY_PAY_ANYONE) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        payments.verifyPayAnyone(
            accountHolder = "Joe Blow",
            accountNumber = "98765432",
            bsb = "123456"
        ) { resource ->

            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val response = resource.data
            assertNotNull(response)

            assertEquals("98765432", response?.accountNumber)
            assertEquals("Joe Blow", response?.accountHolder)
            assertEquals("123456", response?.bsb)
            assertEquals("Westpac Manly Corso", response?.bsbName)
            assertEquals(true, response?.valid)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(PaymentsAPI.URL_VERIFY_PAY_ANYONE, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testVerifyPayAnyoneInvalid() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.payment_verify_pay_anyone_invalid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == PaymentsAPI.URL_VERIFY_PAY_ANYONE) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        payments.verifyPayAnyone(
            accountHolder = "Joe Blow",
            accountNumber = "98765432",
            bsb = "123456"
        ) { resource ->

            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val response = resource.data
            assertNotNull(response)

            assertNull(response?.accountNumber)
            assertNull(response?.accountHolder)
            assertNull(response?.bsb)
            assertNull(response?.bsbName)
            assertEquals(false, response?.valid)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(PaymentsAPI.URL_VERIFY_PAY_ANYONE, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testVerifyPayAnyoneFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        payments.verifyPayAnyone(
            accountHolder = "Joe Blow",
            accountNumber = "98765432",
            bsb = "123456"
        ) { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (resource.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testVerifyPayIdInvalid() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.payment_verify_payid_response)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == PaymentsAPI.URL_VERIFY_PAY_ID) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        payments.verifyPayId(
            payId = "+61411111111",
            type = PayIDType.MOBILE
        ) { resource ->

            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val response = resource.data
            assertNotNull(response)

            assertEquals("John Doe", response?.name)
            assertEquals("+61411111111", response?.payId)
            assertEquals(PayIDType.MOBILE, response?.type)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(PaymentsAPI.URL_VERIFY_PAY_ID, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testVerifyPayIdFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        payments.verifyPayId(
            payId = "+61411111111",
            type = PayIDType.MOBILE
        ) { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (resource.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testVerifyBSB() {
        initSetup()

        val signal = CountDownLatch(1)

        val bsb = "517000"
        val requestPath = "bsb_details/$bsb"

        val body = readStringFromJson(app, R.raw.payment_verify_bsb_response)
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

        payments.verifyBSB(bsb) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val response = resource.data
            assertNotNull(response)

            assertEquals("517-000", response?.bsb)
            assertEquals("VOL", response?.institutionMnemonic)
            assertEquals("Volt Bank Limited", response?.name)
            assertEquals("Level 3, 41 McLaren Street", response?.streetAddress)
            assertEquals("North Sydney", response?.suburb)
            assertEquals("NSW", response?.state)
            assertEquals("2060", response?.postcode)
            assertEquals("EH", response?.paymentsFlags)
            assertTrue(response?.isNPPAllowed == true)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testVerifyBSBFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        payments.verifyBSB("517000") { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (resource.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }
}
