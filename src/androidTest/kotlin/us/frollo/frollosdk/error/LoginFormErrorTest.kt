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

package us.frollo.frollosdk.error

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test

import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
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