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
import us.frollo.frollosdk.mapping.toConsent
import us.frollo.frollosdk.mapping.toProvider
import us.frollo.frollosdk.mapping.toProviderAccount
import us.frollo.frollosdk.model.testConsentResponseData
import us.frollo.frollosdk.model.testProviderAccountResponseData
import us.frollo.frollosdk.model.testProviderResponseData

class ConsentDaoTest {

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
        val data1 = testConsentResponseData(consentId = 100)
        val data2 = testConsentResponseData(consentId = 101)
        val data3 = testConsentResponseData(consentId = 102)
        val data4 = testConsentResponseData(consentId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.consents().insertAll(*list.map { it.toConsent() }.toList().toTypedArray())

        val testObserver = db.consents().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testLoadById() {
        val data = testConsentResponseData(consentId = 102)
        val list = mutableListOf(testConsentResponseData(consentId = 101), data, testConsentResponseData(consentId = 103))
        db.consents().insertAll(*list.map { it.toConsent() }.toList().toTypedArray())

        val testObserver = db.consents().load(data.consentId).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(data.consentId, testObserver.value()?.consentId)
    }

    @Test
    fun testInsertAll() {
        val data1 = testConsentResponseData(consentId = 100)
        val data2 = testConsentResponseData(consentId = 101)
        val data3 = testConsentResponseData(consentId = 102)
        val data4 = testConsentResponseData(consentId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.consents().insertAll(*list.map { it.toConsent() }.toList().toTypedArray())

        val testObserver = db.consents().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(4, testObserver.value().size)
    }

    @Test
    fun testInsert() {
        val data = testConsentResponseData()

        db.consents().insert(data.toConsent())

        val testObserver = db.consents().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(data.consentId, testObserver.value()[0].consentId)
    }

    @Test
    fun testGetStaleIds() {
        val data1 = testConsentResponseData(consentId = 100)
        val data2 = testConsentResponseData(consentId = 101)
        val data3 = testConsentResponseData(consentId = 102)
        val data4 = testConsentResponseData(consentId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.consents().insertAll(*list.map { it.toConsent() }.toList().toTypedArray())

        val staleIds = db.consents().getStaleIds(longArrayOf(100, 103)).sorted()

        assertEquals(2, staleIds.size)
        assertTrue(staleIds.containsAll(mutableListOf<Long>(101, 102)))
    }

    @Test
    fun testDeleteMany() {
        val data1 = testConsentResponseData(consentId = 100)
        val data2 = testConsentResponseData(consentId = 101)
        val data3 = testConsentResponseData(consentId = 102)
        val data4 = testConsentResponseData(consentId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.consents().insertAll(*list.map { it.toConsent() }.toList().toTypedArray())

        db.consents().deleteMany(longArrayOf(100, 103))

        val testObserver = db.consents().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(2, testObserver.value().size)
    }

    @Test
    fun testDelete() {
        val data1 = testConsentResponseData(consentId = 100)
        val data2 = testConsentResponseData(consentId = 101)
        val data3 = testConsentResponseData(consentId = 102)
        val data4 = testConsentResponseData(consentId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.consents().insertAll(*list.map { it.toConsent() }.toList().toTypedArray())

        db.consents().delete(100)

        val testObserver = db.consents().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isNotEmpty())
        assertEquals(3, testObserver.value().size)
    }

    @Test
    fun testClear() {
        val data1 = testConsentResponseData(consentId = 100)
        val data2 = testConsentResponseData(consentId = 101)
        val data3 = testConsentResponseData(consentId = 102)
        val data4 = testConsentResponseData(consentId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        db.consents().insertAll(*list.map { it.toConsent() }.toList().toTypedArray())

        db.consents().clear()

        val testObserver = db.consents().load().test()

        testObserver.awaitValue()
        assertTrue(testObserver.value().isEmpty())
    }

    @Test
    fun testLoadAllWithRelation() {
        db.providers().insert(testProviderResponseData(providerId = 123).toProvider())
        db.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 234, providerId = 123).toProviderAccount())
        db.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 235, providerId = 123).toProviderAccount())
        db.consents().insert(testConsentResponseData(consentId = 345, providerId = 123, providerAccountId = 234).toConsent())
        db.consents().insert(testConsentResponseData(consentId = 346, providerId = 123, providerAccountId = 235).toConsent())

        val testObserver = db.consents().loadWithRelation().test()

        testObserver.awaitValue()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(2, testObserver.value()?.size)

        val model = testObserver.value()?.first()

        assertEquals(123L, model?.provider?.provider?.providerId)
        assertEquals(1, model?.providerAccounts?.size)
        assertEquals(234L, model?.providerAccount?.providerAccount?.providerAccountId)
        assertEquals(345L, model?.consent?.consentId)
    }

    @Test
    fun testLoadByIdWithRelation() {
        db.providers().insert(testProviderResponseData(providerId = 123).toProvider())
        db.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 234, providerId = 123).toProviderAccount())
        db.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 235, providerId = 123).toProviderAccount())
        db.consents().insert(testConsentResponseData(consentId = 345, providerId = 123, providerAccountId = 234).toConsent())
        db.consents().insert(testConsentResponseData(consentId = 346, providerId = 123, providerAccountId = 235).toConsent())

        val testObserver = db.consents().loadWithRelation(consentId = 345).test()
        testObserver.awaitValue()

        val model = testObserver.value()

        assertEquals(123L, model?.provider?.provider?.providerId)
        assertEquals(1, model?.providerAccounts?.size)
        assertEquals(234L, model?.providerAccount?.providerAccount?.providerAccountId)
        assertEquals(345L, model?.consent?.consentId)
    }
}
