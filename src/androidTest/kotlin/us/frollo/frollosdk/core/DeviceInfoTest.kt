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

package us.frollo.frollosdk.core

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DeviceInfoTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
    private lateinit var di: DeviceInfo

    @Before
    fun setUp() {
        di = DeviceInfo(context)
    }

    @Test
    fun testDeviceIdNotNullOrEmpty() {
        Assert.assertNotEquals("", di.deviceId)
        Assert.assertNotNull(di.deviceId)
    }

    @Test
    fun testDeviceNameNotNullOrEmpty() {
        Assert.assertNotEquals("", di.deviceName)
        Assert.assertNotNull(di.deviceName)
    }

    @Test
    fun testDeviceTypeNotNullOrEmpty() {
        Assert.assertNotEquals("", di.deviceType)
        Assert.assertNotNull(di.deviceType)
    }
}
