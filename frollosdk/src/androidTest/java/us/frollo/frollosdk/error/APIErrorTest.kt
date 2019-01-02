package us.frollo.frollosdk.error

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.model.api.shared.APIErrorCode

class APIErrorTest {

    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Before
    fun setUp() {
        FrolloSDK.app = app
    }

    @Test
    fun testAPIErrorType() {
        var apiError = APIError(401, "")
        assertEquals(APIErrorType.OTHER_AUTHORISATION, apiError.type)

        apiError = APIError(401, "{\"error\":{\"error_code\":\"F0101\",\"error_message\":\"Invalid access token\"}}")
        assertEquals(APIErrorType.INVALID_ACCESS_TOKEN, apiError.type)
    }

    @Test
    fun testErrorCode() {
        var apiError = APIError(401, "")
        assertNull(apiError.errorCode)

        apiError = APIError(401, "{\"error\":{\"error_code\":\"F0101\",\"error_message\":\"Invalid access token\"}}")
        assertEquals(APIErrorCode.INVALID_ACCESS_TOKEN, apiError.errorCode)
    }

    @Test
    fun testAPIErrorMessage() {
        var apiError = APIError(401, "")
        assertNull(apiError.message)

        apiError = APIError(401, "{\"error\":{\"error_code\":\"F0101\",\"error_message\":\"Invalid access token\"}}")
        assertEquals("Invalid access token", apiError.message)
    }

    @Test
    fun testLocalizedDescription() {
        var apiError = APIError(401, "{\"error\":{\"error_code\":\"F0101\",\"error_message\":\"Invalid access token\"}}")
        assertEquals(app.resources.getString(APIErrorType.INVALID_ACCESS_TOKEN.textResource), apiError.localizedDescription)

        apiError = APIError(401, "")
        assertEquals(app.resources.getString(APIErrorType.OTHER_AUTHORISATION.textResource), apiError.localizedDescription)
    }

    @Test
    fun testDebugDescription() {
        var apiError = APIError(401, "{\"error\":{\"error_code\":\"F0101\",\"error_message\":\"Invalid access token\"}}")
        var localizedDescription =  app.resources.getString(APIErrorType.INVALID_ACCESS_TOKEN.textResource)
        var str = "APIError: Type [INVALID_ACCESS_TOKEN] HTTP Status Code: 401 F0101: Invalid access token | $localizedDescription"
        assertEquals(str, apiError.debugDescription)

        apiError = APIError(401, "")
        localizedDescription =  app.resources.getString(APIErrorType.OTHER_AUTHORISATION.textResource)
        str = "APIError: Type [OTHER_AUTHORISATION] HTTP Status Code: 401 $localizedDescription"
        assertEquals(str, apiError.debugDescription)
    }

    @Test
    fun testStatusCode() {
        val apiError = APIError(401, "")
        assertEquals(401, apiError.statusCode)
    }
}