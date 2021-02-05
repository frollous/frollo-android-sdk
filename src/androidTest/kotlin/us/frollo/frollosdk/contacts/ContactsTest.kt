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

package us.frollo.frollosdk.contacts

import com.jraska.livedata.test
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
import us.frollo.frollosdk.base.PaginatedResult
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.mapping.toContact
import us.frollo.frollosdk.model.coredata.contacts.CRNType
import us.frollo.frollosdk.model.coredata.contacts.PayIDType
import us.frollo.frollosdk.model.coredata.contacts.PaymentDetails
import us.frollo.frollosdk.model.coredata.contacts.PaymentMethod
import us.frollo.frollosdk.model.testContactResponseData
import us.frollo.frollosdk.network.api.ContactsAPI
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ContactsTest : BaseAndroidTest() {

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
    }

    @Test
    fun testFetchContactById() {
        initSetup()

        val data1 = testContactResponseData(contactId = 100)
        val data2 = testContactResponseData(contactId = 101)
        val data3 = testContactResponseData(contactId = 102)

        val list = mutableListOf(data1, data2, data3)

        database.contacts().insertAll(*list.map { it.toContact() }.toList().toTypedArray())

        val testObserver = contacts.fetchContact(contactId = 101).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(101L, testObserver.value()?.contactId)

        tearDown()
    }

    @Test
    fun testFetchContacts() {
        initSetup()

        val data1 = testContactResponseData(contactId = 100)
        val data2 = testContactResponseData(contactId = 101)
        val data3 = testContactResponseData(contactId = 102)
        val data4 = testContactResponseData(contactId = 103)
        val data5 = testContactResponseData(contactId = 104)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        database.contacts().insertAll(*list.map { it.toContact() }.toList().toTypedArray())

        val testObserver = contacts.fetchContacts().test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(5, testObserver.value()?.size)

        tearDown()
    }

    @Test
    fun testFetchContactsFiltered() {
        initSetup()

        val data1 = testContactResponseData(contactId = 100, paymentMethod = PaymentMethod.PAY_ANYONE)
        val data2 = testContactResponseData(contactId = 101, paymentMethod = PaymentMethod.BPAY)
        val data3 = testContactResponseData(contactId = 102, paymentMethod = PaymentMethod.PAY_ANYONE)
        val data4 = testContactResponseData(contactId = 103, paymentMethod = PaymentMethod.PAY_ID)
        val data5 = testContactResponseData(contactId = 104, paymentMethod = PaymentMethod.INTERNATIONAL)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        database.contacts().insertAll(*list.map { it.toContact() }.toList().toTypedArray())

        val testObserver = contacts.fetchContacts(paymentMethod = PaymentMethod.PAY_ANYONE).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(2, testObserver.value()?.size)

        tearDown()
    }

    @Test
    fun testRefreshContactByID() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.contact_by_id)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "contacts/2") {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        contacts.refreshContact(2L) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = contacts.fetchContact(2L).test()
            testObserver.awaitValue()
            val model = testObserver.value()
            assertNotNull(model)
            assertEquals(2L, model?.contactId)
            assertEquals("Steven", model?.name)
            assertEquals("Steven", model?.nickName)
            assertEquals(PaymentMethod.PAY_ANYONE, model?.paymentMethod)
            assertEquals("2020-12-07T14:00:44.940+11:00", model?.createdDate)
            assertEquals("2020-12-07T14:00:44.940+11:00", model?.modifiedDate)
            assertEquals(false, model?.verified)
            val paymentDetails = model?.paymentDetails as PaymentDetails.PayAnyone
            assertEquals("Mr Steven Smith", paymentDetails.accountHolder)
            assertEquals("12345679", paymentDetails.accountNumber)
            assertEquals("100-123", paymentDetails.bsb)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals("contacts/2", request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshContactsIsCached() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.contacts_page_1)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == ContactsAPI.URL_CONTACTS) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        contacts.refreshContactsWithPagination { result ->
            assertTrue(result is PaginatedResult.Success)

            val testObserver = contacts.fetchContacts().test()
            testObserver.awaitValue()
            val models = testObserver.value()
            assertNotNull(models)
            assertEquals(10, models?.size)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(ContactsAPI.URL_CONTACTS, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshContactByIdFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        contacts.refreshContact(2L) { result ->
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
    fun testFetchPaginatedContacts() {
        initSetup()

        val requestPath1 = "${ContactsAPI.URL_CONTACTS}?size=10"
        val requestPath2 = "${ContactsAPI.URL_CONTACTS}?after=10&size=10"

        val signal = CountDownLatch(1)

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath1) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(readStringFromJson(app, R.raw.contacts_page_1))
                } else if (request?.trimmedPath == requestPath2) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(readStringFromJson(app, R.raw.contacts_page_2))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        // Insert some stale contacts
        val data1 = testContactResponseData(contactId = 14)
        val data2 = testContactResponseData(contactId = 15)
        val list = mutableListOf(data1, data2)
        database.contacts().insertAll(*list.map { it.toContact() }.toList().toTypedArray())

        contacts.refreshContactsWithPagination(size = 10) { result1 ->
            assertTrue(result1 is PaginatedResult.Success)
            assertNull((result1 as PaginatedResult.Success).paginationInfo?.before)
            assertEquals(10L, result1.paginationInfo?.after)

            contacts.refreshContactsWithPagination(size = 10, after = result1.paginationInfo?.after) { result2 ->
                assertTrue(result2 is PaginatedResult.Success)
                assertEquals(10L, (result2 as PaginatedResult.Success).paginationInfo?.before)
                assertNull(result2.paginationInfo?.after)

                val testObserver = contacts.fetchContacts().test()
                testObserver.awaitValue()
                val models = testObserver.value()
                assertNotNull(models)
                assertEquals(13, models?.size)

                // Verify that the stale contacts are deleted from the database
                assertEquals(0, models?.filter { it.contactId == 14L && it.contactId == 15L }?.size)

                signal.countDown()
            }
        }

        signal.await(3, TimeUnit.SECONDS)

        assertEquals(2, mockServer.requestCount)

        tearDown()
    }

    @Test
    fun testCreatePayAnyoneContact() {
        initSetup()

        val signal = CountDownLatch(1)
        val body = readStringFromJson(app, R.raw.contact_create_pay_anyone_contact)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == ContactsAPI.URL_CONTACTS) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        contacts.createPayAnyoneContact(
            name = "Johnathan",
            nickName = "Johnny Boy",
            accountName = "Mr Johnathan Smith",
            bsb = "100-123",
            accountNumber = "12345678"
        ) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)
            assertEquals(10L, resource.data)

            val testObserver = contacts.fetchContact(10).test()

            testObserver.awaitValue()
            val model = testObserver.value()
            assertNotNull(model)
            assertEquals(10L, model?.contactId)
            val paymentDetails = model?.paymentDetails as PaymentDetails.PayAnyone
            assertEquals("Mr Johnathan Smith", paymentDetails.accountHolder)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(ContactsAPI.URL_CONTACTS, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testCreateBPayContact() {
        initSetup()

        val signal = CountDownLatch(1)
        val body = readStringFromJson(app, R.raw.contact_create_bpay_contact)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == ContactsAPI.URL_CONTACTS) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        contacts.createBPayContact(
            nickName = "ACME Inc.",
            billerCode = "209999",
            crn = "84100064513925",
            billerName = "ACME Inc."
        ) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)
            assertEquals(5L, resource.data)

            val testObserver = contacts.fetchContact(5).test()

            testObserver.awaitValue()
            val model = testObserver.value()
            assertNotNull(model)
            assertEquals(5L, model?.contactId)
            val paymentDetails = model?.paymentDetails as PaymentDetails.Biller
            assertEquals("209999", paymentDetails.billerCode)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(ContactsAPI.URL_CONTACTS, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testCreatePayIDContact() {
        initSetup()

        val signal = CountDownLatch(1)
        val body = readStringFromJson(app, R.raw.contact_create_pay_id_contact)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == ContactsAPI.URL_CONTACTS) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        contacts.createPayIDContact(
            name = "Johnathan",
            nickName = "Johnny Boy",
            description = "That guy I buy tyres from",
            payId = "0412345678",
            payIdName = "J SMITH",
            payIdType = PayIDType.MOBILE
        ) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)
            assertEquals(4L, resource.data)

            val testObserver = contacts.fetchContact(4).test()

            testObserver.awaitValue()
            val model = testObserver.value()
            assertNotNull(model)
            assertEquals(4L, model?.contactId)
            val paymentDetails = model?.paymentDetails as PaymentDetails.PayID
            assertEquals("0412345678", paymentDetails.payId)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(ContactsAPI.URL_CONTACTS, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testCreateInternationalContact() {
        initSetup()

        val signal = CountDownLatch(1)
        val body = readStringFromJson(app, R.raw.contact_create_international_contact)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == ContactsAPI.URL_CONTACTS) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        contacts.createInternationalContact(
            name = "Anne Frank",
            nickName = "Annie",
            country = "New Zeland",
            bankCountry = "New Zeland",
            accountNumber = "12345678"
        ) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)
            assertEquals(9L, resource.data)

            val testObserver = contacts.fetchContact(9).test()

            testObserver.awaitValue()
            val model = testObserver.value()
            assertNotNull(model)
            assertEquals(9L, model?.contactId)
            val paymentDetails = model?.paymentDetails as PaymentDetails.International
            assertEquals("New Zeland", paymentDetails.beneficiary.country)
            assertEquals("New Zeland", paymentDetails.bankDetails.country)
            assertEquals("12345678", paymentDetails.bankDetails.accountNumber)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(ContactsAPI.URL_CONTACTS, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testCreateContactFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)
        clearLoggedInPreferences()

        contacts.createPayAnyoneContact(
            name = "Johnathan",
            nickName = "Johnny Boy",
            accountName = "Mr Johnathan Smith",
            bsb = "100-123",
            accountNumber = "12345678"
        ) { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testUpdatePayAnyoneContact() {
        initSetup()

        val signal = CountDownLatch(1)

        val contactId: Long = 1
        val requestPath = "contacts/$contactId"

        val body = readStringFromJson(app, R.raw.contact_update_pay_anyone_contact)
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

        val contact = testContactResponseData(contactId).toContact()

        database.contacts().insert(contact)

        contacts.updatePayAnyoneContact(
            contactId = contactId,
            name = "Johnathan",
            nickName = "Johnny Boy",
            accountName = "Mr Johnathan Smith",
            bsb = "100-123",
            accountNumber = "12345678"
        ) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = contacts.fetchContact(contactId).test()

            testObserver.awaitValue()
            val model = testObserver.value()
            assertNotNull(model)
            assertEquals(contactId, model?.contactId)
            val paymentDetails = model?.paymentDetails as PaymentDetails.PayAnyone
            assertEquals("Mr Johnathan Smith", paymentDetails.accountHolder)
            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testUpdateBPayContact() {
        initSetup()

        val signal = CountDownLatch(1)

        val contactId: Long = 9
        val requestPath = "contacts/$contactId"

        val body = readStringFromJson(app, R.raw.contact_update_bpay_contact)
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

        val contact = testContactResponseData(contactId).toContact()

        database.contacts().insert(contact)

        contacts.updateBPayContact(
            contactId = contactId,
            name = "Tenstra Inc",
            nickName = "Tenstra",
            description = "Test Desc update",
            billerCode = "2275362",
            crn = "723647803",
            billerName = "Tenstra Inc"
        ) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = contacts.fetchContact(contactId).test()

            testObserver.awaitValue()
            val model = testObserver.value()
            assertNotNull(model)
            assertEquals(contactId, model?.contactId)
            val paymentDetails = model?.paymentDetails as PaymentDetails.Biller
            assertEquals("2275362", paymentDetails.billerCode)
            assertEquals(CRNType.FIXED, paymentDetails.crnType)
            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testUpdatePayIDContact() {
        initSetup()

        val signal = CountDownLatch(1)

        val contactId: Long = 4
        val requestPath = "contacts/$contactId"

        val body = readStringFromJson(app, R.raw.contact_update_pay_id_contact)
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

        val contact = testContactResponseData(contactId).toContact()

        database.contacts().insert(contact)

        contacts.updatePayIDContact(
            contactId = contactId,
            name = "Johnathan Smith",
            nickName = "Johnny Boy",
            payId = "0412345678",
            payIdName = "J SMITH",
            payIdType = PayIDType.MOBILE
        ) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = contacts.fetchContact(contactId).test()

            testObserver.awaitValue()
            val model = testObserver.value()
            assertNotNull(model)
            assertEquals(contactId, model?.contactId)
            val paymentDetails = model?.paymentDetails as PaymentDetails.PayID
            assertEquals("j.gilbert@frollo.com", paymentDetails.payId)
            assertEquals(PayIDType.EMAIL, paymentDetails.type)
            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testUpdateInternationalContact() {
        initSetup()

        val signal = CountDownLatch(1)

        val contactId: Long = 9
        val requestPath = "contacts/$contactId"

        val body = readStringFromJson(app, R.raw.contact_update_international_contact)
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

        val contact = testContactResponseData(contactId).toContact()

        database.contacts().insert(contact)

        contacts.updateInternationalContact(
            contactId = contactId,
            name = "Anne Maria",
            nickName = "Mary",
            country = "New Zeland",
            message = "Test message new",
            bankCountry = "New Zeland",
            accountNumber = "12345666",
            bankAddress = "666",
            bic = "777",
            fedwireNumber = "1234566",
            sortCode = "ABC 666",
            chipNumber = "555",
            routingNumber = "444",
            legalEntityIdentifier = "123666"
        ) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = contacts.fetchContact(contactId).test()

            testObserver.awaitValue()
            val model = testObserver.value()
            assertNotNull(model)
            assertEquals(contactId, model?.contactId)
            val paymentDetails = model?.paymentDetails as PaymentDetails.International
            assertEquals("Anne Maria", paymentDetails.beneficiary.name)
            assertEquals("New Zeland", paymentDetails.beneficiary.country)
            assertEquals("New Zeland", paymentDetails.bankDetails.country)
            assertEquals("ABC 666", paymentDetails.bankDetails.bankAddress?.address)
            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testUpdateContactFailsIfLoggedOut() {
        initSetup()

        clearLoggedInPreferences()
        val signal = CountDownLatch(1)

        contacts.updatePayAnyoneContact(
            contactId = 1,
            name = "Johnathan",
            nickName = "Johnny Boy",
            accountName = "Mr Johnathan Smith",
            bsb = "100-123",
            accountNumber = "12345678"
        ) { result ->
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
    fun testDeleteContact() {
        initSetup()

        val signal = CountDownLatch(1)
        val contactId: Long = 6

        val requestPath = "contacts/$contactId"

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                        .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        database.contacts().insert(testContactResponseData(contactId).toContact())

        var testObserver = contacts.fetchContact(contactId).test()

        testObserver.awaitValue()
        val model = testObserver.value()
        assertNotNull(model)
        assertEquals(contactId, model?.contactId)

        contacts.deleteContact(contactId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            testObserver = contacts.fetchContact(contactId).test()

            testObserver.awaitValue()
            assertNull(testObserver.value())
            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testDeleteContactFailsIfLoggedOut() {
        initSetup()

        clearLoggedInPreferences()
        val signal = CountDownLatch(1)

        contacts.deleteContact(6) { result ->
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
