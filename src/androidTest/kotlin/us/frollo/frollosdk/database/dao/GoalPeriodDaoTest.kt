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
import us.frollo.frollosdk.mapping.toGoal
import us.frollo.frollosdk.mapping.toGoalPeriod
import us.frollo.frollosdk.model.testGoalPeriodResponseData
import us.frollo.frollosdk.model.testGoalResponseData

class GoalPeriodDaoTest {

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
        val data1 = testGoalPeriodResponseData(goalPeriodId = 100)
        val data2 = testGoalPeriodResponseData(goalPeriodId = 101)
        val data3 = testGoalPeriodResponseData(goalPeriodId = 102)
        val data4 = testGoalPeriodResponseData(goalPeriodId = 103)
        val data5 = testGoalPeriodResponseData(goalPeriodId = 104)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.goalPeriods().insertAll(*list.map { it.toGoalPeriod() }.toList().toTypedArray())

        val testObserver = db.goalPeriods().load(goalPeriodId = data3.goalPeriodId).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data3.goalPeriodId, testObserver.value()?.goalPeriodId)
    }

    @Test
    fun testLoadByGoalId() {
        val data1 = testGoalPeriodResponseData(goalPeriodId = 100, goalId = 200)
        val data2 = testGoalPeriodResponseData(goalPeriodId = 101, goalId = 200)
        val data3 = testGoalPeriodResponseData(goalPeriodId = 102, goalId = 201)
        val data4 = testGoalPeriodResponseData(goalPeriodId = 103, goalId = 200)
        val data5 = testGoalPeriodResponseData(goalPeriodId = 104, goalId = 201)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.goalPeriods().insertAll(*list.map { it.toGoalPeriod() }.toList().toTypedArray())

        val testObserver = db.goalPeriods().loadByGoalId(goalId = 200).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(3, testObserver.value()?.size)
    }

    @Test
    fun testLoadByQuery() {
        val data1 = testGoalPeriodResponseData(goalPeriodId = 100, goalId = 200)
        val data2 = testGoalPeriodResponseData(goalPeriodId = 101, goalId = 200)
        val data3 = testGoalPeriodResponseData(goalPeriodId = 102, goalId = 201)
        val data4 = testGoalPeriodResponseData(goalPeriodId = 103, goalId = 200)
        val data5 = testGoalPeriodResponseData(goalPeriodId = 104, goalId = 201)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.goalPeriods().insertAll(*list.map { it.toGoalPeriod() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT * FROM goal_period WHERE goal_id = 200")
        val testObserver = db.goalPeriods().loadByQuery(query).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testInsertAll() {
        val data1 = testGoalPeriodResponseData(goalPeriodId = 100)
        val data2 = testGoalPeriodResponseData(goalPeriodId = 101)
        val data3 = testGoalPeriodResponseData(goalPeriodId = 102)
        val data4 = testGoalPeriodResponseData(goalPeriodId = 103)
        val data5 = testGoalPeriodResponseData(goalPeriodId = 104)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.goalPeriods().insertAll(*list.map { it.toGoalPeriod() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT * FROM goal_period")
        val testObserver = db.goalPeriods().loadByQuery(query).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(5, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testGoalPeriodResponseData(goalPeriodId = 100)

        db.goalPeriods().insert(data.toGoalPeriod())

        val testObserver = db.goalPeriods().load(goalPeriodId = data.goalPeriodId).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.goalPeriodId, testObserver.value()?.goalPeriodId)
    }

    @Test
    fun testGetIdsByGoalIds() {
        val data1 = testGoalPeriodResponseData(goalPeriodId = 100, goalId = 200)
        val data2 = testGoalPeriodResponseData(goalPeriodId = 101, goalId = 200)
        val data3 = testGoalPeriodResponseData(goalPeriodId = 102, goalId = 201)
        val data4 = testGoalPeriodResponseData(goalPeriodId = 103, goalId = 202)
        val data5 = testGoalPeriodResponseData(goalPeriodId = 104, goalId = 201)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.goalPeriods().insertAll(*list.map { it.toGoalPeriod() }.toList().toTypedArray())

        val ids = db.goalPeriods().getIdsByGoalIds(goalIds = longArrayOf(200, 202)).sorted()
        assertEquals(3, ids.size)
        assertTrue(ids.containsAll(mutableListOf(100L, 101L, 103L)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testGoalPeriodResponseData(goalPeriodId = 100)
        val data2 = testGoalPeriodResponseData(goalPeriodId = 101)
        val data3 = testGoalPeriodResponseData(goalPeriodId = 102)
        val data4 = testGoalPeriodResponseData(goalPeriodId = 103)
        val data5 = testGoalPeriodResponseData(goalPeriodId = 104)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.goalPeriods().insertAll(*list.map { it.toGoalPeriod() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT * FROM goal_period")
        var testObserver = db.goalPeriods().loadByQuery(query).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(5, testObserver.value().size)

        db.goalPeriods().deleteMany(longArrayOf(100, 103))

        testObserver = db.goalPeriods().loadByQuery(query).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testDelete() {
        val data1 = testGoalPeriodResponseData(goalPeriodId = 100)
        val data2 = testGoalPeriodResponseData(goalPeriodId = 101)
        val data3 = testGoalPeriodResponseData(goalPeriodId = 102)
        val data4 = testGoalPeriodResponseData(goalPeriodId = 103)
        val data5 = testGoalPeriodResponseData(goalPeriodId = 104)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.goalPeriods().insertAll(*list.map { it.toGoalPeriod() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT * FROM goal_period")
        var testObserver = db.goalPeriods().loadByQuery(query).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(5, testObserver.value().size)

        db.goalPeriods().delete(100)

        testObserver = db.goalPeriods().loadByQuery(query).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testGoalPeriodResponseData(goalPeriodId = 100)
        val data2 = testGoalPeriodResponseData(goalPeriodId = 101)
        val data3 = testGoalPeriodResponseData(goalPeriodId = 102)
        val data4 = testGoalPeriodResponseData(goalPeriodId = 103)
        val data5 = testGoalPeriodResponseData(goalPeriodId = 104)
        val list = mutableListOf(data1, data2, data3, data4, data5)

        db.goalPeriods().insertAll(*list.map { it.toGoalPeriod() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT * FROM goal_period")
        var testObserver = db.goalPeriods().loadByQuery(query).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(5, testObserver.value().size)

        db.goalPeriods().clear()

        testObserver = db.goalPeriods().loadByQuery(query).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }

    @Test
    fun testLoadByGoalPeriodIdWithRelation() {
        db.goals().insert(testGoalResponseData(goalId = 123, accountId = 345).toGoal())
        db.goalPeriods().insert(testGoalPeriodResponseData(goalPeriodId = 456, goalId = 123).toGoalPeriod())

        val testObserver = db.goalPeriods().loadWithRelation(goalPeriodId = 456).test()

        testObserver.awaitValue()

        val model = testObserver.value()

        assertNotNull(model)
        assertEquals(123L, model?.goal?.goal?.goalId)
        assertEquals(123L, model?.goalPeriod?.goalId)
        assertEquals(456L, model?.goalPeriod?.goalPeriodId)
    }

    @Test
    fun testLoadByGoalIdWithRelation() {
        db.goals().insert(testGoalResponseData(goalId = 123, accountId = 345).toGoal())
        db.goalPeriods().insert(testGoalPeriodResponseData(goalPeriodId = 456, goalId = 123).toGoalPeriod())

        val testObserver = db.goalPeriods().loadByGoalIdWithRelation(goalId = 123).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)

        val model = testObserver.value()[0]

        assertEquals(123L, model.goal?.goal?.goalId)
        assertEquals(123L, model.goalPeriod?.goalId)
        assertEquals(456L, model.goalPeriod?.goalPeriodId)
    }

    @Test
    fun testLoadByQueryWithRelation() {
        db.goals().insert(testGoalResponseData(goalId = 123, accountId = 345).toGoal())
        db.goalPeriods().insert(testGoalPeriodResponseData(goalPeriodId = 456, goalId = 123).toGoalPeriod())

        val query = SimpleSQLiteQuery("SELECT * FROM goal_period WHERE goal_id = 123")
        val testObserver = db.goalPeriods().loadByQueryWithRelation(query).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)

        val model = testObserver.value()[0]

        assertEquals(123L, model.goal?.goal?.goalId)
        assertEquals(123L, model.goalPeriod?.goalId)
        assertEquals(456L, model.goalPeriod?.goalPeriodId)
    }
}
