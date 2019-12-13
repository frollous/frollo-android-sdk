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
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Before

import org.junit.Rule
import org.junit.Test
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.extensions.sqlForBudgetPeriodIds
import us.frollo.frollosdk.extensions.sqlForBudgetPeriods
import us.frollo.frollosdk.mapping.toBudget
import us.frollo.frollosdk.mapping.toBudgetPeriod
import us.frollo.frollosdk.model.testBudgetPeriodResponseData
import us.frollo.frollosdk.model.testBudgetResponseData

class BudgetPeriodDaoTest {

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
    fun testLoadByGoalPeriodId() {
        val data1 = testBudgetPeriodResponseData(budgetPeriodId = 100)
        val data2 = testBudgetPeriodResponseData(budgetPeriodId = 101)
        val data3 = testBudgetPeriodResponseData(budgetPeriodId = 102)
        val data4 = testBudgetPeriodResponseData(budgetPeriodId = 103)
        val data5 = testBudgetPeriodResponseData(budgetPeriodId = 104)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.budgetPeriods().insertAll(*list.map { it.toBudgetPeriod() }.toList().toTypedArray())

        val testObserver = db.budgetPeriods().load(budgetPeriodId = data3.budgetPeriodId).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data3.budgetPeriodId, testObserver.value()?.budgetPeriodId)
    }

    @Test
    fun testLoadByGoalId() {
        val data1 = testBudgetPeriodResponseData(budgetPeriodId = 100, budgetId = 200)
        val data2 = testBudgetPeriodResponseData(budgetPeriodId = 101, budgetId = 200)
        val data3 = testBudgetPeriodResponseData(budgetPeriodId = 102, budgetId = 201)
        val data4 = testBudgetPeriodResponseData(budgetPeriodId = 103, budgetId = 200)
        val data5 = testBudgetPeriodResponseData(budgetPeriodId = 104, budgetId = 201)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.budgetPeriods().insertAll(*list.map { it.toBudgetPeriod() }.toList().toTypedArray())

        val testObserver = db.budgetPeriods().loadByBudgetId(budgetId = 200).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(3, testObserver.value()?.size)
    }

    @Test
    fun testLoadByQuery() {
        val data1 = testBudgetPeriodResponseData(budgetPeriodId = 100, budgetId = 200)
        val data2 = testBudgetPeriodResponseData(budgetPeriodId = 101, budgetId = 200)
        val data3 = testBudgetPeriodResponseData(budgetPeriodId = 102, budgetId = 201)
        val data4 = testBudgetPeriodResponseData(budgetPeriodId = 103, budgetId = 200)
        val data5 = testBudgetPeriodResponseData(budgetPeriodId = 104, budgetId = 201)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.budgetPeriods().insertAll(*list.map { it.toBudgetPeriod() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT * FROM budget_period WHERE budget_id = 200")
        val testObserver = db.budgetPeriods().loadByQuery(query).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testInsertAll() {
        val data1 = testBudgetPeriodResponseData(budgetPeriodId = 100)
        val data2 = testBudgetPeriodResponseData(budgetPeriodId = 101)
        val data3 = testBudgetPeriodResponseData(budgetPeriodId = 102)
        val data4 = testBudgetPeriodResponseData(budgetPeriodId = 103)
        val data5 = testBudgetPeriodResponseData(budgetPeriodId = 104)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.budgetPeriods().insertAll(*list.map { it.toBudgetPeriod() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT * FROM budget_period")
        val testObserver = db.budgetPeriods().loadByQuery(query).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(5, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testBudgetPeriodResponseData(budgetPeriodId = 100)

        db.budgetPeriods().insert(data.toBudgetPeriod())

        val testObserver = db.budgetPeriods().load(budgetPeriodId = data.budgetPeriodId).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.budgetPeriodId, testObserver.value()?.budgetPeriodId)
    }

    @Test
    fun testGetIdsByGoalIds() {
        val data1 = testBudgetPeriodResponseData(budgetPeriodId = 100, budgetId = 200)
        val data2 = testBudgetPeriodResponseData(budgetPeriodId = 101, budgetId = 200)
        val data3 = testBudgetPeriodResponseData(budgetPeriodId = 102, budgetId = 201)
        val data4 = testBudgetPeriodResponseData(budgetPeriodId = 103, budgetId = 202)
        val data5 = testBudgetPeriodResponseData(budgetPeriodId = 104, budgetId = 201)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.budgetPeriods().insertAll(*list.map { it.toBudgetPeriod() }.toList().toTypedArray())

        val ids = db.budgetPeriods().getIdsByBudgetIds(budgetIds = longArrayOf(200, 202)).sorted()
        assertEquals(3, ids.size)
        assertTrue(ids.containsAll(mutableListOf(100L, 101L, 103L)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testBudgetPeriodResponseData(budgetPeriodId = 100)
        val data2 = testBudgetPeriodResponseData(budgetPeriodId = 101)
        val data3 = testBudgetPeriodResponseData(budgetPeriodId = 102)
        val data4 = testBudgetPeriodResponseData(budgetPeriodId = 103)
        val data5 = testBudgetPeriodResponseData(budgetPeriodId = 104)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.budgetPeriods().insertAll(*list.map { it.toBudgetPeriod() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT * FROM budget_period")
        var testObserver = db.budgetPeriods().loadByQuery(query).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(5, testObserver.value().size)

        db.budgetPeriods().deleteMany(longArrayOf(100, 103))

        testObserver = db.budgetPeriods().loadByQuery(query).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testDelete() {
        val data1 = testBudgetPeriodResponseData(budgetPeriodId = 100)
        val data2 = testBudgetPeriodResponseData(budgetPeriodId = 101)
        val data3 = testBudgetPeriodResponseData(budgetPeriodId = 102)
        val data4 = testBudgetPeriodResponseData(budgetPeriodId = 103)
        val data5 = testBudgetPeriodResponseData(budgetPeriodId = 104)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.budgetPeriods().insertAll(*list.map { it.toBudgetPeriod() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT * FROM budget_period")
        var testObserver = db.budgetPeriods().loadByQuery(query).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(5, testObserver.value().size)

        db.budgetPeriods().delete(100)

        testObserver = db.budgetPeriods().loadByQuery(query).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testBudgetPeriodResponseData(budgetPeriodId = 100)
        val data2 = testBudgetPeriodResponseData(budgetPeriodId = 101)
        val data3 = testBudgetPeriodResponseData(budgetPeriodId = 102)
        val data4 = testBudgetPeriodResponseData(budgetPeriodId = 103)
        val data5 = testBudgetPeriodResponseData(budgetPeriodId = 104)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.budgetPeriods().insertAll(*list.map { it.toBudgetPeriod() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT * FROM budget_period")
        var testObserver = db.budgetPeriods().loadByQuery(query).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(5, testObserver.value().size)

        db.budgetPeriods().clear()

        testObserver = db.budgetPeriods().loadByQuery(query).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }

    @Test
    fun testLoadByGoalPeriodIdWithRelation() {
        db.budgets().insert(testBudgetResponseData(budgetId = 123).toBudget())
        db.budgetPeriods().insert(testBudgetPeriodResponseData(budgetPeriodId = 456, budgetId = 123).toBudgetPeriod())

        val testObserver = db.budgetPeriods().loadWithRelation(budgetPeriodId = 456).test()

        testObserver.awaitValue()

        val model = testObserver.value()

        assertNotNull(model)
        assertEquals(123L, model?.budget?.budget?.budgetId)
        assertEquals(123L, model?.budgetPeriod?.budgetId)
        assertEquals(456L, model?.budgetPeriod?.budgetPeriodId)
    }

    @Test
    fun testLoadByGoalIdWithRelation() {
        db.budgets().insert(testBudgetResponseData(budgetId = 123).toBudget())
        db.budgetPeriods().insert(testBudgetPeriodResponseData(budgetPeriodId = 456, budgetId = 123).toBudgetPeriod())

        val testObserver = db.budgetPeriods().loadByQueryWithRelation(sqlForBudgetPeriods(budgetId = 123)).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)

        val model = testObserver.value()[0]

        model.budgetPeriod
        assertEquals(123L, model.budget?.budget?.budgetId)
        assertEquals(123L, model.budgetPeriod?.budgetId)
        assertEquals(456L, model.budgetPeriod?.budgetPeriodId)
    }

    @Test
    fun testLoadByQueryWithRelation() {
        db.budgets().insert(testBudgetResponseData(budgetId = 123).toBudget())
        db.budgetPeriods().insert(testBudgetPeriodResponseData(budgetPeriodId = 456, budgetId = 123).toBudgetPeriod())

        val query = SimpleSQLiteQuery("SELECT * FROM budget_period WHERE budget_id = 123")
        val testObserver = db.budgetPeriods().loadByQueryWithRelation(query).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)

        val model = testObserver.value()[0]

        assertEquals(123L, model.budget?.budget?.budgetId)
        assertEquals(123L, model.budgetPeriod?.budgetId)
        assertEquals(456L, model.budgetPeriod?.budgetPeriodId)
    }

    @Test
    fun testBudgetPeriodGetIds() {

        db.budgetPeriods().insert(testBudgetPeriodResponseData(budgetPeriodId = 456, budgetId = 123).toBudgetPeriod())
        db.budgetPeriods().insert(testBudgetPeriodResponseData(budgetPeriodId = 4561, budgetId = 123).toBudgetPeriod())

        val testObserver = db.budgetPeriods().getIds(sqlForBudgetPeriodIds(123))
        // test data start date 2019-02-01
        val testObserver2 = db.budgetPeriods().getIds(sqlForBudgetPeriodIds(123, "2019-01-02", "2019-02-10"))

        assertEquals(2, testObserver.size)
        assertEquals(456, testObserver[0])
        assertEquals(2, testObserver2.size)
        assertEquals(4561, testObserver2[1])
    }
}