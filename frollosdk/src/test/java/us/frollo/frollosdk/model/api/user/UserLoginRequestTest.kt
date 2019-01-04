package us.frollo.frollosdk.model.api.user

import org.junit.Test

import org.junit.Assert.*
import us.frollo.frollosdk.model.testEmailLoginData
import us.frollo.frollosdk.model.testFacebookLoginData
import us.frollo.frollosdk.model.testInvalidLoginData
import us.frollo.frollosdk.model.testVoltLoginData

class UserLoginRequestTest {

    @Test
    fun testValidEmail() {
        val request = testEmailLoginData()
        assertTrue(request.valid())
    }

    @Test
    fun testValidFacebook() {
        val request = testFacebookLoginData()
        assertTrue(request.valid())
    }

    @Test
    fun testValidVolt() {
        val request = testVoltLoginData()
        assertTrue(request.valid())
    }

    @Test
    fun testInvalidRequest() {
        val request = testInvalidLoginData()
        assertFalse(request.valid())
    }
}