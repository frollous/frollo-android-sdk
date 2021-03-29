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

package us.frollo.frollosdk.managedproducts

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
import us.frollo.frollosdk.base.PaginatedResultWithData
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.network.api.ManagedProductsAPI
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ManagedProductsTest : BaseAndroidTest() {

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
    }

    @Test
    fun testFetchManagedProduct() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.managed_product_response)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "manage/products/1") {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        managedProducts.fetchManagedProduct(1L) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val model = resource.data
            assertNotNull(model)
            assertEquals(1L, model?.productId)
            assertEquals("Volt Bank Savings", model?.name)
            assertEquals(22772L, model?.providerId)
            assertEquals("bank", model?.container)
            assertEquals("savings", model?.accountType)
            assertEquals(1L, model?.termsConditions?.first()?.termsId)
            assertEquals("Volt Bank Savings Terms & Conditions", model?.termsConditions?.first()?.name)
            assertEquals("https://www.voltbank.com.au/voltsaveterms", model?.termsConditions?.first()?.url)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals("manage/products/1", request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testFetchManagedProductFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        managedProducts.fetchManagedProduct(1L) { resource ->
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
    fun testFetchAvailableProducts() {
        initSetup()

        val requestPath = "${ManagedProductsAPI.URL_AVAILABLE_PRODUCTS}?after=10&size=3"

        val signal = CountDownLatch(1)

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(readStringFromJson(app, R.raw.managed_products_response))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        managedProducts.fetchAvailableProducts(after = 10, size = 3) { result ->
            assertTrue(result is PaginatedResultWithData.Success)
            assertEquals(10L, (result as PaginatedResultWithData.Success).paginationInfo?.before)
            assertEquals(13L, result.paginationInfo?.after)

            val models = result.data
            assertNotNull(models)
            assertEquals(3, models?.size)

            val model = models?.first()
            assertEquals(11L, model?.productId)
            assertEquals("Volt Bank Savings", model?.name)
            assertEquals(22772L, model?.providerId)
            assertEquals("bank", model?.container)
            assertEquals("savings", model?.accountType)
            assertEquals(1L, model?.termsConditions?.first()?.termsId)
            assertEquals("Volt Bank Savings Terms & Conditions", model?.termsConditions?.first()?.name)
            assertEquals("https://www.voltbank.com.au/voltsaveterms", model?.termsConditions?.first()?.url)

            signal.countDown()
        }

        signal.await(120, TimeUnit.SECONDS)

        assertEquals(1, mockServer.requestCount)

        tearDown()
    }

    @Test
    fun testFetchAvailableProductsFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        managedProducts.fetchAvailableProducts { result ->
            assertTrue(result is PaginatedResultWithData.Error)
            assertNotNull((result as PaginatedResultWithData.Error).error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testFetchManagedProducts() {
        initSetup()

        val requestPath = "${ManagedProductsAPI.URL_MANAGED_PRODUCTS}?after=10&size=3"

        val signal = CountDownLatch(1)

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(readStringFromJson(app, R.raw.managed_products_response))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        managedProducts.fetchManagedProducts(after = 10, size = 3) { result ->
            assertTrue(result is PaginatedResultWithData.Success)
            assertEquals(10L, (result as PaginatedResultWithData.Success).paginationInfo?.before)
            assertEquals(13L, result.paginationInfo?.after)

            val models = result.data
            assertNotNull(models)
            assertEquals(3, models?.size)

            val model = models?.first()
            assertEquals(11L, model?.productId)
            assertEquals("Volt Bank Savings", model?.name)
            assertEquals(22772L, model?.providerId)
            assertEquals("bank", model?.container)
            assertEquals("savings", model?.accountType)
            assertEquals(1L, model?.termsConditions?.first()?.termsId)
            assertEquals("Volt Bank Savings Terms & Conditions", model?.termsConditions?.first()?.name)
            assertEquals("https://www.voltbank.com.au/voltsaveterms", model?.termsConditions?.first()?.url)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        assertEquals(1, mockServer.requestCount)

        tearDown()
    }

    @Test
    fun testFetchManagedProductsFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        managedProducts.fetchManagedProducts { result ->
            assertTrue(result is PaginatedResultWithData.Error)
            assertNotNull((result as PaginatedResultWithData.Error).error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testCreateManagedProduct() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.managed_product_response)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == ManagedProductsAPI.URL_MANAGED_PRODUCTS) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        managedProducts.createManagedProduct(1L, listOf(1, 2)) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val model = resource.data
            assertNotNull(model)
            assertEquals(1L, model?.productId)
            assertEquals("Volt Bank Savings", model?.name)
            assertEquals(22772L, model?.providerId)
            assertEquals("bank", model?.container)
            assertEquals("savings", model?.accountType)
            assertEquals(1L, model?.termsConditions?.first()?.termsId)
            assertEquals("Volt Bank Savings Terms & Conditions", model?.termsConditions?.first()?.name)
            assertEquals("https://www.voltbank.com.au/voltsaveterms", model?.termsConditions?.first()?.url)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(ManagedProductsAPI.URL_MANAGED_PRODUCTS, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testCreateManagedProductFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)
        clearLoggedInPreferences()

        managedProducts.createManagedProduct(1L, listOf(1, 2)) { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testDeleteManagedProduct() {
        initSetup()

        val signal = CountDownLatch(1)
        val productId: Long = 1

        val requestPath = "manage/products/$productId"

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                        .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        managedProducts.deleteManagedProduct(productId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testDeleteManagedProductFailsIfLoggedOut() {
        initSetup()

        clearLoggedInPreferences()
        val signal = CountDownLatch(1)

        managedProducts.deleteManagedProduct(1) { result ->
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
