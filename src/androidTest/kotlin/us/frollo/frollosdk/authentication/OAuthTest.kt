package us.frollo.frollosdk.authentication

import org.junit.Assert.*
import org.junit.Test
import us.frollo.frollosdk.core.testSDKConfig
import us.frollo.frollosdk.testutils.randomString

class OAuthTest {
    val oAuth = OAuth(testSDKConfig())

    @Test
    fun testGetRefreshTokensRequest() {
        val refreshToken = randomString(32)
        val request = oAuth.getRefreshTokensRequest(refreshToken = refreshToken)
        assertNotNull(request)
        assertTrue(request.valid)
        assertEquals(refreshToken, request.refreshToken)
    }

    @Test
    fun testGetLoginRequest() {
        val username = randomString(32)
        val password = randomString(8)
        val request = oAuth.getLoginRequest(username = username, password = password)
        assertNotNull(request)
        assertTrue(request.valid)
        assertEquals(username, request.username)
        assertEquals(password, request.password)
    }

    @Test
    fun testGetRegisterRequest() {
        val username = randomString(32)
        val password = randomString(8)
        val request = oAuth.getRegisterRequest(username = username, password = password)
        assertNotNull(request)
        assertTrue(request.valid)
        assertEquals(username, request.username)
        assertEquals(password, request.password)
    }

    @Test
    fun testGetExchangeAuthorizationCodeRequest() {
        val code = randomString(32)
        val codeVerifier = randomString(32)
        val request = oAuth.getExchangeAuthorizationCodeRequest(code = code, codeVerifier = codeVerifier)
        assertNotNull(request)
        assertTrue(request.valid)
        assertEquals(code, request.code)
        assertEquals(codeVerifier, request.codeVerifier)
    }

    @Test
    fun testGetExchangeTokenRequest() {
        val legacyToken = randomString(32)
        val request = oAuth.getExchangeTokenRequest(legacyToken = legacyToken)
        assertNotNull(request)
        assertTrue(request.valid)
        assertEquals(legacyToken, request.legacyToken)
    }

    @Test
    fun testGetAuthorizationRequest() {
        val request = oAuth.getAuthorizationRequest()
        assertNotNull(request)
        assertEquals(oAuth.config.clientId, request.clientId)
        assertEquals(oAuth.config.redirectUrl, request.redirectUri.toString())
        assertEquals("offline_access", request.scope)
        assertEquals(oAuth.config.authorizationUri, request.configuration.authorizationEndpoint)
        assertEquals(oAuth.config.tokenUri, request.configuration.tokenEndpoint)
    }
}