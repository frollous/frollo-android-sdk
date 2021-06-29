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

package us.frollo.frollosdk.address

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
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.mapping.toAddress
import us.frollo.frollosdk.model.testAddressResponseData
import us.frollo.frollosdk.network.api.AddressAPI
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AddressManagementTest : BaseAndroidTest() {

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
    }

    @Test
    fun testFetchAddressById() {
        initSetup()

        val data1 = testAddressResponseData(addressId = 100)
        val data2 = testAddressResponseData(addressId = 101)
        val data3 = testAddressResponseData(addressId = 102)

        val list = mutableListOf(data1, data2, data3)

        database.addresses().insertAll(*list.map { it.toAddress() }.toTypedArray())

        val testObserver = addressManagement.fetchAddress(addressId = 101).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(101L, testObserver.value()?.addressId)

        tearDown()
    }

    @Test
    fun testFetchAddresses() {
        initSetup()

        val data1 = testAddressResponseData(addressId = 100)
        val data2 = testAddressResponseData(addressId = 101)
        val data3 = testAddressResponseData(addressId = 102)
        val data4 = testAddressResponseData(addressId = 103)

        val list = mutableListOf(data1, data2, data3, data4)

        database.addresses().insertAll(*list.map { it.toAddress() }.toTypedArray())

        val testObserver = addressManagement.fetchAddresses().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value()?.isNotEmpty() == true)
        assertEquals(4, testObserver.value()?.size)

        tearDown()
    }

    @Test
    fun testRefreshAddressById() {
        initSetup()

        val signal = CountDownLatch(1)

        val addressId: Long = 3

        val requestPath = "addresses/$addressId"

        val body = readStringFromJson(app, R.raw.address_by_id)
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

        addressManagement.refreshAddress(addressId = addressId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = addressManagement.fetchAddress(addressId = addressId).test()

            testObserver.awaitValue()
            val model = testObserver.value()
            assertNotNull(model)
            assertEquals(addressId, model?.addressId)
            assertEquals("105 Ashmole", model?.buildingName)
            assertEquals("", model?.unitNumber)
            assertEquals("105", model?.streetNumber)
            assertEquals("Ashmole", model?.streetName)
            assertEquals("Road", model?.streetType)
            assertEquals("Redcliffe", model?.suburb)
            assertEquals("", model?.town)
            assertEquals("", model?.region)
            assertEquals("QLD", model?.state)
            assertEquals("AU", model?.country)
            assertEquals("4020", model?.postcode)
            assertEquals("105 Ashmole Road, REDCLIFFE QLD 4020", model?.longForm)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshAddressByIdFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        addressManagement.refreshAddress(addressId = 3) { result ->
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
    fun testRefreshAddresses() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.addresses_get)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == AddressAPI.URL_ADDRESSES) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        addressManagement.refreshAddresses { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val testObserver = addressManagement.fetchAddresses().test()

            testObserver.awaitValue()
            val models = testObserver.value()
            assertNotNull(models)
            assertEquals(2, models?.size)
            assertEquals(3L, models?.first()?.addressId)
            assertEquals("105 Ashmole", models?.first()?.buildingName)
            assertEquals("", models?.first()?.unitNumber)
            assertEquals("105", models?.first()?.streetNumber)
            assertEquals("Ashmole", models?.first()?.streetName)
            assertEquals("Road", models?.first()?.streetType)
            assertEquals("Redcliffe", models?.first()?.suburb)
            assertEquals("", models?.first()?.town)
            assertEquals("", models?.first()?.region)
            assertEquals("QLD", models?.first()?.state)
            assertEquals("AU", models?.first()?.country)
            assertEquals("4020", models?.first()?.postcode)
            assertEquals("105 Ashmole Road, REDCLIFFE QLD 4020", models?.first()?.longForm)

            assertEquals(4L, models?.get(1)?.addressId)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(AddressAPI.URL_ADDRESSES, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshAddressesFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        addressManagement.refreshAddresses { resource ->
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
    fun testCreateAddress() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.address_by_id)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == AddressAPI.URL_ADDRESSES) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        addressManagement.createAddress(
            buildingName = "105 Ashmole",
            streetNumber = "105",
            streetName = "Ashmole",
            streetType = "Road",
            suburb = "Redcliffe",
            state = "QLD",
            country = "AU",
            postcode = "4020"
        ) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)
            assertEquals(3L, resource.data)

            val testObserver = addressManagement.fetchAddress(addressId = 3).test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value())
            assertEquals(3L, testObserver.value()?.addressId)
            assertEquals("Ashmole", testObserver.value()?.streetName)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(AddressAPI.URL_ADDRESSES, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testCreateAddressFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        addressManagement.createAddress(
            buildingName = "105 Ashmole",
            streetNumber = "105",
            streetName = "Ashmole",
            streetType = "Road",
            suburb = "Redcliffe",
            state = "QLD",
            country = "AU",
            postcode = "4020"
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
    fun testUpdateAddress() {
        initSetup()

        val signal = CountDownLatch(1)

        val addressId: Long = 3

        val requestPath = "addresses/$addressId"

        val body = readStringFromJson(app, R.raw.address_by_id)
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

        val address = testAddressResponseData(addressId = addressId).toAddress()

        database.addresses().insert(address)

        addressManagement.updateAddress(
            addressId = address.addressId,
            buildingName = "105 Ashmole",
            streetNumber = "105",
            streetName = "Ashmole",
            streetType = "Road",
            suburb = "Redcliffe",
            state = "QLD",
            country = "AU",
            postcode = "4020"
        ) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = addressManagement.fetchAddress(addressId = addressId).test()

            testObserver.awaitValue()
            val model = testObserver.value()
            assertNotNull(model)
            assertEquals(3L, testObserver.value()?.addressId)
            assertEquals("Ashmole", testObserver.value()?.streetName)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testUpdateAddressFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        addressManagement.updateAddress(
            addressId = 3,
            buildingName = "105 Ashmole",
            streetNumber = "105",
            streetName = "Ashmole",
            streetType = "Road",
            suburb = "Redcliffe",
            state = "QLD",
            country = "AU",
            postcode = "4020"
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
    fun testDeleteAddress() {
        initSetup()

        val signal = CountDownLatch(1)

        val addressId: Long = 3

        val requestPath = "addresses/$addressId"

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                        .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        database.addresses().insert(testAddressResponseData(addressId = 3).toAddress())

        var testObserver = addressManagement.fetchAddress(addressId).test()

        testObserver.awaitValue()
        val model = testObserver.value()
        assertNotNull(model)
        assertEquals(addressId, model?.addressId)

        addressManagement.deleteAddress(addressId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            testObserver = addressManagement.fetchAddress(addressId).test()

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
    fun testDeleteAddressFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        addressManagement.deleteAddress(3) { result ->
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
    fun testAddressAutocomplete() {
        initSetup()

        val signal = CountDownLatch(1)

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val requestPath = "${AddressAPI.URL_ADDRESS_AUTOCOMPLETE}?query=ashmole&max=20"

        val body = readStringFromJson(app, R.raw.address_autocomplete)
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

        addressManagement.fetchSuggestedAddresses(query = "ashmole", max = 20) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val models = resource.data
            assertEquals(20, models?.size)
            assertEquals("c3a85816-a9c5-11eb-81f0-68c07153d52e", models?.first()?.id)
            assertEquals("105 Ashmole Road, REDCLIFFE QLD 4020", models?.first()?.address)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testAddressAutocompleteFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        addressManagement.fetchSuggestedAddresses(query = "ashmole", max = 20) { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (resource.error as DataError).subType)

            signal.countDown()
        }

        assertEquals(0, mockServer.requestCount)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testFetchAddress() {
        initSetup()

        val signal = CountDownLatch(1)

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val addressId = "c3a85816-a9c5-11eb-81f0-68c07153d52e"
        val requestPath = "addresses/autocomplete/$addressId"

        val body = readStringFromJson(app, R.raw.address_autocomplete_by_id)
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

        addressManagement.fetchSuggestedAddress(addressId) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            assertEquals("105 Ashmole", resource.data?.buildingName)
            assertEquals("", resource.data?.unitNumber)
            assertEquals("105", resource.data?.streetNumber)
            assertEquals("Ashmole", resource.data?.streetName)
            assertEquals("Road", resource.data?.streetType)
            assertEquals("Redcliffe", resource.data?.suburb)
            assertEquals("", resource.data?.town)
            assertEquals("", resource.data?.region)
            assertEquals("QLD", resource.data?.state)
            assertEquals("AU", resource.data?.country)
            assertEquals("4020", resource.data?.postcode)
            assertEquals("105 Ashmole Road, REDCLIFFE QLD 4020", resource.data?.longForm)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testFetchAddressFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        addressManagement.fetchSuggestedAddress("c3a85816-a9c5-11eb-81f0-68c07153d52e") { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (resource.error as DataError).subType)

            signal.countDown()
        }

        assertEquals(0, mockServer.requestCount)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }
}
