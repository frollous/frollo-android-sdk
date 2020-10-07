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

package us.frollo.frollosdk.images

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
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.mapping.toImage
import us.frollo.frollosdk.model.testImageResponseData
import us.frollo.frollosdk.network.api.ImagesAPI
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ImagesTest : BaseAndroidTest() {

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
    }

    // Image Tests

    @Test
    fun testFetchImageById() {
        initSetup()

        val data1 = testImageResponseData(imageId = 100)
        val data2 = testImageResponseData(imageId = 101)
        val data3 = testImageResponseData(imageId = 102)

        val list = mutableListOf(data1, data2, data3)

        database.images().insertAll(*list.map { it.toImage() }.toList().toTypedArray())

        val testObserver = images.fetchImage(imageId = 101).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(101L, testObserver.value().data?.imageId)

        tearDown()
    }

    @Test
    fun testFetchImages() {
        initSetup()

        val data1 = testImageResponseData(imageId = 100, imageTypes = listOf("goal", "challenge"))
        val data2 = testImageResponseData(imageId = 101, imageTypes = listOf("goal"))
        val data3 = testImageResponseData(imageId = 102, imageTypes = listOf("goal", "challenge"))
        val data4 = testImageResponseData(imageId = 103, imageTypes = listOf("challenge"))

        val list = mutableListOf(data1, data2, data3, data4)

        database.images().insertAll(*list.map { it.toImage() }.toList().toTypedArray())

        val testObserver = images.fetchImages(imageType = "goal").test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().data?.isNotEmpty() == true)
        assertEquals(3, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testRefreshImages() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.images_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == ImagesAPI.URL_IMAGES) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val data1 = testImageResponseData()
        val data2 = testImageResponseData(imageTypes = listOf("derp", "test"))
        val data3 = testImageResponseData()
        val list = mutableListOf(data1, data2, data3)

        database.images().insertAll(*list.map { it.toImage() }.toList().toTypedArray())

        val testObserver1 = images.fetchImages().test()
        testObserver1.awaitValue()
        assertTrue(testObserver1.value().data?.isNotEmpty() == true)
        assertEquals(3, testObserver1.value().data?.size)

        images.refreshImages { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = images.fetchImages().test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value().data)
            assertEquals(3, testObserver.value().data?.size)

            assertEquals("Koala", testObserver.value().data?.get(0)?.name)
            assertEquals("Cockatoo", testObserver.value().data?.get(1)?.name)
            assertEquals("Kangaroo", testObserver.value().data?.get(2)?.name)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(ImagesAPI.URL_IMAGES, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshImagesFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        images.refreshImages { result ->
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
