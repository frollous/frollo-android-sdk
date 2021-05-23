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
import us.frollo.frollosdk.mapping.toPayday
import us.frollo.frollosdk.model.coredata.payday.Payday
import us.frollo.frollosdk.model.coredata.payday.PaydayFrequency
import us.frollo.frollosdk.model.coredata.payday.PaydayStatus
import us.frollo.frollosdk.model.testPaydayResponseData

class PaydayDaoTest {

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
    fun testLoadLiveData() {
        val testObserver = db.payday().loadLiveData().test()
        testObserver.awaitValue()
        assertNull(testObserver.value())

        db.payday().insert(testPaydayResponseData().toPayday())

        val testObserver2 = db.payday().loadLiveData().test()
        testObserver2.awaitValue()
        assertNotNull(testObserver2.value())
    }

    @Test
    fun testLoad() {
        val model = db.payday().load()
        assertNull(model)

        db.payday().insert(testPaydayResponseData().toPayday())

        val model2 = db.payday().load()
        assertNotNull(model2)
    }

    @Test
    fun testInsert() {
        val data = testPaydayResponseData()
        db.payday().insert(data.toPayday())

        val testObserver = db.payday().loadLiveData().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.frequency, testObserver.value()?.frequency)
    }

    @Test
    fun testUpdate() {
        db.payday().insert(
            testPaydayResponseData(
                frequency = PaydayFrequency.MONTHLY,
                status = PaydayStatus.CALCULATING
            ).toPayday()
        )

        val testObserver = db.payday().loadLiveData().test()
        testObserver.awaitValue()
        val existingData = testObserver.value()
        assertNotNull(existingData)
        assertEquals(PaydayFrequency.MONTHLY, existingData?.frequency)

        val updatedData = Payday(
            status = PaydayStatus.CONFIRMED,
            frequency = PaydayFrequency.WEEKLY,
            nextDate = existingData?.nextDate,
            previousDate = existingData?.previousDate
        )
        updatedData.paydayId = existingData?.paydayId ?: 0
        db.payday().update(updatedData)

        val testObserver2 = db.payday().loadLiveData().test()
        testObserver2.awaitValue()
        assertNotNull(testObserver2.value())
        assertEquals(PaydayFrequency.WEEKLY, testObserver2.value()?.frequency)
    }

    @Test
    fun testClear() {
        val data = testPaydayResponseData()
        db.payday().insert(data.toPayday())

        db.payday().clear()
        val testObserver = db.payday().loadLiveData().test()
        testObserver.awaitValue()
        assertNull(testObserver.value())
    }
}
