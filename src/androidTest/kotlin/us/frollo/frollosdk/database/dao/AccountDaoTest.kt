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
import us.frollo.frollosdk.mapping.toAccount
import us.frollo.frollosdk.mapping.toProvider
import us.frollo.frollosdk.mapping.toProviderAccount
import us.frollo.frollosdk.mapping.toTransaction
import us.frollo.frollosdk.model.testAccountResponseData
import us.frollo.frollosdk.model.testProviderAccountResponseData
import us.frollo.frollosdk.model.testProviderResponseData
import us.frollo.frollosdk.model.testTransactionResponseData

class AccountDaoTest {

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
        val data1 = testAccountResponseData(accountId = 100)
        val data2 = testAccountResponseData(accountId = 101)
        val data3 = testAccountResponseData(accountId = 102)
        val data4 = testAccountResponseData(accountId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.accounts().insertAll(*list.map { it.toAccount() }.toList().toTypedArray())

        val testObserver = db.accounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testLoadByAccountId() {
        val data = testAccountResponseData(accountId = 102)
        val list = mutableListOf(testAccountResponseData(accountId = 101), data, testAccountResponseData(accountId = 103))
        db.accounts().insertAll(*list.map { it.toAccount() }.toList().toTypedArray())

        val testObserver = db.accounts().load(data.accountId).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.accountId, testObserver.value()?.accountId)
    }

