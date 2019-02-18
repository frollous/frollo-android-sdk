package us.frollo.frollosdk.error

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import us.frollo.frollosdk.FrolloSDK

class LoginFormErrorTest {

    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Before
    fun setUp() {
        FrolloSDK.app = app
    }

    @Test
    fun testAdditionalError() {
        val formError = LoginFormError(LoginFormErrorType.MISSING_REQUIRED_FIELD, "Username")
        assertNull(formError.additionalError)

        formError.additionalError = "Additional Error"
        assertEquals("Additional Error", formError.additionalError)
    }

    @Test
    fun testLocalizedDescription() {
        val formError = LoginFormError(LoginFormErrorType.MISSING_REQUIRED_FIELD, "Username")
        val str = app.resources.getString(LoginFormErrorType.MISSING_REQUIRED_FIELD.textResource, "Username")
        assertEquals(str, formError.localizedDescription)

        formError.additionalError = "Additional Error"
        assertEquals("$str Additional Error", formError.localizedDescription)
    }

    @Test
    fun testDebugDescription() {
        val formError = LoginFormError(LoginFormErrorType.MISSING_REQUIRED_FIELD, "Username")
        val localizedDescription = app.resources.getString(LoginFormErrorType.MISSING_REQUIRED_FIELD.textResource, "Username")
        val str = "LoginFormError: MISSING_REQUIRED_FIELD: $localizedDescription"
        assertEquals(str, formError.debugDescription)
    }

    @Test
    fun testLoginFormErrorType() {
        val formError = LoginFormError(LoginFormErrorType.MISSING_REQUIRED_FIELD, "Username")
        assertEquals(LoginFormErrorType.MISSING_REQUIRED_FIELD, formError.type)
    }

    @Test
    fun testFieldName() {
        val formError = LoginFormError(LoginFormErrorType.MISSING_REQUIRED_FIELD, "Username")
        assertEquals("Username", formError.fieldName)
    }
}