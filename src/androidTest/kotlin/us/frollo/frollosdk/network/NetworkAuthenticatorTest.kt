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
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.error.APIError
import us.frollo.frollosdk.error.APIErrorType
import us.frollo.frollosdk.network.api.UserAPI
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import us.frollo.frollosdk.testutils.wait

class NetworkAuthenticatorTest : BaseAndroidTest() {

    private lateinit var userAPI: UserAPI

    override fun initSetup() {
        super.initSetup()

        userAPI = network.create(UserAPI::class.java)
    }

    @Test
    fun testPreemptiveAccessTokenRefresh() {
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

        mockTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 30 // 30 seconds in the future falls within the 5 minute access token expiry

        userAPI.fetchUser().enqueue { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            assertEquals(1, mockServer.requestCount)
            assertEquals(1, mockTokenServer.requestCount)
            assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(2550794799, preferences.accessTokenExpiry)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testInvalidAccessTokenRefresh() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            var failedOnce = false

            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    if (failedOnce) {
                        return MockResponse()
                                .setResponseCode(200)
                                .setBody(readStringFromJson(app, R.raw.user_details_complete))
                    } else {
                        failedOnce = true
                        return MockResponse()
                                .setResponseCode(401)
                                .setBody(readStringFromJson(app, R.raw.error_invalid_access_token))
                    }
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        userAPI.fetchUser().enqueue { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            assertEquals(2, mockServer.requestCount)
            assertEquals(1, mockTokenServer.requestCount)
            assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(2550794799, preferences.accessTokenExpiry)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRequestsGetCancelledAfterMultipleInvalidAccessTokenFromServer() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(401)
                            .setBody(readStringFromJson(app, R.raw.error_invalid_access_token))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        userAPI.fetchUser().enqueue { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)

            assertEquals(APIErrorType.INVALID_ACCESS_TOKEN, (resource.error as APIError).type)

            assertEquals(6, mockServer.requestCount)
            assertEquals(5, mockTokenServer.requestCount)

            // Retries is not reset
            assertEquals(6, network.invalidTokenRetries)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testInvalidAccessTokenRetriesReset() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            var userRequestCount = 0

            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    if (userRequestCount < 3) {
                        userRequestCount++
                        return MockResponse()
                                .setResponseCode(401)
                                .setBody(readStringFromJson(app, R.raw.error_invalid_access_token))
                    } else {
                        return MockResponse()
                                .setResponseCode(200)
                                .setBody(readStringFromJson(app, R.raw.user_details_complete))
                    }
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        userAPI.fetchUser().enqueue { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            assertEquals(4, mockServer.requestCount)
            assertEquals(3, mockTokenServer.requestCount)

            // Retries is reset
            assertEquals(0, network.invalidTokenRetries)
        }

        wait(8)

        tearDown()
    }

    @Test
    fun testInvalidRefreshTokenFails() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                return MockResponse()
                        .setResponseCode(401)
                        .setBody(readStringFromJson(app, R.raw.error_invalid_refresh_token))
            }
        })

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        userAPI.fetchUser().enqueue { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)

            assertEquals(1, mockServer.requestCount)

            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1, preferences.accessTokenExpiry)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRequestsGetRetriedAfterRefreshingAccessToken() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            var userRequestCount = 0

            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    if (userRequestCount < 3) {
                        userRequestCount++
                        return MockResponse()
                                .setResponseCode(401)
                                .setBody(readStringFromJson(app, R.raw.error_invalid_access_token))
                    } else {
                        return MockResponse()
                                .setResponseCode(200)
                                .setBody(readStringFromJson(app, R.raw.user_details_complete))
                    }
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        userAPI.fetchUser().enqueue { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            assertNotNull(resource.data)
        }

        userAPI.fetchUser().enqueue { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            assertNotNull(resource.data)
        }

        userAPI.fetchUser().enqueue { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            assertNotNull(resource.data)
        }

        wait(8)

        assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keystore.decrypt(preferences.encryptedAccessToken))
        assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keystore.decrypt(preferences.encryptedRefreshToken))
        assertEquals(2550794799, preferences.accessTokenExpiry)

        tearDown()
    }

    @Test
    fun testRequestsGetCancelledAfterRefreshingAccessTokenFails() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(401)
                            .setBody(readStringFromJson(app, R.raw.error_invalid_access_token))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        mockTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                            .setResponseCode(401)
                            .setBody(readStringFromJson(app, R.raw.error_oauth2_invalid_client))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        userAPI.fetchUser().enqueue { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)

            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1, preferences.accessTokenExpiry)
        }

        userAPI.fetchUser().enqueue { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
        }

        userAPI.fetchUser().enqueue { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
        }

        wait(8)

        tearDown()
    }
}