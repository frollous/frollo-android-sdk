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
import us.frollo.frollosdk.mapping.toAddress
import us.frollo.frollosdk.mapping.toUser
import us.frollo.frollosdk.model.testAddressResponseData
import us.frollo.frollosdk.model.testModifyUserResponseData
import us.frollo.frollosdk.model.testUserResponseData

class UserDaoTest {

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
        val testObserver = db.users().load().test()
        testObserver.awaitValue()
        assertNull(testObserver.value())

        db.users().insert(testUserResponseData().toUser())

        val testObserver2 = db.users().load().test()
        testObserver2.awaitValue()
        assertNotNull(testObserver2.value())
    }

    @Test
    fun testInsert() {
        val data = testUserResponseData()
        db.users().insert(data.toUser())

        val testObserver = db.users().load().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.userId, testObserver.value()?.userId)

        db.users().insert(data.testModifyUserResponseData("New first name").toUser())

        val testObserver2 = db.users().load().test()
        testObserver2.awaitValue()
        assertNotNull(testObserver2.value())
        assertEquals(data.userId, testObserver2.value()?.userId)
        assertEquals("New first name", testObserver2.value()?.firstName)
    }

    @Test
    fun testClear() {
        val data = testUserResponseData()
        db.users().insert(data.toUser())

        db.users().clear()
        val testObserver = db.users().load().test()
        testObserver.awaitValue()
        assertNull(testObserver.value())
    }

    @Test
    fun testLoadWithRelation() {
        db.addresses().insert(testAddressResponseData(addressId = 345).toAddress())
        db.addresses().insert(testAddressResponseData(addressId = 346).toAddress())
        db.users().insert(
            testUserResponseData(
                userId = 123,
                residentialAddressId = 345,
                mailingAddressId = 345,
                previousAddressId = 346
            ).toUser()
        )

        val testObserver = db.users().loadWithRelation().test()
        testObserver.awaitValue()

        val model = testObserver.value()
        assertNotNull(model)
        assertEquals(123L, model?.user?.userId)
        assertEquals(345L, model?.residentialAddress?.addressId)
        assertEquals(345L, model?.mailingAddress?.addressId)
        assertEquals(346L, model?.previousAddress?.addressId)
    }
}
