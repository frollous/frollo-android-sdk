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
import us.frollo.frollosdk.mapping.toAccount
import us.frollo.frollosdk.mapping.toCard
import us.frollo.frollosdk.model.testAccountResponseData
import us.frollo.frollosdk.model.testCardResponseData

class CardDaoTest {

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
        val data1 = testCardResponseData(cardId = 100)
        val data2 = testCardResponseData(cardId = 101)
        val data3 = testCardResponseData(cardId = 102)
        val data4 = testCardResponseData(cardId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.cards().insertAll(*list.map { it.toCard() }.toTypedArray())

        val testObserver = db.cards().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testLoadByCardId() {
        val data = testCardResponseData(cardId = 102)
        val list = mutableListOf(testCardResponseData(cardId = 101), data, testCardResponseData(cardId = 103))
        db.cards().insertAll(*list.map { it.toCard() }.toTypedArray())

        val testObserver = db.cards().load(data.cardId).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.cardId, testObserver.value()?.cardId)
    }

    @Test
    fun testLoadByQuery() {
        val data1 = testCardResponseData(cardId = 100)
        val data2 = testCardResponseData(cardId = 101)
        val data3 = testCardResponseData(cardId = 102)
        val data4 = testCardResponseData(cardId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.cards().insertAll(*list.map { it.toCard() }.toTypedArray())

        val query = SimpleSQLiteQuery("SELECT * FROM card WHERE card_id IN (101,102,103)")

        val testObserver = db.cards().loadByQuery(query).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testInsertAll() {
        val data1 = testCardResponseData(cardId = 100)
        val data2 = testCardResponseData(cardId = 101)
        val data3 = testCardResponseData(cardId = 102)
        val data4 = testCardResponseData(cardId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.cards().insertAll(*list.map { it.toCard() }.toTypedArray())

        val testObserver = db.cards().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testCardResponseData()

        db.cards().insert(data.toCard())

        val testObserver = db.cards().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(data.cardId, testObserver.value()[0].cardId)
    }

    @Test
    fun testGetIds() {
        val data1 = testCardResponseData(cardId = 101)
        val data2 = testCardResponseData(cardId = 102)
        val list = mutableListOf(data1, data2)

        db.cards().insertAll(*list.map { it.toCard() }.toTypedArray())

        val ids = db.cards().getIds().sorted()

        assertEquals(2, ids.size)
        assertTrue(ids.containsAll(mutableListOf(101L, 102L)))
    }

    @Test
    fun testGetIdsByAccountIds() {
        val data1 = testCardResponseData(cardId = 100, accountId = 1)
        val data2 = testCardResponseData(cardId = 101, accountId = 2)
        val data3 = testCardResponseData(cardId = 102, accountId = 2)
        val data4 = testCardResponseData(cardId = 103, accountId = 1)
        val data5 = testCardResponseData(cardId = 104, accountId = 3)
        val data6 = testCardResponseData(cardId = 105, accountId = 1)
        val list = mutableListOf(data1, data2, data3, data4, data5, data6)
        db.cards().insertAll(*list.map { it.toCard() }.toTypedArray())

        val ids = db.cards().getIdsByAccountIds(longArrayOf(2, 3))
        assertTrue(ids.isNotEmpty())
        assertEquals(3, ids.size)
        assertTrue(ids.toList().containsAll(listOf(101L, 102L, 104L)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testCardResponseData(cardId = 100)
        val data2 = testCardResponseData(cardId = 101)
        val data3 = testCardResponseData(cardId = 102)
        val data4 = testCardResponseData(cardId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.cards().insertAll(*list.map { it.toCard() }.toTypedArray())

        db.cards().deleteMany(longArrayOf(100, 103))

        val testObserver = db.cards().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testDelete() {
        val data1 = testCardResponseData(cardId = 100)
        val data2 = testCardResponseData(cardId = 101)
        val data3 = testCardResponseData(cardId = 102)
        val data4 = testCardResponseData(cardId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.cards().insertAll(*list.map { it.toCard() }.toTypedArray())

        db.cards().delete(100)

        val testObserver = db.cards().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testCardResponseData(cardId = 100)
        val data2 = testCardResponseData(cardId = 101)
        val data3 = testCardResponseData(cardId = 102)
        val data4 = testCardResponseData(cardId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.cards().insertAll(*list.map { it.toCard() }.toTypedArray())

        db.cards().clear()

        val testObserver = db.cards().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }

    @Test
    fun testLoadAllWithRelation() {
        db.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        db.cards().insert(testCardResponseData(cardId = 123, accountId = 345).toCard())

        val testObserver = db.cards().loadWithRelation().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)

        val model = testObserver.value()[0]

        assertEquals(123L, model.card?.cardId)
        assertEquals(345L, model.account?.account?.accountId)
    }

    @Test
    fun testLoadByCardIdWithRelation() {
        db.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        db.cards().insert(testCardResponseData(cardId = 123, accountId = 345).toCard())

        val testObserver = db.cards().loadWithRelation(cardId = 123).test()
        testObserver.awaitValue()

        val model = testObserver.value()

        assertEquals(123L, model?.card?.cardId)
        assertEquals(345L, model?.account?.account?.accountId)
    }

    @Test
    fun testLoadByQueryWithRelation() {
        db.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        db.cards().insert(testCardResponseData(cardId = 123, accountId = 345).toCard())

        val query = SimpleSQLiteQuery("SELECT * FROM card")
        val testObserver = db.cards().loadByQueryWithRelation(query).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)

        val model = testObserver.value()[0]

        assertEquals(123L, model.card?.cardId)
        assertEquals(345L, model.account?.account?.accountId)
    }
}
