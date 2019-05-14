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

import org.junit.Assert.assertEquals
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.R

class FrolloSDKErrorTest {

    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Before
    fun setUp() {
        FrolloSDK.app = app
    }

    @Test
    fun testLocalizedDescription() {
        var sdkError = FrolloSDKError()
        assertEquals(app.resources.getString(R.string.FrolloSDK_Error_Generic_UnknownError), sdkError.localizedDescription)

        sdkError = FrolloSDKError("Frollo SDK Error")
        assertEquals("Frollo SDK Error", sdkError.localizedDescription)
    }

    @Test
    fun testDebugDescription() {
        var sdkError = FrolloSDKError()
        assertEquals(app.resources.getString(R.string.FrolloSDK_Error_Generic_UnknownError), sdkError.debugDescription)

        sdkError = FrolloSDKError("Frollo SDK Error")
        assertEquals("Frollo SDK Error", sdkError.debugDescription)
    }
}