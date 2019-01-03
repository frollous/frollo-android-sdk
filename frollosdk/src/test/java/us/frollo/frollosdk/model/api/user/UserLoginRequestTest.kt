package us.frollo.frollosdk.model.api.user

import org.junit.Test

import org.junit.Assert.*
import us.frollo.frollosdk.auth.AuthType

class UserLoginRequestTest {

    @Test
    fun testValidEmail() {
        var request = UserLoginRequest(AuthType.EMAIL, "", "", "", null, null)
        assertFalse(request.valid())
        request = UserLoginRequest(AuthType.EMAIL, "", "", "", "test@frollo.us", "12345678")
        assertTrue(request.valid())
    }

    @Test
    fun testValidFacebook() {
        var request = UserLoginRequest(AuthType.FACEBOOK, "", "", "", null, null, null, null)
        assertFalse(request.valid())
        request = UserLoginRequest(AuthType.FACEBOOK, "", "", "", "test@frollo.us", null, "123456", "abcdefgh")
        assertTrue(request.valid())
    }

    @Test
    fun testValidVolt() {
        var request = UserLoginRequest(AuthType.VOLT, "", "", "", null, null, null, null)
        assertFalse(request.valid())
        request = UserLoginRequest(AuthType.VOLT, "", "", "", null, null, "123456", "abcdefgh")
        assertTrue(request.valid())
    }
}