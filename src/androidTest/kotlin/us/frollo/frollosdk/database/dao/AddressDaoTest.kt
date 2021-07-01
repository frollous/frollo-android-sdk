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
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.threetenabp.AndroidThreeTen
import com.jraska.livedata.test
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.mapping.toAddress
import us.frollo.frollosdk.model.testAddressResponseData

class AddressDaoTest {

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
    fun testLoadAll() {
        val data1 = testAddressResponseData(addressId = 100)
        val data2 = testAddressResponseData(addressId = 101)
        val data3 = testAddressResponseData(addressId = 102)
        val data4 = testAddressResponseData(addressId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.addresses().insertAll(*list.map { it.toAddress() }.toTypedArray())

        val testObserver = db.addresses().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testLoadByAddressId() {
        val data = testAddressResponseData(addressId = 102)
        val list = mutableListOf(testAddressResponseData(addressId = 101), data, testAddressResponseData(addressId = 103))
        db.addresses().insertAll(*list.map { it.toAddress() }.toTypedArray())

        val testObserver = db.addresses().load(data.addressId).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.addressId, testObserver.value()?.addressId)
    }

    @Test
    fun testLoadByQuery() {
        val data1 = testAddressResponseData(addressId = 100)
        val data2 = testAddressResponseData(addressId = 101)
        val data3 = testAddressResponseData(addressId = 102)
        val data4 = testAddressResponseData(addressId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.addresses().insertAll(*list.map { it.toAddress() }.toTypedArray())

        val query = SimpleSQLiteQuery("SELECT * FROM addresses WHERE address_id IN (101,102,103)")

        val testObserver = db.addresses().loadByQuery(query).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testInsertAll() {
        val data1 = testAddressResponseData(addressId = 100)
        val data2 = testAddressResponseData(addressId = 101)
        val data3 = testAddressResponseData(addressId = 102)
        val data4 = testAddressResponseData(addressId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.addresses().insertAll(*list.map { it.toAddress() }.toTypedArray())

        val testObserver = db.addresses().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testAddressResponseData()

        db.addresses().insert(data.toAddress())

        val testObserver = db.addresses().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(data.addressId, testObserver.value()[0].addressId)
    }

    @Test
    fun testGetIds() {
        val data1 = testAddressResponseData(addressId = 101)
        val data2 = testAddressResponseData(addressId = 102)
        val list = mutableListOf(data1, data2)

        db.addresses().insertAll(*list.map { it.toAddress() }.toTypedArray())

        val ids = db.addresses().getIds().sorted()

        assertEquals(2, ids.size)
        assertTrue(ids.containsAll(mutableListOf(101L, 102L)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testAddressResponseData(addressId = 100)
        val data2 = testAddressResponseData(addressId = 101)
        val data3 = testAddressResponseData(addressId = 102)
        val data4 = testAddressResponseData(addressId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.addresses().insertAll(*list.map { it.toAddress() }.toTypedArray())

        db.addresses().deleteMany(longArrayOf(100, 103))

        val testObserver = db.addresses().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testDelete() {
        val data1 = testAddressResponseData(addressId = 100)
        val data2 = testAddressResponseData(addressId = 101)
        val data3 = testAddressResponseData(addressId = 102)
        val data4 = testAddressResponseData(addressId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.addresses().insertAll(*list.map { it.toAddress() }.toTypedArray())

        db.addresses().delete(100)

        val testObserver = db.addresses().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testAddressResponseData(addressId = 100)
        val data2 = testAddressResponseData(addressId = 101)
        val data3 = testAddressResponseData(addressId = 102)
        val data4 = testAddressResponseData(addressId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.addresses().insertAll(*list.map { it.toAddress() }.toTypedArray())

        db.addresses().clear()

        val testObserver = db.addresses().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }
}
