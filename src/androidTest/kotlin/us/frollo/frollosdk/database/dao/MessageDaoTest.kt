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
import org.junit.Before

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.extensions.generateSQLQueryMessages

import us.frollo.frollosdk.model.testMessageResponseData
import us.frollo.frollosdk.model.testModifyUserResponseData

class MessageDaoTest {

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
        val data1 = testMessageResponseData(read = false)
        val data2 = testMessageResponseData(read = true)
        val data3 = testMessageResponseData(read = false)
        val data4 = testMessageResponseData(read = true)
        val list = mutableListOf(data1, data2, data3, data4)

        db.messages().insertAll(*list.toTypedArray())

        val testObserver = db.messages().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testLoadByRead() {
        val data1 = testMessageResponseData(read = false)
        val data2 = testMessageResponseData(read = true)
        val data3 = testMessageResponseData(read = false)
        val data4 = testMessageResponseData(read = true)
        val list = mutableListOf(data1, data2, data3, data4)

        db.messages().insertAll(*list.toTypedArray())

        val testObserver = db.messages().load(readBool = false).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testLoadByMessageType() {
        val data1 = testMessageResponseData(types = mutableListOf("survey"))
        val data2 = testMessageResponseData(types = mutableListOf("event"))
        val data3 = testMessageResponseData(types = mutableListOf("survey", "welcome"))
        val data4 = testMessageResponseData(types = mutableListOf("dashboard_survey"))
        val list = mutableListOf(data1, data2, data3, data4)

        db.messages().insertAll(*list.toTypedArray())

        val testObserver = db.messages().load("survey").test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testLoadByQuery() {
        val data1 = testMessageResponseData(types = mutableListOf("survey"), read = false)
        val data2 = testMessageResponseData(types = mutableListOf("event"), read = false)
        val data3 = testMessageResponseData(types = mutableListOf("survey", "welcome"), read = true)
        val data4 = testMessageResponseData(types = mutableListOf("dashboard_event"), read = false)
        val data5 = testMessageResponseData(types = mutableListOf("survey", "dashboard_event"), read = false)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.messages().insertAll(*list.toTypedArray())

        val messageTypes = mutableListOf("survey", "dashboard_event")
        val query = generateSQLQueryMessages(messageTypes, false)

        val testObserver = db.messages().loadByQuery(query).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testFindByMessageId() {
        val data = testMessageResponseData()
        val list = mutableListOf(testMessageResponseData(), data, testMessageResponseData())
        db.messages().insertAll(*list.toTypedArray())

        val testObserver = db.messages().load(data.messageId).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.messageId, testObserver.value()?.messageId)
    }

    @Test
    fun testInsertAll() {
        val list = mutableListOf(testMessageResponseData(), testMessageResponseData(), testMessageResponseData())
        db.messages().insertAll(*list.toTypedArray())

        val testObserver = db.messages().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testMessageResponseData()
        db.messages().insert(data)

        val testObserver = db.messages().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(data.messageId, testObserver.value()[0].messageId)

        db.messages().insert(data.testModifyUserResponseData("New title"))

        val testObserver2 = db.messages().load().test()
        testObserver2.awaitValue()
        assertTrue(testObserver2.value().isNotEmpty())
        assertEquals(data.messageId, testObserver.value()[0].messageId)
        assertEquals("New title", testObserver.value()[0].title)
    }

    @Test
    fun testGetStaleIds() {
        val data1 = testMessageResponseData(msgId = 100)
        val data2 = testMessageResponseData(msgId = 101)
        val data3 = testMessageResponseData(msgId = 102)
        val data4 = testMessageResponseData(msgId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.messages().insertAll(*list.toTypedArray())

        val staleIds = db.messages().getStaleIds(longArrayOf(100, 103)).sorted()
        assertEquals(2, staleIds.size)
        assertTrue(staleIds.containsAll(mutableListOf<Long>(101, 102)))
    }

    @Test
    fun testGetUnreadStaleIds() {
        val data1 = testMessageResponseData(msgId = 100, read = true)
        val data2 = testMessageResponseData(msgId = 101, read = false)
        val data3 = testMessageResponseData(msgId = 102, read = false)
        val data4 = testMessageResponseData(msgId = 103, read = false)
        val list = mutableListOf(data1, data2, data3, data4)

        db.messages().insertAll(*list.toTypedArray())

        val staleIds = db.messages().getUnreadStaleIds(longArrayOf(101)).sorted()
        assertEquals(2, staleIds.size)
        assertTrue(staleIds.containsAll(mutableListOf<Long>(102, 103)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testMessageResponseData(msgId = 100)
        val data2 = testMessageResponseData(msgId = 101)
        val data3 = testMessageResponseData(msgId = 102)
        val data4 = testMessageResponseData(msgId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.messages().insertAll(*list.toTypedArray())

        db.messages().deleteMany(longArrayOf(100, 103))

        val testObserver = db.messages().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testDelete() {
        val data1 = testMessageResponseData(msgId = 100)
        val data2 = testMessageResponseData(msgId = 101)
        val data3 = testMessageResponseData(msgId = 102)
        val data4 = testMessageResponseData(msgId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.messages().insertAll(*list.toTypedArray())

        db.messages().delete(100)

        val testObserver = db.messages().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testMessageResponseData(msgId = 100)
        val data2 = testMessageResponseData(msgId = 101)
        val data3 = testMessageResponseData(msgId = 102)
        val data4 = testMessageResponseData(msgId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.messages().insertAll(*list.toTypedArray())

        db.messages().clear()

        val testObserver = db.messages().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }
}