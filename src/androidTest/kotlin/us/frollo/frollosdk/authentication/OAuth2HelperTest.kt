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

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import net.openid.appauth.AuthorizationRequest.Scope.EMAIL
import net.openid.appauth.AuthorizationRequest.Scope.OFFLINE_ACCESS
import net.openid.appauth.AuthorizationRequest.Scope.OPENID
import net.openid.appauth.AuthorizationService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import us.frollo.frollosdk.core.testSDKConfig
import us.frollo.frollosdk.model.oauth.OAuthGrantType
import us.frollo.frollosdk.testutils.randomString

class OAuth2HelperTest {
    val oAuth = OAuth2Helper(testSDKConfig())
    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Test
    fun testGetRefreshTokensRequest() {
        val refreshToken = randomString(32)
        val request = oAuth.getRefreshTokensRequest(refreshToken = refreshToken)
        assertNotNull(request)
        assertTrue(request.valid)
        assertEquals(refreshToken, request.refreshToken)
        assertNull(request.scope)
    }

    @Test
    fun testGetLoginRequest() {
        val username = randomString(32)
        val password = randomString(8)
        val request = oAuth.getLoginRequest(username = username, password = password, scopes = listOf("offline_access", "openid", "email"), grantType = OAuthGrantType.PASSWORD)
        assertNotNull(request)
        assertTrue(request.valid)
        assertEquals(username, request.username)
        assertEquals(password, request.password)
        assertEquals("offline_access openid email", request.scope)
    }

    @Test
    fun testGetRegisterRequest() {
        val username = randomString(32)
        val password = randomString(8)
        val request = oAuth.getRegisterRequest(username = username, password = password, scopes = listOf("offline_access", "openid", "email"), grantType = OAuthGrantType.PASSWORD)
        assertNotNull(request)
        assertTrue(request.valid)
        assertEquals(username, request.username)
        assertEquals(password, request.password)
        assertEquals("offline_access openid email", request.scope)
    }

    @Test
    fun testGetExchangeAuthorizationCodeRequest() {
        val code = randomString(32)
        val codeVerifier = randomString(32)
        val request = oAuth.getExchangeAuthorizationCodeRequest(code = code, codeVerifier = codeVerifier, scopes = listOf("offline_access", "openid", "email"))
        assertNotNull(request)
        assertTrue(request.valid)
        assertEquals(code, request.code)
        assertEquals(codeVerifier, request.codeVerifier)
        assertEquals("offline_access openid email", request.scope)
    }

    @Test
    fun testGetExchangeTokenRequest() {
        val legacyToken = randomString(32)
        val request = oAuth.getExchangeTokenRequest(legacyToken = legacyToken, scopes = listOf("offline_access", "openid", "email"))
        assertNotNull(request)
        assertTrue(request.valid)
        assertEquals(legacyToken, request.legacyToken)
        assertEquals("offline_access openid email", request.scope)
    }

    @Test
    fun testGetAuthorizationRequest() {
        val request = oAuth.getAuthorizationRequest(scopes = listOf("offline_access", "openid", "email"))
        assertNotNull(request)
        assertEquals(oAuth.config.clientId, request.clientId)
        assertEquals(oAuth.oAuth2.redirectUrl, request.redirectUri.toString())
        assertTrue(request.scopeSet?.containsAll(setOf(OFFLINE_ACCESS, EMAIL, OPENID)) == true)
        assertEquals(oAuth.oAuth2.authorizationUri, request.configuration.authorizationEndpoint)
        assertEquals(oAuth.oAuth2.tokenUri, request.configuration.tokenEndpoint)
    }

    @Test
    fun testGetCustomTabsIntent() {
        val service = AuthorizationService(app)
        val request = oAuth.getAuthorizationRequest(scopes = listOf("offline_access", "openid", "email"))
        val intent = oAuth.getCustomTabsIntent(service, request)

        assertNotNull(intent)
    }
}
