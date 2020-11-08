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

package us.frollo.frollosdk.database.dao

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.threetenabp.AndroidThreeTen
import com.jraska.livedata.test
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.mapping.toCDRConfiguration
import us.frollo.frollosdk.model.testCDRConfigurationData

class CDRConfigurationDaoTest {

    @get:Rule val testRule = InstantTaskExecutorRule()

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private val db = SDKDatabase.getInstance(app)

    @Before
    fun setUp() {
        AndroidThreeTen.init(app)
    }

    @After
    fun tearDown() {
        db.clearAllTables()
    }

    @Test
    fun testLoad() {
        db.cdrConfiguration().insert(testCDRConfigurationData(adrId = "102").toCDRConfiguration())

        val testObserver = db.cdrConfiguration().load().test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals("102", testObserver.value()?.adrId)
    }

    @Test
    fun testInsert() {
        db.cdrConfiguration().insert(testCDRConfigurationData(adrId = "102").toCDRConfiguration())

        val testObserver = db.cdrConfiguration().load().test()

        testObserver.awaitValue()
        assertEquals("102", testObserver.value()?.adrId)
    }

    @Test
    fun testDeleteStaleIds() {
        val data1 = testCDRConfigurationData(adrId = "100")
        val data2 = testCDRConfigurationData(adrId = "101")
        val data3 = testCDRConfigurationData(adrId = "102")
        val list = mutableListOf(data1, data2, data3)

        list.forEach {
            db.cdrConfiguration().insert(it.toCDRConfiguration())
        }

        db.cdrConfiguration().deleteStaleIds("101")

        val testObserver = db.cdrConfiguration().load().test()

        testObserver.awaitValue()
        assertEquals("101", testObserver.value()?.adrId)
    }

    @Test
    fun testClear() {
        val data1 = testCDRConfigurationData(adrId = "100")
        val data2 = testCDRConfigurationData(adrId = "101")
        val data3 = testCDRConfigurationData(adrId = "102")
        val list = mutableListOf(data1, data2, data3)

        list.forEach {
            db.cdrConfiguration().insert(it.toCDRConfiguration())
        }

        db.cdrConfiguration().clear()

        val testObserver = db.cdrConfiguration().load().test()

        testObserver.awaitValue()
        assertNull(testObserver.value())
    }
}
