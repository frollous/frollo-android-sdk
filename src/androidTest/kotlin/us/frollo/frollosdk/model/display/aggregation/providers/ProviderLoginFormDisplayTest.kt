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

package us.frollo.frollosdk.model.display.aggregation.providers

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.error.LoginFormError
import us.frollo.frollosdk.error.LoginFormErrorType
import us.frollo.frollosdk.model.loginFormFilledInvalidMultipleChoiceField
import us.frollo.frollosdk.model.loginFormMultipleChoiceFields

class ProviderLoginFormDisplayTest {

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Before
    fun setUp() {
        FrolloSDK.app = app
    }

    @Test
    fun testParsingProviderLoginFormDisplay() {
        val providerLoginForm = loginFormMultipleChoiceFields()

        val displayModel = providerLoginForm.toDisplay()

        assertEquals(2, displayModel.containers.size)
        assertEquals(3, displayModel.containers.last().rows.size)
        assertEquals("0002 Choice", displayModel.containers.last().fieldRowChoice)
        assertEquals(displayModel.containers.last().rows.first().rowId, displayModel.containers.last().selectedRowID)

        val dataModel = displayModel.toDataModel()
        assertEquals(providerLoginForm.rows.size, dataModel.rows.size)
    }

    @Test
    fun testProviderLoginFormMultipleChoiceValidation() {
        val loginForm = loginFormFilledInvalidMultipleChoiceField()

        val displayModel = loginForm.toDisplay()

        displayModel.validateMultipleChoice { valid, error ->
            assertFalse(valid)
            assertNotNull(error)

            assertEquals(LoginFormErrorType.FIELD_CHOICE_NOT_SELECTED, (error as LoginFormError).type)
            assertEquals("An Option", error.fieldName)
        }
    }
}