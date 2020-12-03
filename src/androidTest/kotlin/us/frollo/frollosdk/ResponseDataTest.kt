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

package us.frollo.frollosdk

import com.google.gson.Gson
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.extensions.fromJson
import us.frollo.frollosdk.model.coredata.user.User
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ResponseDataTest : BaseAndroidTest() {

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
    }

    @Test
    fun downloadResponseData() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.user_details_complete)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "api/v2/cdr/consents/1234/confirmation") {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val url = "http://${mockServer.hostName}:${mockServer.port}/api/v2/cdr/consents/1234/confirmation"
        FrolloSDK.downloadResponseData(url) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            assertNotNull(resource.data)
            val bodyString = resource.data?.string()?.replace("\n", "")
            bodyString?.let {
                val user: User? = Gson().fromJson(it)
                assertEquals("jacob@frollo.us", user?.email)
            }

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals("api/v2/cdr/consents/1234/confirmation", request?.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }
}
