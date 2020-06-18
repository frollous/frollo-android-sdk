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

package us.frollo.frollosdk.base

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.error.FrolloSDKError

class ResultTest {

    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Before
    fun setup() {
        FrolloSDK.app = app
    }

    @Test
    fun testResultSuccess() {
        val result = Result.success()
        assertNotNull(result)
        assertEquals(Result.Status.SUCCESS, result.status)
        assertNull(result.error)
    }

    @Test
    fun testResultError() {
        val result = Result.error(FrolloSDKError(errorMessage = "Unauthorized"))
        assertNotNull(result)
        assertEquals(Result.Status.ERROR, result.status)
        assertNotNull(result.error)
        assertEquals("Unauthorized", result.error?.debugDescription)
        assertEquals("Unauthorized", result.error?.localizedDescription)
    }
}
