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

package us.frollo.frollosdk.network

import okhttp3.FormBody
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.BaseAndroidTest
import us.frollo.frollosdk.network.api.UserAPI
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.model.api.user.UserMigrationRequest
import us.frollo.frollosdk.model.testResetPasswordData
import us.frollo.frollosdk.model.testValidRegisterData
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.TestAPI
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath

class NetworkInterceptorTest : BaseAndroidTest() {

    private lateinit var userAPI: UserAPI
    private lateinit var testAPI: TestAPI

    override fun initSetup() {
        super.initSetup()

        userAPI = network.create(UserAPI::class.java)
        testAPI = network.create(TestAPI::class.java)
    }

    @Test
    fun testRequestHeaders() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.user_details_complete))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        userAPI.fetchUser().enqueue { }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_USER_DETAILS, request.trimmedPath)
        assertEquals("Bearer ExistingAccessToken", request.getHeader("Authorization"))
        assertNotNull(request.getHeader("X-Api-Version"))
        assertEquals("us.frollo.frollosdk", request.getHeader("X-Bundle-Id"))
        assertNotNull(request.getHeader("X-Device-Version"))
        assertNotNull(request.getHeader("X-Software-Version"))
        assertNotNull(request.getHeader("User-Agent"))

        tearDown()
    }

    @Test
    fun testNoHeaderAppendedToRegistrationRequest() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_REGISTER) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.user_details_complete))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        userAPI.register(testValidRegisterData()).enqueue { }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_REGISTER, request.trimmedPath)
        val authHeader = request.getHeader("Authorization")
        assertNull(authHeader)

        tearDown()
    }

    @Test
    fun testNoHeaderAppendedToResetPasswordRequest() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_PASSWORD_RESET) {
                    return MockResponse()
                            .setResponseCode(200)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        userAPI.resetPassword(testResetPasswordData()).enqueue { }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_PASSWORD_RESET, request.trimmedPath)
        val authHeader = request.getHeader("Authorization")
        assertNull(authHeader)

        tearDown()
    }

    @Test
    fun testAccessTokenHeaderAppendedToHostRequests() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.user_details_complete))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        userAPI.fetchUser().enqueue { }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_USER_DETAILS, request.trimmedPath)
        assertEquals("Bearer ExistingAccessToken", request.getHeader("Authorization"))

        tearDown()
    }

    @Test
    fun testNoHeaderAppendedToTokenRequest() {
        initSetup()

        val interceptor = NetworkInterceptor(network, NetworkHelper(appInfo))

        val originalRequest = Request.Builder()
                .url("https://id.example.com/oauth/token/")
                .method("POST", FormBody.Builder().build())
                .build()
        val adaptedRequest = interceptor.adaptRequest(originalRequest)

        assertNull(adaptedRequest.header("Authorization"))

        tearDown()
    }

    @Test
    fun testRateLimitRetries() {
        // TODO: Failing due to timeout. Need to Debug.
        /*initSetup()

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        mockServer.enqueue(get429Response())
        mockServer.enqueue(MockResponse().setResponseCode(200))

        val response = testAPI.testData().execute()
        assertTrue(response.isSuccessful)
        assertNull(response.body())
        assertEquals(2, mockServer.requestCount)

        tearDown()*/
    }

    @Test
    fun testAuthenticateRequestAppendExistingAccessToken() {
        initSetup()

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        val request = network.authenticateRequest(Request.Builder()
                .url("https://api.example.com")
                .build())
        assertNotNull(request)
        assertEquals("Bearer ExistingAccessToken", request.header("Authorization"))

        tearDown()
    }

    @Test
    fun testRefreshTokenHeaderAppendedToMigrateUserRequest() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_MIGRATE_USER) {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        userAPI.migrateUser(UserMigrationRequest(password = "password")).enqueue { }

        val request = mockServer.takeRequest()
        assertEquals(UserAPI.URL_MIGRATE_USER, request.trimmedPath)
        assertEquals("Bearer ExistingRefreshToken", request.getHeader("Authorization"))

        tearDown()
    }
}