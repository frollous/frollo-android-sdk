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
import javax.net.ssl.SSLException

class NetworkErrorTest {

    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Before
    fun setUp() {
        FrolloSDK.app = app
    }

    @Test
    fun testLocalizedDescription() {
        val networkError = NetworkError(SSLException("SSL Error"))
        val str = "${app.resources.getString(NetworkErrorType.INVALID_SSL.textResource)} | SSL Error"
        assertEquals(str, networkError.localizedDescription)
    }

    @Test
    fun testDebugDescription() {
        val networkError = NetworkError(SSLException("SSL Error"))
        val localizedDescription = app.resources.getString(NetworkErrorType.INVALID_SSL.textResource)
        val str = "NetworkError: INVALID_SSL: $localizedDescription | SSL Error"
        assertEquals(str, networkError.debugDescription)
    }

    @Test
    fun testNetworkErrorType() {
        val networkError = NetworkError(SSLException("SSL Error"))
        assertEquals(NetworkErrorType.INVALID_SSL, networkError.type)
    }
}