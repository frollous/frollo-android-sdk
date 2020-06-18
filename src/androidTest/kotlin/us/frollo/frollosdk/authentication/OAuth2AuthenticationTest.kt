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

package us.frollo.frollosdk.authentication

import android.content.Intent
import com.jraska.livedata.test
import net.openid.appauth.AuthorizationException
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.BaseAndroidTest
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.error.APIError
import us.frollo.frollosdk.error.APIErrorType
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.error.OAuth2Error
import us.frollo.frollosdk.error.OAuth2ErrorType
import us.frollo.frollosdk.network.api.UserAPI
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.randomString
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class OAuth2AuthenticationTest : BaseAndroidTest() {

    @Test
    fun testGetLoggedIn() {
        initSetup()

        assertFalse(oAuth2Authentication.loggedIn)

        preferences.loggedIn = true

        assertTrue(oAuth2Authentication.loggedIn)

        tearDown()
    }

    @Test
    fun testLoginUser() {
        initSetup()

        val signal = CountDownLatch(1)

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

        oAuth2Authentication.loginUser("user@frollo.us", "password", scopes) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            assertTrue(oAuth2Authentication.loggedIn)
            assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(2550794799, preferences.accessTokenExpiry)

            signal.countDown()
        }

        val request2 = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request2.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testInvalidLoginUser() {
        initSetup()

        val signal = CountDownLatch(1)

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

        oAuth2Authentication.loginUser("user@frollo.us", "wrong_password", scopes) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            val testObserver = userManagement.fetchUser().test()
            testObserver.awaitValue()
            assertNull(testObserver.value().data)

            assertEquals(OAuth2ErrorType.INVALID_CLIENT, (result.error as OAuth2Error).type)
            assertFalse(oAuth2Authentication.loggedIn)

            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1L, preferences.accessTokenExpiry)

            signal.countDown()
        }

        val request = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testLoginUserFailsIfLoggedIn() {
        initSetup()

        val signal = CountDownLatch(1)

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

        preferences.loggedIn = true

        oAuth2Authentication.loginUser("user@frollo.us", "password", scopes) { result ->
            assertTrue(oAuth2Authentication.loggedIn)

            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.ALREADY_LOGGED_IN, (result.error as DataError).subType)

            signal.countDown()
        }

        assertEquals(0, mockTokenServer.requestCount)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testLoginUserViaWebPendingIntent() {
        // TODO: to be implemented
    }

    @Test
    fun testLoginUserViaWeb() {
        // TODO: to be implemented
    }

    @Test
    fun testHandleWebLoginResponse() {
        initSetup()

        val signal = CountDownLatch(1)

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

        val json = readStringFromJson(app, R.raw.authorization_code_valid)
        val intent = Intent().putExtra("net.openid.appauth.AuthorizationResponse", json)

        oAuth2Authentication.handleWebLoginResponse(intent, scopes) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            assertTrue(oAuth2Authentication.loggedIn)
            assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(2550794799, preferences.accessTokenExpiry)

            signal.countDown()
        }

        val request2 = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request2.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testHandleWebLoginResponseFail() {
        initSetup()

        val intent = AuthorizationException.AuthorizationRequestErrors.ACCESS_DENIED.toIntent()

        oAuth2Authentication.handleWebLoginResponse(intent, scopes) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertEquals(OAuth2ErrorType.ACCESS_DENIED, (result.error as OAuth2Error).type)

            assertFalse(oAuth2Authentication.loggedIn)
            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1L, preferences.accessTokenExpiry)
        }

        tearDown()
    }

    @Test
    fun testLogoutUser() {
        initSetup()

        val signal = CountDownLatch(2)

        var tokenRevoked = false

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        mockRevokeTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == REVOKE_TOKEN_URL) {
                    tokenRevoked = true
                    signal.countDown()
                    return MockResponse()
                        .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        oAuth2Authentication.logout {
            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        assertFalse(oAuth2Authentication.loggedIn)
        assertNull(preferences.encryptedAccessToken)
        assertNull(preferences.encryptedRefreshToken)
        assertEquals(-1, preferences.accessTokenExpiry)
        assertTrue(tokenRevoked)

        tearDown()
    }

    @Test
    fun testUserLoggedOutOn401() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.error_suspended_device)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                        .setResponseCode(401)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        userManagement.refreshUser { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertTrue(result.error is APIError)
            assertEquals(APIErrorType.SUSPENDED_DEVICE, (result.error as APIError).type)

            assertFalse(oAuth2Authentication.loggedIn)
            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1, preferences.accessTokenExpiry)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshTokensFailsIfNoRefreshToken() {
        initSetup()

        val signal = CountDownLatch(1)

        preferences.loggedIn = true
        preferences.resetEncryptedRefreshToken()
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        mockRevokeTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(readStringFromJson(app, R.raw.token_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        oAuth2Authentication.refreshTokens { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertTrue(result.error is DataError)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_REFRESH_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testExchangeAuthorizationCode() {
        initSetup()

        val signal = CountDownLatch(1)

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

        oAuth2Authentication.exchangeAuthorizationCode(code = randomString(32), codeVerifier = randomString(32), scopes = scopes) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            assertTrue(oAuth2Authentication.loggedIn)
            assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(2550794799, preferences.accessTokenExpiry)

            signal.countDown()
        }

        val request2 = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request2.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testExchangeAuthorizationCodeInvalid() {
        initSetup()

        val signal = CountDownLatch(1)

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

        oAuth2Authentication.exchangeAuthorizationCode(code = randomString(32), codeVerifier = randomString(32), scopes = scopes) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertEquals(OAuth2ErrorType.INVALID_CLIENT, (result.error as OAuth2Error).type)

            assertFalse(oAuth2Authentication.loggedIn)
            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1L, preferences.accessTokenExpiry)

            signal.countDown()
        }

        val request = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testExchangeAuthorizationCodeFailsIfLoggedIn() {
        initSetup()

        val signal = CountDownLatch(1)

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

        preferences.loggedIn = true

        oAuth2Authentication.exchangeAuthorizationCode(code = randomString(32), codeVerifier = randomString(32), scopes = scopes) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.ALREADY_LOGGED_IN, (result.error as DataError).subType)

            signal.countDown()
        }

        assertEquals(0, mockTokenServer.requestCount)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testExchangeLegacyRefreshToken() {
        initSetup()

        val signal = CountDownLatch(1)

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

        val legacyToken = randomString(32)

        oAuth2Authentication.exchangeLegacyToken(legacyToken = legacyToken) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            assertTrue(oAuth2Authentication.loggedIn)
            assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(2550794799, preferences.accessTokenExpiry)

            signal.countDown()
        }

        val request2 = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request2.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testExchangeLegacyRefreshTokenFailure() {
        initSetup()

        val signal = CountDownLatch(1)

        mockTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == TOKEN_URL) {
                    return MockResponse()
                        .setResponseCode(401)
                        .setBody(readStringFromJson(app, R.raw.error_oauth2_invalid_grant))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val legacyToken = randomString(32)

        oAuth2Authentication.exchangeLegacyToken(legacyToken = legacyToken) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertEquals(OAuth2ErrorType.INVALID_GRANT, (result.error as OAuth2Error).type)

            assertFalse(oAuth2Authentication.loggedIn)
            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1L, preferences.accessTokenExpiry)

            signal.countDown()
        }

        val request2 = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request2.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testReset() {
        initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        assertTrue(oAuth2Authentication.loggedIn)

        oAuth2Authentication.reset()

        assertFalse(oAuth2Authentication.loggedIn)
        assertNull(preferences.encryptedAccessToken)
        assertNull(preferences.encryptedRefreshToken)
        assertEquals(-1, preferences.accessTokenExpiry)

        tearDown()
    }
}
