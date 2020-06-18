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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.mapping.toAccount
import us.frollo.frollosdk.mapping.toProvider
import us.frollo.frollosdk.mapping.toProviderAccount
import us.frollo.frollosdk.model.testAccountResponseData
import us.frollo.frollosdk.model.testProviderAccountResponseData
import us.frollo.frollosdk.model.testProviderResponseData

class ProviderAccountDaoTest {

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
        val data1 = testProviderAccountResponseData(providerAccountId = 1)
        val data2 = testProviderAccountResponseData(providerAccountId = 2)
        val data3 = testProviderAccountResponseData(providerAccountId = 3)
        val data4 = testProviderAccountResponseData(providerAccountId = 4)
        val list = mutableListOf(data1, data2, data3, data4)

        db.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        val testObserver = db.providerAccounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testLoadByProviderAccountId() {
        val data = testProviderAccountResponseData(providerAccountId = 102)
        val list = mutableListOf(testProviderAccountResponseData(providerAccountId = 101), data, testProviderAccountResponseData(providerAccountId = 103))
        db.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        val testObserver = db.providerAccounts().load(data.providerAccountId).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.providerAccountId, testObserver.value()?.providerAccountId)
    }

    @Test
    fun testLoadByProviderId() {
        val data1 = testProviderAccountResponseData(providerId = 1)
        val data2 = testProviderAccountResponseData(providerId = 2)
        val data3 = testProviderAccountResponseData(providerId = 1)
        val data4 = testProviderAccountResponseData(providerId = 1)
        val list = mutableListOf(data1, data2, data3, data4)
        db.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        val testObserver = db.providerAccounts().loadByProviderId(providerId = 1).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testInsertAll() {
        val data1 = testProviderAccountResponseData(providerAccountId = 1)
        val data2 = testProviderAccountResponseData(providerAccountId = 2)
        val data3 = testProviderAccountResponseData(providerAccountId = 3)
        val data4 = testProviderAccountResponseData(providerAccountId = 4)
        val list = mutableListOf(data1, data2, data3, data4)

        db.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        val testObserver = db.providerAccounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testProviderAccountResponseData()

        db.providerAccounts().insert(data.toProviderAccount())

        val testObserver = db.providerAccounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(data.providerAccountId, testObserver.value()[0].providerAccountId)
    }

    @Test
    fun testGetIdsByProviderIds() {
        val data1 = testProviderAccountResponseData(providerAccountId = 100, providerId = 1)
        val data2 = testProviderAccountResponseData(providerAccountId = 101, providerId = 2)
        val data3 = testProviderAccountResponseData(providerAccountId = 102, providerId = 2)
        val data4 = testProviderAccountResponseData(providerAccountId = 103, providerId = 1)
        val data5 = testProviderAccountResponseData(providerAccountId = 104, providerId = 3)
        val data6 = testProviderAccountResponseData(providerAccountId = 105, providerId = 1)
        val list = mutableListOf(data1, data2, data3, data4, data5, data6)

        db.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        val ids = db.providerAccounts().getIdsByProviderIds(providerIds = longArrayOf(2, 3))
        assertTrue(ids.isNotEmpty())
        assertEquals(3, ids.size)
        assertTrue(ids.toList().containsAll(listOf<Long>(101, 102, 104)))
    }

    @Test
    fun testGetStaleIds() {
        val data1 = testProviderAccountResponseData(providerAccountId = 100)
        val data2 = testProviderAccountResponseData(providerAccountId = 101)
        val data3 = testProviderAccountResponseData(providerAccountId = 102)
        val data4 = testProviderAccountResponseData(providerAccountId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        val staleIds = db.providerAccounts().getStaleIds(longArrayOf(100, 103)).sorted()
        assertEquals(2, staleIds.size)
        assertTrue(staleIds.containsAll(mutableListOf<Long>(101, 102)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testProviderAccountResponseData(providerAccountId = 100)
        val data2 = testProviderAccountResponseData(providerAccountId = 101)
        val data3 = testProviderAccountResponseData(providerAccountId = 102)
        val data4 = testProviderAccountResponseData(providerAccountId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        db.providerAccounts().deleteMany(longArrayOf(100, 103))

        val testObserver = db.providerAccounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testDelete() {
        val data1 = testProviderAccountResponseData(providerAccountId = 100)
        val data2 = testProviderAccountResponseData(providerAccountId = 101)
        val data3 = testProviderAccountResponseData(providerAccountId = 102)
        val data4 = testProviderAccountResponseData(providerAccountId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        db.providerAccounts().delete(100)

        val testObserver = db.providerAccounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testProviderAccountResponseData(providerAccountId = 100)
        val data2 = testProviderAccountResponseData(providerAccountId = 101)
        val data3 = testProviderAccountResponseData(providerAccountId = 102)
        val data4 = testProviderAccountResponseData(providerAccountId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        db.providerAccounts().clear()

        val testObserver = db.providerAccounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }

    @Test
    fun testLoadAllWithRelation() {
        db.providers().insert(testProviderResponseData(providerId = 123).toProvider())
        db.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 234, providerId = 123).toProviderAccount())
        db.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        db.accounts().insert(testAccountResponseData(accountId = 346, providerAccountId = 234).toAccount())

        val testObserver = db.providerAccounts().loadWithRelation().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)

        val model = testObserver.value()[0]

        assertEquals(123L, model.provider?.providerId)
        assertEquals(234L, model.providerAccount?.providerAccountId)
        assertEquals(2, model.accounts?.size)
        assertEquals(345L, model.accounts?.get(0)?.accountId)
        assertEquals(346L, model.accounts?.get(1)?.accountId)
    }

    @Test
    fun testLoadByProviderAccountIdWithRelation() {
        db.providers().insert(testProviderResponseData(providerId = 123).toProvider())
        db.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 234, providerId = 123).toProviderAccount())
        db.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        db.accounts().insert(testAccountResponseData(accountId = 346, providerAccountId = 234).toAccount())

        val testObserver = db.providerAccounts().loadWithRelation(providerAccountId = 234).test()
        testObserver.awaitValue()

        val model = testObserver.value()

        assertEquals(123L, model?.provider?.providerId)
        assertEquals(234L, model?.providerAccount?.providerAccountId)
        assertEquals(2, model?.accounts?.size)
        assertEquals(345L, model?.accounts?.get(0)?.accountId)
        assertEquals(346L, model?.accounts?.get(1)?.accountId)
    }

    @Test
    fun testLoadByProviderIdWithRelation() {
        db.providers().insert(testProviderResponseData(providerId = 123).toProvider())
        db.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 234, providerId = 123).toProviderAccount())
        db.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        db.accounts().insert(testAccountResponseData(accountId = 346, providerAccountId = 234).toAccount())
        db.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 235, providerId = 123).toProviderAccount())
        db.accounts().insert(testAccountResponseData(accountId = 347, providerAccountId = 235).toAccount())
        db.accounts().insert(testAccountResponseData(accountId = 348, providerAccountId = 235).toAccount())

        val testObserver = db.providerAccounts().loadByProviderIdWithRelation(providerId = 123).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)

        val model1 = testObserver.value()[0]

        assertEquals(123L, model1.provider?.providerId)
        assertEquals(234L, model1.providerAccount?.providerAccountId)
        assertEquals(2, model1.accounts?.size)
        assertEquals(345L, model1.accounts?.get(0)?.accountId)
        assertEquals(346L, model1.accounts?.get(1)?.accountId)

        val model2 = testObserver.value()[1]

        assertEquals(123L, model2.provider?.providerId)
        assertEquals(235L, model2.providerAccount?.providerAccountId)
        assertEquals(2, model2.accounts?.size)
        assertEquals(347L, model2.accounts?.get(0)?.accountId)
        assertEquals(348L, model2.accounts?.get(1)?.accountId)
    }
}
