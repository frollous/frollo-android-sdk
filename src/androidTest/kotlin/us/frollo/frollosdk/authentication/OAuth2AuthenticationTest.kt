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
import org.junit.Test

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
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
import us.frollo.frollosdk.testutils.wait

class OAuth2AuthenticationTest : BaseAndroidTest() {

    private lateinit var localAuthentication: OAuth2Authentication

    override fun initSetup() {
        super.initSetup()

        localAuthentication = authentication as OAuth2Authentication
    }

    @Test
    fun testGetLoggedIn() {
        initSetup()

        assertFalse(authentication.loggedIn)

        preferences.loggedIn = true

        assertTrue(authentication.loggedIn)

        tearDown()
    }

    @Test
    fun testLoginUser() {
        initSetup()

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

        localAuthentication.loginUser("user@frollo.us", "password", scopes) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            assertTrue(authentication.loggedIn)
            assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(2550794799, preferences.accessTokenExpiry)
        }

        val request2 = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request2.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testInvalidLoginUser() {
        initSetup()

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

        localAuthentication.loginUser("user@frollo.us", "wrong_password", scopes) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            val testObserver = userManagement.fetchUser().test()
            testObserver.awaitValue()
            assertNull(testObserver.value().data)

            assertEquals(OAuth2ErrorType.INVALID_CLIENT, (result.error as OAuth2Error).type)
            assertFalse(authentication.loggedIn)

            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1L, preferences.accessTokenExpiry)
        }

        val request = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testLoginUserFailsIfLoggedIn() {
        initSetup()

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

        localAuthentication.loginUser("user@frollo.us", "password", scopes) { result ->
            assertTrue(authentication.loggedIn)

            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.ALREADY_LOGGED_IN, (result.error as DataError).subType)
        }

        assertEquals(0, mockTokenServer.requestCount)

        wait(3)

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

        localAuthentication.handleWebLoginResponse(intent, scopes) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            assertTrue(authentication.loggedIn)
            assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(2550794799, preferences.accessTokenExpiry)
        }

        val request2 = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request2.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testHandleWebLoginResponseFail() {
        initSetup()

        val intent = AuthorizationException.AuthorizationRequestErrors.ACCESS_DENIED.toIntent()

        localAuthentication.handleWebLoginResponse(intent, scopes) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertEquals(OAuth2ErrorType.ACCESS_DENIED, (result.error as OAuth2Error).type)

            assertFalse(authentication.loggedIn)
            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1L, preferences.accessTokenExpiry)
        }

        tearDown()
    }

    @Test
    fun testLogoutUser() {
        initSetup()

        var tokenRevoked = false

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        mockRevokeTokenServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == REVOKE_TOKEN_URL) {
                    tokenRevoked = true
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        authentication.logout()

        wait(2)

        assertFalse(authentication.loggedIn)
        assertNull(preferences.encryptedAccessToken)
        assertNull(preferences.encryptedRefreshToken)
        assertEquals(-1, preferences.accessTokenExpiry)
        assertTrue(tokenRevoked)

        tearDown()
    }

    @Test
    fun testUserLoggedOutOn401() {
        initSetup()

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

            assertFalse(authentication.loggedIn)
            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1, preferences.accessTokenExpiry)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshTokensFailsIfNoRefreshToken() {
        initSetup()

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

        authentication.refreshTokens { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertTrue(result.error is DataError)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_REFRESH_TOKEN, (result.error as DataError).subType)
        }

        wait(2)

        tearDown()
    }

    @Test
    fun testExchangeAuthorizationCode() {
        initSetup()

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

        localAuthentication.exchangeAuthorizationCode(code = randomString(32), codeVerifier = randomString(32), scopes = scopes) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            assertTrue(authentication.loggedIn)
            assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(2550794799, preferences.accessTokenExpiry)
        }

        val request2 = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request2.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testExchangeAuthorizationCodeInvalid() {
        initSetup()

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

        localAuthentication.exchangeAuthorizationCode(code = randomString(32), codeVerifier = randomString(32), scopes = scopes) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertEquals(OAuth2ErrorType.INVALID_CLIENT, (result.error as OAuth2Error).type)

            assertFalse(authentication.loggedIn)
            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1L, preferences.accessTokenExpiry)
        }

        val request = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testExchangeAuthorizationCodeFailsIfLoggedIn() {
        initSetup()

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

        localAuthentication.exchangeAuthorizationCode(code = randomString(32), codeVerifier = randomString(32), scopes = scopes) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.ALREADY_LOGGED_IN, (result.error as DataError).subType)
        }

        assertEquals(0, mockTokenServer.requestCount)

        wait(3)

        tearDown()
    }

    @Test
    fun testExchangeLegacyRefreshToken() {
        initSetup()

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

        localAuthentication.exchangeLegacyToken(legacyToken = legacyToken) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            assertTrue(authentication.loggedIn)
            assertEquals("MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3", keystore.decrypt(preferences.encryptedAccessToken))
            assertEquals("IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk", keystore.decrypt(preferences.encryptedRefreshToken))
            assertEquals(2550794799, preferences.accessTokenExpiry)
        }

        val request2 = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request2.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testExchangeLegacyRefreshTokenFailure() {
        initSetup()

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

        localAuthentication.exchangeLegacyToken(legacyToken = legacyToken) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)

            assertEquals(OAuth2ErrorType.INVALID_GRANT, (result.error as OAuth2Error).type)

            assertFalse(authentication.loggedIn)
            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1L, preferences.accessTokenExpiry)
        }

        val request2 = mockTokenServer.takeRequest()
        assertEquals(TOKEN_URL, request2.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testReset() {
        initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        assertTrue(authentication.loggedIn)

        authentication.reset()

        assertFalse(authentication.loggedIn)
        assertNull(preferences.encryptedAccessToken)
        assertNull(preferences.encryptedRefreshToken)
        assertEquals(-1, preferences.accessTokenExpiry)

        tearDown()
    }
}