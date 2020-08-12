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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import us.frollo.frollosdk.FrolloSDK

class DataErrorTest {

    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Before
    fun setUp() {
        FrolloSDK.context = app
    }

    @Test
    fun testLocalizedDescription() {
        var dataError = DataError(DataErrorType.DATABASE, DataErrorSubType.DISK_FULL)
        assertEquals(app.resources.getString(DataErrorSubType.DISK_FULL.textResource), dataError.localizedDescription)

        dataError = DataError(DataErrorType.DATABASE, DataErrorSubType.INVALID_DATA)
        assertEquals(app.resources.getString(DataErrorType.DATABASE.textResource), dataError.localizedDescription)
    }

    @Test
    fun testDebugDescription() {
        val dataError = DataError(DataErrorType.DATABASE, DataErrorSubType.DISK_FULL)
        val localizedDescription = app.resources.getString(DataErrorSubType.DISK_FULL.textResource)
        val str = "DataError: DATABASE.DISK_FULL: $localizedDescription"
        assertEquals(str, dataError.debugDescription)
    }

    @Test
    fun testDataErrorType() {
        val dataError = DataError(DataErrorType.DATABASE, DataErrorSubType.DISK_FULL)
        assertEquals(DataErrorType.DATABASE, dataError.type)
    }

    @Test
    fun testDataErrorSubType() {
        val dataError = DataError(DataErrorType.DATABASE, DataErrorSubType.DISK_FULL)
        assertEquals(DataErrorSubType.DISK_FULL, dataError.subType)
    }
}
