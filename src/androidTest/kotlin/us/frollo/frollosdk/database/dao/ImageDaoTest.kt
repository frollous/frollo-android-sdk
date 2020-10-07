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
import us.frollo.frollosdk.mapping.toImage
import us.frollo.frollosdk.model.testImageResponseData

class ImageDaoTest {

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
        val data1 = testImageResponseData(imageId = 100)
        val data2 = testImageResponseData(imageId = 101)
        val data3 = testImageResponseData(imageId = 102)
        val data4 = testImageResponseData(imageId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.images().insertAll(*list.map { it.toImage() }.toTypedArray())

        val testObserver = db.images().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testLoadByImageId() {
        val data = testImageResponseData(imageId = 102)
        val list = mutableListOf(testImageResponseData(imageId = 101), data, testImageResponseData(imageId = 103))
        db.images().insertAll(*list.map { it.toImage() }.toList().toTypedArray())

        val testObserver = db.images().load(data.imageId).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.imageId, testObserver.value()?.imageId)
    }

    @Test
    fun testLoadByQuery() {
        val data1 = testImageResponseData(imageId = 100, imageTypes = listOf("goal", "challenge"))
        val data2 = testImageResponseData(imageId = 101, imageTypes = listOf("goal"))
        val data3 = testImageResponseData(imageId = 102, imageTypes = listOf("goal", "challenge"))
        val data4 = testImageResponseData(imageId = 103, imageTypes = listOf("challenge"))
        val list = mutableListOf(data1, data2, data3, data4)

        db.images().insertAll(*list.map { it.toImage() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT * FROM image WHERE image_types LIKE '%|goal|%'")

        val testObserver = db.images().loadByQuery(query).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testInsertAll() {
        val data1 = testImageResponseData(imageId = 100)
        val data2 = testImageResponseData(imageId = 101)
        val data3 = testImageResponseData(imageId = 102)
        val data4 = testImageResponseData(imageId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.images().insertAll(*list.map { it.toImage() }.toList().toTypedArray())

        val testObserver = db.images().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testImageResponseData()

        db.images().insert(data.toImage())

        val testObserver = db.images().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(data.imageId, testObserver.value()[0].imageId)
    }

    @Test
    fun testGetIdsByQuery() {
        val data1 = testImageResponseData(imageId = 100, imageTypes = listOf("goal", "challenge"))
        val data2 = testImageResponseData(imageId = 101, imageTypes = listOf("goal"))
        val data3 = testImageResponseData(imageId = 102, imageTypes = listOf("goal", "challenge"))
        val data4 = testImageResponseData(imageId = 103, imageTypes = listOf("challenge"))
        val list = mutableListOf(data1, data2, data3, data4)

        db.images().insertAll(*list.map { it.toImage() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT image_id FROM image WHERE image_types LIKE '%|challenge|%'")
        val ids = db.images().getIdsByQuery(query).sorted()

        assertEquals(3, ids.size)
        assertTrue(ids.containsAll(mutableListOf(100L, 102L, 103L)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testImageResponseData(imageId = 100)
        val data2 = testImageResponseData(imageId = 101)
        val data3 = testImageResponseData(imageId = 102)
        val data4 = testImageResponseData(imageId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.images().insertAll(*list.map { it.toImage() }.toList().toTypedArray())

        db.images().deleteMany(longArrayOf(100, 103))

        val testObserver = db.images().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testDelete() {
        val data1 = testImageResponseData(imageId = 100)
        val data2 = testImageResponseData(imageId = 101)
        val data3 = testImageResponseData(imageId = 102)
        val data4 = testImageResponseData(imageId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.images().insertAll(*list.map { it.toImage() }.toList().toTypedArray())

        db.images().delete(100)

        val testObserver = db.images().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testImageResponseData(imageId = 100)
        val data2 = testImageResponseData(imageId = 101)
        val data3 = testImageResponseData(imageId = 102)
        val data4 = testImageResponseData(imageId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.images().insertAll(*list.map { it.toImage() }.toList().toTypedArray())

        db.images().clear()

        val testObserver = db.images().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }
}
