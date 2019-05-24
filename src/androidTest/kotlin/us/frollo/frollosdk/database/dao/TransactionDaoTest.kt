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
import us.frollo.frollosdk.extensions.sqlForTransactionStaleIds
import us.frollo.frollosdk.mapping.toAccount
import us.frollo.frollosdk.mapping.toMerchant
import us.frollo.frollosdk.mapping.toProvider
import us.frollo.frollosdk.mapping.toProviderAccount
import us.frollo.frollosdk.mapping.toTransaction
import us.frollo.frollosdk.mapping.toTransactionCategory
import us.frollo.frollosdk.model.testAccountResponseData
import us.frollo.frollosdk.model.testMerchantResponseData
import us.frollo.frollosdk.model.testProviderAccountResponseData
import us.frollo.frollosdk.model.testProviderResponseData
import us.frollo.frollosdk.model.testTransactionCategoryResponseData
import us.frollo.frollosdk.model.testTransactionResponseData

class TransactionDaoTest {

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
    fun testLoadAll() {
        val data1 = testTransactionResponseData(transactionId = 100)
        val data2 = testTransactionResponseData(transactionId = 101)
        val data3 = testTransactionResponseData(transactionId = 102)
        val data4 = testTransactionResponseData(transactionId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val testObserver = db.transactions().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testLoadByTransactionId() {
        val data = testTransactionResponseData(transactionId = 102)
        val list = mutableListOf(testTransactionResponseData(transactionId = 101), data, testTransactionResponseData(transactionId = 103))
        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val testObserver = db.transactions().load(data.transactionId).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.transactionId, testObserver.value()?.transactionId)
    }

    @Test
    fun testLoadByTransactionIds() {
        val data1 = testTransactionResponseData(transactionId = 100)
        val data2 = testTransactionResponseData(transactionId = 101)
        val data3 = testTransactionResponseData(transactionId = 102)
        val data4 = testTransactionResponseData(transactionId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val testObserver = db.transactions().load(longArrayOf(100, 101)).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testLoadByAccountId() {
        val data1 = testTransactionResponseData(accountId = 1)
        val data2 = testTransactionResponseData(accountId = 1)
        val data3 = testTransactionResponseData(accountId = 2)
        val data4 = testTransactionResponseData(accountId = 1)
        val list = mutableListOf(data1, data2, data3, data4)
        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val testObserver = db.transactions().loadByAccountId(accountId = 1).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testLoadTransaction() {
        val data1 = testTransactionResponseData(transactionId = 100)
        val data2 = testTransactionResponseData(transactionId = 101)
        val list = mutableListOf(data1, data2)
        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val model = db.transactions().loadTransaction(transactionId = 101)
        assertNotNull(model)
        assertEquals(101L, model?.transactionId)
    }

    @Test
    fun testLoadByQuery() {
        val data1 = testTransactionResponseData(transactionId = 100)
        val data2 = testTransactionResponseData(transactionId = 101)
        val data3 = testTransactionResponseData(transactionId = 102)
        val data4 = testTransactionResponseData(transactionId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT * FROM transaction_model WHERE transaction_id IN (101,102,103)")

        val testObserver = db.transactions().loadByQuery(query).test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testLoadIdsByQuery() {
        val data1 = testTransactionResponseData(transactionId = 100, accountId = 1, transactionDate = "2019-01-04", included = false)
        val data2 = testTransactionResponseData(transactionId = 101, accountId = 1, transactionDate = "2019-01-20", included = false)
        val data3 = testTransactionResponseData(transactionId = 102, accountId = 1, transactionDate = "2018-12-31", included = false)
        val data4 = testTransactionResponseData(transactionId = 103, accountId = 2, transactionDate = "2019-01-20", included = false)
        val data5 = testTransactionResponseData(transactionId = 104, accountId = 1, transactionDate = "2019-02-03", included = false)
        val data6 = testTransactionResponseData(transactionId = 105, accountId = 1, transactionDate = "2019-01-02", included = false)
        val data7 = testTransactionResponseData(transactionId = 106, accountId = 1, transactionDate = "2019-02-04", included = false)
        val list = mutableListOf(data1, data2, data3, data4, data5, data6, data7)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val query = sqlForTransactionStaleIds(fromDate = "2019-01-03", toDate = "2019-02-03", accountIds = longArrayOf(1), transactionIncluded = false)

        val ids = db.transactions().getIdsQuery(query)

        assertTrue(ids.isNotEmpty())
        assertEquals(3, ids.size)
    }

    @Test
    fun testInsertAll() {
        val data1 = testTransactionResponseData(transactionId = 100)
        val data2 = testTransactionResponseData(transactionId = 101)
        val data3 = testTransactionResponseData(transactionId = 102)
        val data4 = testTransactionResponseData(transactionId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val testObserver = db.transactions().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testTransactionResponseData()

        db.transactions().insert(data.toTransaction())

        val testObserver = db.transactions().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(data.transactionId, testObserver.value()[0].transactionId)
    }

    @Test
    fun testGetIdsByAccountIds() {
        val data1 = testTransactionResponseData(transactionId = 100, accountId = 1)
        val data2 = testTransactionResponseData(transactionId = 101, accountId = 2)
        val data3 = testTransactionResponseData(transactionId = 102, accountId = 2)
        val data4 = testTransactionResponseData(transactionId = 103, accountId = 1)
        val data5 = testTransactionResponseData(transactionId = 104, accountId = 3)
        val data6 = testTransactionResponseData(transactionId = 105, accountId = 1)
        val list = mutableListOf(data1, data2, data3, data4, data5, data6)
        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val ids = db.transactions().getIdsByAccountIds(longArrayOf(2, 3))
        assertTrue(ids.isNotEmpty())
        assertEquals(3, ids.size)
        assertTrue(ids.toList().containsAll(listOf<Long>(101, 102, 104)))
    }

    @Test
    fun testGetStaleIds() {
        val data1 = testTransactionResponseData(transactionId = 100)
        val data2 = testTransactionResponseData(transactionId = 101)
        val data3 = testTransactionResponseData(transactionId = 102)
        val data4 = testTransactionResponseData(transactionId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val staleIds = db.transactions().getStaleIds(longArrayOf(100, 103)).sorted()
        assertEquals(2, staleIds.size)
        assertTrue(staleIds.containsAll(mutableListOf<Long>(101, 102)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testTransactionResponseData(transactionId = 100)
        val data2 = testTransactionResponseData(transactionId = 101)
        val data3 = testTransactionResponseData(transactionId = 102)
        val data4 = testTransactionResponseData(transactionId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        db.transactions().deleteMany(longArrayOf(100, 103))

        val testObserver = db.transactions().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testDelete() {
        val data1 = testTransactionResponseData(transactionId = 100)
        val data2 = testTransactionResponseData(transactionId = 101)
        val data3 = testTransactionResponseData(transactionId = 102)
        val data4 = testTransactionResponseData(transactionId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        db.transactions().delete(100)

        val testObserver = db.transactions().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testDeleteByAccountIds() {
        val data1 = testTransactionResponseData(accountId = 1)
        val data2 = testTransactionResponseData(accountId = 2)
        val data3 = testTransactionResponseData(accountId = 3)
        val data4 = testTransactionResponseData(accountId = 3)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        db.transactions().deleteByAccountIds(longArrayOf(1, 3))

        val testObserver = db.transactions().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)
    }

    @Test
    fun testDeleteByAccountId() {
        val data1 = testTransactionResponseData(accountId = 1)
        val data2 = testTransactionResponseData(accountId = 2)
        val data3 = testTransactionResponseData(accountId = 2)
        val data4 = testTransactionResponseData(accountId = 1)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        db.transactions().deleteByAccountId(1)

        val testObserver = db.transactions().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testTransactionResponseData(transactionId = 100)
        val data2 = testTransactionResponseData(transactionId = 101)
        val data3 = testTransactionResponseData(transactionId = 102)
        val data4 = testTransactionResponseData(transactionId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        db.transactions().clear()

        val testObserver = db.transactions().load().test()
        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }

    @Test
    fun testLoadAllWithRelation() {
        db.transactions().insert(testTransactionResponseData(transactionId = 123, accountId = 234, categoryId = 567, merchantId = 678).toTransaction())
        db.accounts().insert(testAccountResponseData(accountId = 234, providerAccountId = 345).toAccount())
        db.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 345, providerId = 456).toProviderAccount())
        db.providers().insert(testProviderResponseData(providerId = 456).toProvider())
        db.transactionCategories().insert(testTransactionCategoryResponseData(transactionCategoryId = 567).toTransactionCategory())
        db.merchants().insert(testMerchantResponseData(merchantId = 678).toMerchant())

        val testObserver = db.transactions().loadWithRelation().test()
        testObserver.awaitValue()

        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(1, testObserver.value().size)

        val model = testObserver.value()[0]

        assertEquals(123L, model.transaction?.transactionId)
        assertEquals(678L, model.merchant?.merchantId)
        assertEquals(567L, model.transactionCategory?.transactionCategoryId)
        assertEquals(234L, model.account?.account?.accountId)
    }

    @Test
    fun testLoadByTransactionIdWithRelation() {
        db.transactions().insert(testTransactionResponseData(transactionId = 123, accountId = 234, categoryId = 567, merchantId = 678).toTransaction())
        db.accounts().insert(testAccountResponseData(accountId = 234, providerAccountId = 345).toAccount())
        db.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 345, providerId = 456).toProviderAccount())
        db.providers().insert(testProviderResponseData(providerId = 456).toProvider())
        db.transactionCategories().insert(testTransactionCategoryResponseData(transactionCategoryId = 567).toTransactionCategory())
        db.merchants().insert(testMerchantResponseData(merchantId = 678).toMerchant())

        val testObserver = db.transactions().loadWithRelation(transactionId = 123).test()
        testObserver.awaitValue()

        val model = testObserver.value()

        assertEquals(123L, model?.transaction?.transactionId)
        assertEquals(678L, model?.merchant?.merchantId)
        assertEquals(567L, model?.transactionCategory?.transactionCategoryId)
        assertEquals(234L, model?.account?.account?.accountId)
    }

    @Test
    fun testLoadByTransactionIdsWithRelation() {
        db.transactions().insert(testTransactionResponseData(transactionId = 122, accountId = 234, categoryId = 567, merchantId = 678).toTransaction())
        db.transactions().insert(testTransactionResponseData(transactionId = 123, accountId = 234, categoryId = 567, merchantId = 678).toTransaction())
        db.accounts().insert(testAccountResponseData(accountId = 234, providerAccountId = 345).toAccount())
        db.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 345, providerId = 456).toProviderAccount())
        db.providers().insert(testProviderResponseData(providerId = 456).toProvider())
        db.transactionCategories().insert(testTransactionCategoryResponseData(transactionCategoryId = 567).toTransactionCategory())
        db.merchants().insert(testMerchantResponseData(merchantId = 678).toMerchant())

        val testObserver = db.transactions().loadWithRelation(transactionIds = longArrayOf(122, 123)).test()
        testObserver.awaitValue()

        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)

        val model1 = testObserver.value()[0]

        assertEquals(122L, model1.transaction?.transactionId)
        assertEquals(678L, model1.merchant?.merchantId)
        assertEquals(567L, model1.transactionCategory?.transactionCategoryId)
        assertEquals(234L, model1.account?.account?.accountId)

        val model2 = testObserver.value()[1]

        assertEquals(123L, model2.transaction?.transactionId)
        assertEquals(678L, model2.merchant?.merchantId)
        assertEquals(567L, model2.transactionCategory?.transactionCategoryId)
        assertEquals(234L, model2.account?.account?.accountId)
    }

    @Test
    fun testLoadByAccountIdWithRelation() {
        db.transactions().insert(testTransactionResponseData(transactionId = 122, accountId = 234, categoryId = 567, merchantId = 678).toTransaction())
        db.transactions().insert(testTransactionResponseData(transactionId = 123, accountId = 234, categoryId = 567, merchantId = 678).toTransaction())
        db.accounts().insert(testAccountResponseData(accountId = 234, providerAccountId = 345).toAccount())
        db.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 345, providerId = 456).toProviderAccount())
        db.providers().insert(testProviderResponseData(providerId = 456).toProvider())
        db.transactionCategories().insert(testTransactionCategoryResponseData(transactionCategoryId = 567).toTransactionCategory())
        db.merchants().insert(testMerchantResponseData(merchantId = 678).toMerchant())

        val testObserver = db.transactions().loadByAccountIdWithRelation(accountId = 234).test()
        testObserver.awaitValue()

        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)

        val model1 = testObserver.value()[0]

        assertEquals(122L, model1.transaction?.transactionId)
        assertEquals(678L, model1.merchant?.merchantId)
        assertEquals(567L, model1.transactionCategory?.transactionCategoryId)
        assertEquals(234L, model1.account?.account?.accountId)

        val model2 = testObserver.value()[1]

        assertEquals(123L, model2.transaction?.transactionId)
        assertEquals(678L, model2.merchant?.merchantId)
        assertEquals(567L, model2.transactionCategory?.transactionCategoryId)
        assertEquals(234L, model2.account?.account?.accountId)
    }

    @Test
    fun testLoadByQueryWithRelation() {
        db.transactions().insert(testTransactionResponseData(transactionId = 122, accountId = 234, categoryId = 567, merchantId = 678).toTransaction())
        db.transactions().insert(testTransactionResponseData(transactionId = 123, accountId = 234, categoryId = 567, merchantId = 678).toTransaction())
        db.transactions().insert(testTransactionResponseData(transactionId = 124, accountId = 235, categoryId = 567, merchantId = 678).toTransaction())
        db.transactions().insert(testTransactionResponseData(transactionId = 125, accountId = 235, categoryId = 567, merchantId = 678).toTransaction())
        db.accounts().insert(testAccountResponseData(accountId = 234, providerAccountId = 345).toAccount())
        db.accounts().insert(testAccountResponseData(accountId = 235, providerAccountId = 345).toAccount())
        db.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 345, providerId = 456).toProviderAccount())
        db.providers().insert(testProviderResponseData(providerId = 456).toProvider())
        db.transactionCategories().insert(testTransactionCategoryResponseData(transactionCategoryId = 567).toTransactionCategory())
        db.merchants().insert(testMerchantResponseData(merchantId = 678).toMerchant())

        val query = SimpleSQLiteQuery("SELECT * FROM transaction_model WHERE account_id = 235")

        val testObserver = db.transactions().loadByQueryWithRelation(query).test()
        testObserver.awaitValue()

        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)

        val model1 = testObserver.value()[0]

        assertEquals(124L, model1.transaction?.transactionId)
        assertEquals(678L, model1.merchant?.merchantId)
        assertEquals(567L, model1.transactionCategory?.transactionCategoryId)
        assertEquals(235L, model1.account?.account?.accountId)

        val model2 = testObserver.value()[1]

        assertEquals(125L, model2.transaction?.transactionId)
        assertEquals(678L, model2.merchant?.merchantId)
        assertEquals(567L, model2.transactionCategory?.transactionCategoryId)
        assertEquals(235L, model2.account?.account?.accountId)
    }
}