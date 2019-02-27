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
    fun testGetExchangeTokenRequest() {
        val legacyToken = randomString(32)
        val request = oAuth.getExchangeTokenRequest(legacyToken = legacyToken)
        assertNotNull(request)
        assertTrue(request.valid)
        assertEquals(legacyToken, request.legacyToken)
    }
}