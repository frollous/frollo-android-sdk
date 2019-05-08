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
import org.junit.Assert.*
import org.junit.Before

import org.junit.Rule
import org.junit.Test
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.extensions.sqlForUserTags
import us.frollo.frollosdk.model.coredata.aggregation.tags.TagsSortType
import us.frollo.frollosdk.model.coredata.aggregation.tags.TransactionTag
import us.frollo.frollosdk.model.coredata.shared.OrderType
import us.frollo.frollosdk.model.testTransactionTagData

class TransactionUserTagsDaoTest {

    @get:Rule
    val testRule = InstantTaskExecutorRule()

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
    fun testLoadAllAndInsert() {
        val data1 = testTransactionTagData("tag1")
        val data2 = testTransactionTagData("tag2")
        val data3 = testTransactionTagData("tag4")
        val data4 = testTransactionTagData("tag3")
        val list = mutableListOf(data1, data2, data3, data4)
        db.userTags().insertAll(list)
        val testObserver =  db.userTags().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testDeleteByNames() {
        val data1 = testTransactionTagData("tag1" )
        val data2 = testTransactionTagData("tag2")
        val data3 = testTransactionTagData("tag3")
        val data4 = testTransactionTagData("tag4")
        val list = mutableListOf(data1, data2, data3, data4)
        db.userTags().insertAll(list)
        db.userTags().deleteByNamesInverse(mutableListOf("tag1","tag2"))

        val testObserver =  db.userTags().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testSearchUserTags() {
        val data1 = testTransactionTagData("tag1" )
        val data2 = testTransactionTagData("tag2")
        val data3 = testTransactionTagData("tag3")
        val data4 = testTransactionTagData("hello")
        val list = mutableListOf(data1, data2, data3, data4)
        db.userTags().insertAll(list)


        val testObserver =  db.userTags().custom(sqlForUserTags("tag",TagsSortType.COUNT,OrderType.DESC)).test()
        testObserver.awaitValue()
        val data = testObserver.value()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
        assertTrue(data.get(0).count!!-data.get(1).count!!>0)
    }

}