package us.frollo.frollosdk.model.oauth

import org.junit.Assert.*
import org.junit.Test
import us.frollo.frollosdk.model.testOAuthTokenRequestData
import us.frollo.frollosdk.testutils.randomString

class OAuthTokenRequestTest {

    @Test
    fun testValid() {
        var request = testOAuthTokenRequestData(grantType = OAuthGrantType.PASSWORD, password = randomString(8))
        assertTrue(request.valid)

        request = testOAuthTokenRequestData(grantType = OAuthGrantType.PASSWORD, password = null, legacyToken = null)
        assertFalse(request.valid)

        request = testOAuthTokenRequestData(grantType = OAuthGrantType.REFRESH_TOKEN, refreshToken = null)
        assertFalse(request.valid)

        request = testOAuthTokenRequestData(grantType = OAuthGrantType.AUTHORIZATION_CODE, authorizationCode = null)
        assertFalse(request.valid)
    }
}