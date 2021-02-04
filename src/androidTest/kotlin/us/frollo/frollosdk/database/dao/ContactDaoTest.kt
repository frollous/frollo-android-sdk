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
import us.frollo.frollosdk.mapping.toContact
import us.frollo.frollosdk.model.coredata.contacts.PaymentMethod
import us.frollo.frollosdk.model.testContactResponseData

class ContactDaoTest {

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
        val data1 = testContactResponseData(contactId = 100, paymentMethod = PaymentMethod.PAY_ID)
        val data2 = testContactResponseData(contactId = 101, paymentMethod = PaymentMethod.PAY_ANYONE)
        val data3 = testContactResponseData(contactId = 102, paymentMethod = PaymentMethod.BPAY)
        val data4 = testContactResponseData(contactId = 103, paymentMethod = PaymentMethod.INTERNATIONAL)
        val list = mutableListOf(data1, data2, data3, data4)

        db.contacts().insertAll(*list.map { it.toContact() }.toList().toTypedArray())

        val testObserver = db.contacts().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testLoadByContactId() {
        val data = testContactResponseData(contactId = 102)
        val list = mutableListOf(testContactResponseData(contactId = 101), data, testContactResponseData(contactId = 103))
        db.contacts().insertAll(*list.map { it.toContact() }.toList().toTypedArray())

        val testObserver = db.contacts().load(data.contactId).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.contactId, testObserver.value()?.contactId)
    }

    @Test
    fun testLoadByQuery() {
        val data1 = testContactResponseData(contactId = 100, paymentMethod = PaymentMethod.PAY_ID)
        val data2 = testContactResponseData(contactId = 101, paymentMethod = PaymentMethod.PAY_ANYONE)
        val data3 = testContactResponseData(contactId = 102, paymentMethod = PaymentMethod.BPAY)
        val data4 = testContactResponseData(contactId = 103, paymentMethod = PaymentMethod.INTERNATIONAL)
        val list = mutableListOf(data1, data2, data3, data4)

        db.contacts().insertAll(*list.map { it.toContact() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT * FROM contact WHERE contact_id IN (101,102,103)")

        val testObserver = db.contacts().loadByQuery(query).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testInsertAll() {
        val data1 = testContactResponseData(contactId = 100, paymentMethod = PaymentMethod.PAY_ID)
        val data2 = testContactResponseData(contactId = 101, paymentMethod = PaymentMethod.PAY_ANYONE)
        val data3 = testContactResponseData(contactId = 102, paymentMethod = PaymentMethod.BPAY)
        val data4 = testContactResponseData(contactId = 103, paymentMethod = PaymentMethod.INTERNATIONAL)
        val list = mutableListOf(data1, data2, data3, data4)

        db.contacts().insertAll(*list.map { it.toContact() }.toList().toTypedArray())

        val testObserver = db.contacts().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testContactResponseData()

        db.contacts().insert(data.toContact())

        val testObserver = db.contacts().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(data.contactId, testObserver.value()[0].contactId)
    }

    @Test
    fun testGetIdsByQuery() {
        val data1 = testContactResponseData(contactId = 100, paymentMethod = PaymentMethod.PAY_ID)
        val data2 = testContactResponseData(contactId = 101, paymentMethod = PaymentMethod.PAY_ANYONE)
        val data3 = testContactResponseData(contactId = 102, paymentMethod = PaymentMethod.BPAY)
        val data4 = testContactResponseData(contactId = 103, paymentMethod = PaymentMethod.INTERNATIONAL)
        val list = mutableListOf(data1, data2, data3, data4)

        db.contacts().insertAll(*list.map { it.toContact() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT contact_id FROM contact WHERE contact_id IN (101,102)")
        val ids = db.contacts().getIdsByQuery(query).sorted()

        assertEquals(2, ids.size)
        assertTrue(ids.containsAll(mutableListOf(101L, 102L)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testContactResponseData(contactId = 100, paymentMethod = PaymentMethod.PAY_ID)
        val data2 = testContactResponseData(contactId = 101, paymentMethod = PaymentMethod.PAY_ANYONE)
        val data3 = testContactResponseData(contactId = 102, paymentMethod = PaymentMethod.BPAY)
        val data4 = testContactResponseData(contactId = 103, paymentMethod = PaymentMethod.INTERNATIONAL)
        val list = mutableListOf(data1, data2, data3, data4)

        db.contacts().insertAll(*list.map { it.toContact() }.toList().toTypedArray())

        db.contacts().deleteMany(longArrayOf(100, 103))

        val testObserver = db.contacts().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testContactResponseData(contactId = 100, paymentMethod = PaymentMethod.PAY_ID)
        val data2 = testContactResponseData(contactId = 101, paymentMethod = PaymentMethod.PAY_ANYONE)
        val data3 = testContactResponseData(contactId = 102, paymentMethod = PaymentMethod.BPAY)
        val data4 = testContactResponseData(contactId = 103, paymentMethod = PaymentMethod.INTERNATIONAL)
        val list = mutableListOf(data1, data2, data3, data4)

        db.contacts().insertAll(*list.map { it.toContact() }.toList().toTypedArray())

        db.contacts().clear()

        val testObserver = db.contacts().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }
}
