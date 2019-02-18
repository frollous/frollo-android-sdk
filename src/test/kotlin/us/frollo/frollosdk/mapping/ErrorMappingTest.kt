package us.frollo.frollosdk.mapping

import org.junit.Assert
import org.junit.Test
import us.frollo.frollosdk.error.APIErrorType
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.mapping.toAPIErrorResponse
import us.frollo.frollosdk.mapping.toAPIErrorType
import us.frollo.frollosdk.model.api.shared.APIErrorCode

class ErrorMappingTest {

    @Test
    fun testStringToAPIErrorResponseSuccess() {
        val jsonStr = "{\"error\":{\"error_code\":\"F0111\",\"error_message\":\"Invalid username or password\"}}"
        val response = jsonStr.toAPIErrorResponse()
        Assert.assertNotNull(response)
        Assert.assertEquals("F0111", response?.errorCode.toString())
        Assert.assertEquals("Invalid username or password", response?.errorMessage)
    }

    @Test
    fun testStringToAPIErrorResponseFail() {
        val jsonStr = "Unknown Error"
        val response = jsonStr.toAPIErrorResponse()
        Assert.assertNull(response)
    }

    @Test
    fun testIntToAPIErrorTypeSuccess() {
        val type = 401.toAPIErrorType(APIErrorCode.INVALID_USERNAME_PASSWORD)
        Assert.assertEquals(APIErrorType.INVALID_USERNAME_PASSWORD, type)
    }

    @Test
    fun testStringToDataError() {
        val jsonStr = "{\"type\":\"AUTHENTICATION\",\"sub_type\":\"MISSING_REFRESH_TOKEN\"}"
        val value = jsonStr.toDataError()
        Assert.assertNotNull(value)
        Assert.assertEquals(DataErrorType.AUTHENTICATION, value?.type)
        Assert.assertEquals(DataErrorSubType.MISSING_REFRESH_TOKEN, value?.subType)
    }
}