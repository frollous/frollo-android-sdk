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

class ResourceTest {

    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Before
    fun setup() {
        FrolloSDK.app = app
    }

    @Test
    fun testResource() {
        val resourceStr: Resource<String> = Resource.error(FrolloSDKError(errorMessage = "Unauthorized"), "12345")
        val resourceInt: Resource<Int> = resourceStr.map {
            assertNotNull(it)
            it?.toInt()
        }
        assertNotNull(resourceInt)
        assertNotNull(resourceInt.data)
        assertEquals(12345, resourceInt.data)
        assertNotNull(resourceInt.error)
        assertEquals("Unauthorized", resourceInt.error?.debugDescription)
        assertEquals("Unauthorized", resourceInt.error?.localizedDescription)
    }

    @Test
    fun testResourceSuccess() {
        val resource = Resource.success("12345")
        assertNotNull(resource)
        assertEquals(Resource.Status.SUCCESS, resource.status)
        assertNotNull(resource.data)
        assertEquals("12345", resource.data)
        assertNull(resource.error)
    }

    @Test
    fun testResourceError() {
        val resource = Resource.error(FrolloSDKError(errorMessage = "Unauthorized"), data = null)
        assertNotNull(resource)
        assertEquals(Resource.Status.ERROR, resource.status)
        assertNull(resource.data)
        assertNotNull(resource.error)
        assertEquals("Unauthorized", resource.error?.debugDescription)
        assertEquals("Unauthorized", resource.error?.localizedDescription)
    }
}
