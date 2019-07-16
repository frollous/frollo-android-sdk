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
import us.frollo.frollosdk.mapping.toAccount
import us.frollo.frollosdk.mapping.toGoal
import us.frollo.frollosdk.mapping.toGoalPeriod
import us.frollo.frollosdk.model.testAccountResponseData
import us.frollo.frollosdk.model.testGoalPeriodResponseData
import us.frollo.frollosdk.model.testGoalResponseData

class GoalDaoTest {

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
        val data1 = testGoalResponseData(goalId = 100)
        val data2 = testGoalResponseData(goalId = 101)
        val data3 = testGoalResponseData(goalId = 102)
        val data4 = testGoalResponseData(goalId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.goals().insertAll(*list.map { it.toGoal() }.toList().toTypedArray())

        val testObserver = db.goals().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testLoadByGoalId() {
        val data = testGoalResponseData(goalId = 102)
        val list = mutableListOf(testGoalResponseData(goalId = 101), data, testGoalResponseData(goalId = 103))
        db.goals().insertAll(*list.map { it.toGoal() }.toList().toTypedArray())

        val testObserver = db.goals().load(data.goalId).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.goalId, testObserver.value()?.goalId)
    }

    @Test
    fun testLoadByQuery() {
        val data1 = testGoalResponseData(goalId = 100)
        val data2 = testGoalResponseData(goalId = 101)
        val data3 = testGoalResponseData(goalId = 102)
        val data4 = testGoalResponseData(goalId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.goals().insertAll(*list.map { it.toGoal() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT * FROM goal WHERE goal_id IN (101,102,103)")

        val testObserver = db.goals().loadByQuery(query).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testInsertAll() {
        val data1 = testGoalResponseData(goalId = 100)
        val data2 = testGoalResponseData(goalId = 101)
        val data3 = testGoalResponseData(goalId = 102)
        val data4 = testGoalResponseData(goalId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.goals().insertAll(*list.map { it.toGoal() }.toList().toTypedArray())

        val testObserver = db.goals().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testGoalResponseData()

        db.goals().insert(data.toGoal())

        val testObserver = db.goals().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(data.goalId, testObserver.value()[0].goalId)
    }

    @Test
    fun testGetIdsByQuery() {
        val data1 = testGoalResponseData(goalId = 100)
        val data2 = testGoalResponseData(goalId = 101)
        val data3 = testGoalResponseData(goalId = 102)
        val data4 = testGoalResponseData(goalId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.goals().insertAll(*list.map { it.toGoal() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT goal_id FROM goal WHERE goal_id IN (101,102)")
        val ids = db.goals().getIdsByQuery(query).sorted()

        assertEquals(2, ids.size)
        assertTrue(ids.containsAll(mutableListOf(101L, 102L)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testGoalResponseData(goalId = 100)
        val data2 = testGoalResponseData(goalId = 101)
        val data3 = testGoalResponseData(goalId = 102)
        val data4 = testGoalResponseData(goalId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.goals().insertAll(*list.map { it.toGoal() }.toList().toTypedArray())

        db.goals().deleteMany(longArrayOf(100, 103))

        val testObserver = db.goals().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testDelete() {
        val data1 = testGoalResponseData(goalId = 100)
        val data2 = testGoalResponseData(goalId = 101)
        val data3 = testGoalResponseData(goalId = 102)
        val data4 = testGoalResponseData(goalId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.goals().insertAll(*list.map { it.toGoal() }.toList().toTypedArray())

        db.goals().delete(100)

        val testObserver = db.goals().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testGoalResponseData(goalId = 100)
        val data2 = testGoalResponseData(goalId = 101)
        val data3 = testGoalResponseData(goalId = 102)
        val data4 = testGoalResponseData(goalId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.goals().insertAll(*list.map { it.toGoal() }.toList().toTypedArray())

        db.goals().clear()

        val testObserver = db.goals().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }

    @Test
    fun testLoadAllWithRelation() {
        db.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        db.goals().insert(testGoalResponseData(goalId = 123, accountId = 345).toGoal())
        db.goalPeriods().insert(testGoalPeriodResponseData(goalPeriodId = 456, goalId = 123).toGoalPeriod())
        db.goalPeriods().insert(testGoalPeriodResponseData(goalPeriodId = 457, goalId = 123).toGoalPeriod())

        val testObserver = db.goals().loadWithRelation().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)

        val model = testObserver.value()[0]

        assertEquals(123L, model.goal?.goalId)
        assertEquals(345L, model.account?.account?.accountId)
        assertEquals(2, model.periods?.size)
        assertEquals(456L, model.periods?.get(0)?.goalPeriodId)
        assertEquals(457L, model.periods?.get(1)?.goalPeriodId)
    }

    @Test
    fun testLoadByGoalIdWithRelation() {
        db.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        db.goals().insert(testGoalResponseData(goalId = 123, accountId = 345).toGoal())
        db.goalPeriods().insert(testGoalPeriodResponseData(goalPeriodId = 456, goalId = 123).toGoalPeriod())
        db.goalPeriods().insert(testGoalPeriodResponseData(goalPeriodId = 457, goalId = 123).toGoalPeriod())

        val testObserver = db.goals().loadWithRelation(goalId = 123).test()
        testObserver.awaitValue()

        val model = testObserver.value()

        assertEquals(123L, model?.goal?.goalId)
        assertEquals(345L, model?.account?.account?.accountId)
        assertEquals(2, model?.periods?.size)
        assertEquals(456L, model?.periods?.get(0)?.goalPeriodId)
        assertEquals(457L, model?.periods?.get(1)?.goalPeriodId)
    }

    @Test
    fun testLoadByQueryWithRelation() {
        db.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        db.goals().insert(testGoalResponseData(goalId = 123, accountId = 345).toGoal())
        db.goalPeriods().insert(testGoalPeriodResponseData(goalPeriodId = 456, goalId = 123).toGoalPeriod())
        db.goalPeriods().insert(testGoalPeriodResponseData(goalPeriodId = 457, goalId = 123).toGoalPeriod())

        val query = SimpleSQLiteQuery("SELECT * FROM goal")
        val testObserver = db.goals().loadByQueryWithRelation(query).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)

        val model = testObserver.value()[0]

        assertEquals(123L, model.goal?.goalId)
        assertEquals(345L, model.account?.account?.accountId)
        assertEquals(2, model.periods?.size)
        assertEquals(456L, model.periods?.get(0)?.goalPeriodId)
        assertEquals(457L, model.periods?.get(1)?.goalPeriodId)
    }
}