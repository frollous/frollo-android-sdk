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
import us.frollo.frollosdk.mapping.toBudget
import us.frollo.frollosdk.mapping.toBudgetPeriodX
import us.frollo.frollosdk.model.testBudgetPeriodResponseData
import us.frollo.frollosdk.model.testBudgetResponseData

class BudgetDaoTest {

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
        val data1 = testBudgetResponseData(budgetId = 100)
        val data2 = testBudgetResponseData(budgetId = 101)
        val data3 = testBudgetResponseData(budgetId = 102)
        val data4 = testBudgetResponseData(budgetId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.budgets().insertAll(*list.map { it.toBudget() }.toList().toTypedArray())

        val testObserver = db.budgets().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testLoadBybudgetId() {
        val data = testBudgetResponseData(budgetId = 102)
        val list = mutableListOf(testBudgetResponseData(budgetId = 101), data, testBudgetResponseData(budgetId = 103))
        db.budgets().insertAll(*list.map { it.toBudget() }.toList().toTypedArray())

        val testObserver = db.budgets().load(data.budgetId).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.budgetId, testObserver.value()?.budgetId)
    }

    @Test
    fun testLoadByQuery() {
        val data1 = testBudgetResponseData(budgetId = 100)
        val data2 = testBudgetResponseData(budgetId = 101)
        val data3 = testBudgetResponseData(budgetId = 102)
        val data4 = testBudgetResponseData(budgetId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.budgets().insertAll(*list.map { it.toBudget() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT * FROM budget WHERE budget_id IN (101,102,103)")

        val testObserver = db.budgets().loadByQuery(query).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testInsertAll() {
        val data1 = testBudgetResponseData(budgetId = 100)
        val data2 = testBudgetResponseData(budgetId = 101)
        val data3 = testBudgetResponseData(budgetId = 102)
        val data4 = testBudgetResponseData(budgetId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.budgets().insertAll(*list.map { it.toBudget() }.toList().toTypedArray())

        val testObserver = db.budgets().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testBudgetResponseData()

        db.budgets().insert(data.toBudget())

        val testObserver = db.budgets().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(data.budgetId, testObserver.value()[0].budgetId)
    }

    @Test
    fun testGetIdsByQuery() {
        val data1 = testBudgetResponseData(budgetId = 100)
        val data2 = testBudgetResponseData(budgetId = 101)
        val data3 = testBudgetResponseData(budgetId = 102)
        val data4 = testBudgetResponseData(budgetId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.budgets().insertAll(*list.map { it.toBudget() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT budget_id FROM budget WHERE budget_id IN (101,102)")
        val ids = db.budgets().getIdsByQuery(query).sorted()

        assertEquals(2, ids.size)
        assertTrue(ids.containsAll(mutableListOf(101L, 102L)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testBudgetResponseData(budgetId = 100)
        val data2 = testBudgetResponseData(budgetId = 101)
        val data3 = testBudgetResponseData(budgetId = 102)
        val data4 = testBudgetResponseData(budgetId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.budgets().insertAll(*list.map { it.toBudget() }.toList().toTypedArray())

        db.budgets().deleteMany(longArrayOf(100, 103))

        val testObserver = db.budgets().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testDelete() {
        val data1 = testBudgetResponseData(budgetId = 100)
        val data2 = testBudgetResponseData(budgetId = 101)
        val data3 = testBudgetResponseData(budgetId = 102)
        val data4 = testBudgetResponseData(budgetId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.budgets().insertAll(*list.map { it.toBudget() }.toList().toTypedArray())

        db.budgets().delete(100)

        val testObserver = db.budgets().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testBudgetResponseData(budgetId = 100)
        val data2 = testBudgetResponseData(budgetId = 101)
        val data3 = testBudgetResponseData(budgetId = 102)
        val data4 = testBudgetResponseData(budgetId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.budgets().insertAll(*list.map { it.toBudget() }.toList().toTypedArray())

        db.budgets().clear()

        val testObserver = db.budgets().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }

    @Test
    fun testLoadAllWithRelation() {
        db.budgets().insert(testBudgetResponseData(budgetId = 123).toBudget())
        db.budgetPeriods().insert(testBudgetPeriodResponseData(budgetPeriodId = 456, budgetId = 123).toBudgetPeriodX())
        db.budgetPeriods().insert(testBudgetPeriodResponseData(budgetPeriodId = 457, budgetId = 123).toBudgetPeriodX())

        val testObserver = db.budgets().loadWithRelation().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)

        val model = testObserver.value()[0]

        assertEquals(123L, model.budget?.budgetId)
        assertEquals(2, model.periods?.size)
        assertEquals(456L, model.periods?.get(0)?.budgetPeriodId)
        assertEquals(457L, model.periods?.get(1)?.budgetPeriodId)
    }

    @Test
    fun testLoadBybudgetIdWithRelation() {

        db.budgets().insert(testBudgetResponseData(budgetId = 123).toBudget())
        db.budgetPeriods().insert(testBudgetPeriodResponseData(budgetPeriodId = 456, budgetId = 123).toBudgetPeriodX())
        db.budgetPeriods().insert(testBudgetPeriodResponseData(budgetPeriodId = 457, budgetId = 123).toBudgetPeriodX())

        val testObserver = db.budgets().loadWithRelation(budgetId = 123).test()
        testObserver.awaitValue()

        val model = testObserver.value()

        assertEquals(123L, model?.budget?.budgetId)
        assertEquals(2, model?.periods?.size)
        assertEquals(456L, model?.periods?.get(0)?.budgetPeriodId)
        assertEquals(457L, model?.periods?.get(1)?.budgetPeriodId)
    }

    @Test
    fun testLoadByQueryWithRelation() {
        db.budgets().insert(testBudgetResponseData(budgetId = 123).toBudget())
        db.budgetPeriods().insert(testBudgetPeriodResponseData(budgetPeriodId = 456, budgetId = 123).toBudgetPeriodX())
        db.budgetPeriods().insert(testBudgetPeriodResponseData(budgetPeriodId = 457, budgetId = 123).toBudgetPeriodX())

        val query = SimpleSQLiteQuery("SELECT * FROM budget")
        val testObserver = db.budgets().loadByQueryWithRelation(query).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)

        val model = testObserver.value()[0]

        assertEquals(123L, model.budget?.budgetId)
        assertEquals(2, model.periods?.size)
        assertEquals(456L, model.periods?.get(0)?.budgetPeriodId)
        assertEquals(457L, model.periods?.get(1)?.budgetPeriodId)
    }
}