    @Test
    fun testLoadByProviderAccountId() {
        val data1 = testAccountResponseData(providerAccountId = 1)
        val data2 = testAccountResponseData(providerAccountId = 2)
        val data3 = testAccountResponseData(providerAccountId = 1)
        val data4 = testAccountResponseData(providerAccountId = 1)
        val list = mutableListOf(data1, data2, data3, data4)
        db.accounts().insertAll(*list.map { it.toAccount() }.toList().toTypedArray())

        val testObserver = db.accounts().loadByProviderAccountId(providerAccountId = 1).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testInsertAll() {
        val data1 = testAccountResponseData(accountId = 100)
        val data2 = testAccountResponseData(accountId = 101)
        val data3 = testAccountResponseData(accountId = 102)
        val data4 = testAccountResponseData(accountId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.accounts().insertAll(*list.map { it.toAccount() }.toList().toTypedArray())

        val testObserver = db.accounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testAccountResponseData()

        db.accounts().insert(data.toAccount())

        val testObserver = db.accounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(data.accountId, testObserver.value()[0].accountId)
    }

    @Test
    fun testGetIdsByProviderAccountIds() {
        val data1 = testAccountResponseData(accountId = 100, providerAccountId = 1)
        val data2 = testAccountResponseData(accountId = 101, providerAccountId = 2)
        val data3 = testAccountResponseData(accountId = 102, providerAccountId = 2)
        val data4 = testAccountResponseData(accountId = 103, providerAccountId = 1)
        val data5 = testAccountResponseData(accountId = 104, providerAccountId = 3)
        val data6 = testAccountResponseData(accountId = 105, providerAccountId = 1)
        val list = mutableListOf(data1, data2, data3, data4, data5, data6)

        db.accounts().insertAll(*list.map { it.toAccount() }.toList().toTypedArray())

        val ids = db.accounts().getIdsByProviderAccountIds(providerAccountIds = longArrayOf(2, 3))
        assertTrue(ids.isNotEmpty())
        assertEquals(3, ids.size)
        assertTrue(ids.toList().containsAll(listOf<Long>(101, 102, 104)))
    }

    @Test
    fun testGetStaleIds() {
        val data1 = testAccountResponseData(accountId = 100)
        val data2 = testAccountResponseData(accountId = 101)
        val data3 = testAccountResponseData(accountId = 102)
        val data4 = testAccountResponseData(accountId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.accounts().insertAll(*list.map { it.toAccount() }.toList().toTypedArray())

        val staleIds = db.accounts().getStaleIds(longArrayOf(100, 103)).sorted()
        assertEquals(2, staleIds.size)
        assertTrue(staleIds.containsAll(mutableListOf<Long>(101, 102)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testAccountResponseData(accountId = 100)
        val data2 = testAccountResponseData(accountId = 101)
        val data3 = testAccountResponseData(accountId = 102)
        val data4 = testAccountResponseData(accountId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.accounts().insertAll(*list.map { it.toAccount() }.toList().toTypedArray())

        db.accounts().deleteMany(longArrayOf(100, 103))

        val testObserver = db.accounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testDelete() {
        val data1 = testAccountResponseData(accountId = 100)
        val data2 = testAccountResponseData(accountId = 101)
        val data3 = testAccountResponseData(accountId = 102)
        val data4 = testAccountResponseData(accountId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.accounts().insertAll(*list.map { it.toAccount() }.toList().toTypedArray())

        db.accounts().delete(100)

        val testObserver = db.accounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testDeleteByProviderAccountId() {
        val data1 = testAccountResponseData(providerAccountId = 1)
        val data2 = testAccountResponseData(providerAccountId = 2)
        val data3 = testAccountResponseData(providerAccountId = 2)
        val data4 = testAccountResponseData(providerAccountId = 1)
        val list = mutableListOf(data1, data2, data3, data4)

        db.accounts().insertAll(*list.map { it.toAccount() }.toList().toTypedArray())

        db.accounts().deleteByProviderAccountId(1)

        val testObserver = db.accounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testAccountResponseData(accountId = 100)
        val data2 = testAccountResponseData(accountId = 101)
        val data3 = testAccountResponseData(accountId = 102)
        val data4 = testAccountResponseData(accountId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.accounts().insertAll(*list.map { it.toAccount() }.toList().toTypedArray())

        db.accounts().clear()

        val testObserver = db.accounts().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }

    @Test
    fun testLoadAllWithRelation() {
        db.providers().insert(testProviderResponseData(providerId = 123).toProvider())
        db.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 234, providerId = 123).toProviderAccount())
        db.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        db.transactions().insert(testTransactionResponseData(transactionId = 456, accountId = 345).toTransaction())
        db.transactions().insert(testTransactionResponseData(transactionId = 457, accountId = 345).toTransaction())

        val testObserver = db.accounts().loadWithRelation().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)

        val model = testObserver.value()[0]

        assertEquals(345L, model.account?.accountId)
        assertEquals(234L, model.providerAccount?.providerAccount?.providerAccountId)
        assertEquals(2, model.transactions?.size)
        assertEquals(456L, model.transactions?.get(0)?.transactionId)
        assertEquals(457L, model.transactions?.get(1)?.transactionId)
    }

    @Test
    fun testLoadByAccountIdWithRelation() {
        db.providers().insert(testProviderResponseData(providerId = 123).toProvider())
        db.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 234, providerId = 123).toProviderAccount())
        db.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        db.transactions().insert(testTransactionResponseData(transactionId = 456, accountId = 345).toTransaction())
        db.transactions().insert(testTransactionResponseData(transactionId = 457, accountId = 345).toTransaction())

        val testObserver = db.accounts().loadWithRelation(accountId = 345).test()
        testObserver.awaitValue()

        val model = testObserver.value()

        assertEquals(345L, model?.account?.accountId)
        assertEquals(234L, model?.providerAccount?.providerAccount?.providerAccountId)
        assertEquals(2, model?.transactions?.size)
        assertEquals(456L, model?.transactions?.get(0)?.transactionId)
        assertEquals(457L, model?.transactions?.get(1)?.transactionId)
    }

    @Test
    fun testLoadByProviderAccountIdWithRelation() {
        db.providers().insert(testProviderResponseData(providerId = 123).toProvider())
        db.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 234, providerId = 123).toProviderAccount())
        db.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        db.transactions().insert(testTransactionResponseData(transactionId = 456, accountId = 345).toTransaction())
        db.transactions().insert(testTransactionResponseData(transactionId = 457, accountId = 345).toTransaction())
        db.accounts().insert(testAccountResponseData(accountId = 346, providerAccountId = 234).toAccount())
        db.transactions().insert(testTransactionResponseData(transactionId = 458, accountId = 346).toTransaction())
        db.transactions().insert(testTransactionResponseData(transactionId = 459, accountId = 346).toTransaction())

        val testObserver = db.accounts().loadWithRelation().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)

        val model1 = testObserver.value()[0]

        assertEquals(345L, model1.account?.accountId)
        assertEquals(234L, model1.providerAccount?.providerAccount?.providerAccountId)
        assertEquals(2, model1.transactions?.size)
        assertEquals(456L, model1.transactions?.get(0)?.transactionId)
        assertEquals(457L, model1.transactions?.get(1)?.transactionId)

        val model2 = testObserver.value()[1]

        assertEquals(346L, model2.account?.accountId)
        assertEquals(234L, model2.providerAccount?.providerAccount?.providerAccountId)
        assertEquals(2, model2.transactions?.size)
        assertEquals(458L, model2.transactions?.get(0)?.transactionId)
        assertEquals(459L, model2.transactions?.get(1)?.transactionId)
    }
}