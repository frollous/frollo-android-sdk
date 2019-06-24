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

package us.frollo.frollosdk.aggregation

import androidx.sqlite.db.SimpleSQLiteQuery
import com.jraska.livedata.test
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.BaseAndroidTest
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.TagApplyAllPair
import us.frollo.frollosdk.network.api.AggregationAPI
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.mapping.toAccount
import us.frollo.frollosdk.mapping.toMerchant
import us.frollo.frollosdk.mapping.toProvider
import us.frollo.frollosdk.mapping.toProviderAccount
import us.frollo.frollosdk.mapping.toTransaction
import us.frollo.frollosdk.mapping.toTransactionCategory
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountSubType
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountType
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshStatus
import us.frollo.frollosdk.model.coredata.aggregation.tags.TagsSortType
import us.frollo.frollosdk.model.coredata.shared.OrderType
import us.frollo.frollosdk.model.loginFormFilledData
import us.frollo.frollosdk.model.testAccountResponseData
import us.frollo.frollosdk.model.testMerchantResponseData
import us.frollo.frollosdk.model.testProviderAccountResponseData
import us.frollo.frollosdk.model.testProviderResponseData
import us.frollo.frollosdk.model.testTransactionCategoryResponseData
import us.frollo.frollosdk.model.testTransactionResponseData
import us.frollo.frollosdk.model.testTransactionTagData
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.randomBoolean
import us.frollo.frollosdk.testutils.randomUUID
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import us.frollo.frollosdk.testutils.wait

class AggregationTest : BaseAndroidTest() {

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
    }

    // Provider Tests

    @Test
    fun testFetchProviderByID() {
        initSetup()

        val data = testProviderResponseData()
        val list = mutableListOf(testProviderResponseData(), data, testProviderResponseData())
        database.providers().insertAll(*list.map { it.toProvider() }.toList().toTypedArray())

        val testObserver = aggregation.fetchProvider(data.providerId).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(data.providerId, testObserver.value().data?.providerId)

        tearDown()
    }

    @Test
    fun testFetchProviders() {
        initSetup()

        val data1 = testProviderResponseData()
        val data2 = testProviderResponseData()
        val data3 = testProviderResponseData()
        val data4 = testProviderResponseData()
        val list = mutableListOf(data1, data2, data3, data4)

        database.providers().insertAll(*list.map { it.toProvider() }.toList().toTypedArray())

        val testObserver = aggregation.fetchProviders().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(4, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchProviderByIDWithRelation() {
        initSetup()

        database.providers().insert(testProviderResponseData(providerId = 123).toProvider())
        database.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 234, providerId = 123).toProviderAccount())
        database.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 235, providerId = 123).toProviderAccount())

        val testObserver = aggregation.fetchProviderWithRelation(providerId = 123).test()
        testObserver.awaitValue()

        val model = testObserver.value().data

        assertEquals(123L, model?.provider?.providerId)
        assertEquals(2, model?.providerAccounts?.size)
        assertEquals(234L, model?.providerAccounts?.get(0)?.providerAccountId)
        assertEquals(235L, model?.providerAccounts?.get(1)?.providerAccountId)

        val data = testProviderResponseData()
        val list = mutableListOf(testProviderResponseData(), data, testProviderResponseData())
        database.providers().insertAll(*list.map { it.toProvider() }.toList().toTypedArray())

        tearDown()
    }

    @Test
    fun testFetchProvidersWithRelation() {
        initSetup()

        database.providers().insert(testProviderResponseData(providerId = 123).toProvider())
        database.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 234, providerId = 123).toProviderAccount())
        database.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 235, providerId = 123).toProviderAccount())

        val testObserver = aggregation.fetchProvidersWithRelation().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(1, testObserver.value().data?.size)

        val model = testObserver.value().data?.get(0)

        assertEquals(123L, model?.provider?.providerId)
        assertEquals(2, model?.providerAccounts?.size)
        assertEquals(234L, model?.providerAccounts?.get(0)?.providerAccountId)
        assertEquals(235L, model?.providerAccounts?.get(1)?.providerAccountId)

        tearDown()
    }

    @Test
    fun testRefreshProviders() {
        initSetup()

        val body = readStringFromJson(app, R.raw.providers_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == AggregationAPI.URL_PROVIDERS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshProviders { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchProviders().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(311, models?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals(AggregationAPI.URL_PROVIDERS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshProvidersFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.refreshProviders { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshProviderByID() {
        initSetup()

        val body = readStringFromJson(app, R.raw.provider_id_12345)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "aggregation/providers/12345") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshProvider(12345L) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchProvider(12345L).test()
            testObserver.awaitValue()
            val model = testObserver.value().data
            assertNotNull(model)
            assertEquals(12345L, model?.providerId)
        }

        val request = mockServer.takeRequest()
        assertEquals("aggregation/providers/12345", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshProviderByIdFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.refreshProvider(12345L) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    // Provider Account Tests

    @Test
    fun testFetchProviderAccountByID() {
        initSetup()

        val data = testProviderAccountResponseData()
        val list = mutableListOf(testProviderAccountResponseData(), data, testProviderAccountResponseData())
        database.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        val testObserver = aggregation.fetchProviderAccount(data.providerAccountId).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(data.providerAccountId, testObserver.value().data?.providerAccountId)

        tearDown()
    }

    @Test
    fun testFetchProviderAccounts() {
        initSetup()

        val data1 = testProviderAccountResponseData(accountRefreshStatus = AccountRefreshStatus.NEEDS_ACTION)
        val data2 = testProviderAccountResponseData(accountRefreshStatus = AccountRefreshStatus.FAILED)
        val data3 = testProviderAccountResponseData(accountRefreshStatus = AccountRefreshStatus.ADDING)
        val data4 = testProviderAccountResponseData(accountRefreshStatus = AccountRefreshStatus.NEEDS_ACTION)
        val list = mutableListOf(data1, data2, data3, data4)

        database.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        val testObserver = aggregation.fetchProviderAccounts(refreshStatus = AccountRefreshStatus.NEEDS_ACTION).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchProviderAccountsByProviderId() {
        initSetup()

        val data1 = testProviderAccountResponseData(providerId = 1)
        val data2 = testProviderAccountResponseData(providerId = 2)
        val data3 = testProviderAccountResponseData(providerId = 1)
        val data4 = testProviderAccountResponseData(providerId = 1)
        val list = mutableListOf(data1, data2, data3, data4)

        database.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        val testObserver = aggregation.fetchProviderAccounts(providerId = 1).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(3, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchProviderAccountByIDWithRelation() {
        initSetup()

        database.providers().insert(testProviderResponseData(providerId = 123).toProvider())
        database.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 234, providerId = 123).toProviderAccount())
        database.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        database.accounts().insert(testAccountResponseData(accountId = 346, providerAccountId = 234).toAccount())

        val testObserver = aggregation.fetchProviderAccountWithRelation(providerAccountId = 234).test()
        testObserver.awaitValue()

        val model = testObserver.value().data

        assertEquals(123L, model?.provider?.providerId)
        assertEquals(234L, model?.providerAccount?.providerAccountId)
        assertEquals(2, model?.accounts?.size)
        assertEquals(345L, model?.accounts?.get(0)?.accountId)
        assertEquals(346L, model?.accounts?.get(1)?.accountId)

        tearDown()
    }

    @Test
    fun testFetchProviderAccountsWithRelation() {
        initSetup()

        database.providers().insert(testProviderResponseData(providerId = 123).toProvider())
        database.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 234, providerId = 123).toProviderAccount())
        database.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        database.accounts().insert(testAccountResponseData(accountId = 346, providerAccountId = 234).toAccount())

        val testObserver = aggregation.fetchProviderAccountsWithRelation().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(1, testObserver.value().data?.size)

        val model = testObserver.value().data?.get(0)

        assertEquals(123L, model?.provider?.providerId)
        assertEquals(234L, model?.providerAccount?.providerAccountId)
        assertEquals(2, model?.accounts?.size)
        assertEquals(345L, model?.accounts?.get(0)?.accountId)
        assertEquals(346L, model?.accounts?.get(1)?.accountId)

        tearDown()
    }

    @Test
    fun testFetchProviderAccountsByProviderIdWithRelation() {
        initSetup()

        database.providers().insert(testProviderResponseData(providerId = 123).toProvider())
        database.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 234, providerId = 123).toProviderAccount())
        database.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        database.accounts().insert(testAccountResponseData(accountId = 346, providerAccountId = 234).toAccount())
        database.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 235, providerId = 123).toProviderAccount())
        database.accounts().insert(testAccountResponseData(accountId = 347, providerAccountId = 235).toAccount())
        database.accounts().insert(testAccountResponseData(accountId = 348, providerAccountId = 235).toAccount())

        val testObserver = aggregation.fetchProviderAccountsWithRelation(providerId = 123).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        val model1 = testObserver.value().data?.get(0)

        assertEquals(123L, model1?.provider?.providerId)
        assertEquals(234L, model1?.providerAccount?.providerAccountId)
        assertEquals(2, model1?.accounts?.size)
        assertEquals(345L, model1?.accounts?.get(0)?.accountId)
        assertEquals(346L, model1?.accounts?.get(1)?.accountId)

        val model2 = testObserver.value().data?.get(1)

        assertEquals(123L, model2?.provider?.providerId)
        assertEquals(235L, model2?.providerAccount?.providerAccountId)
        assertEquals(2, model2?.accounts?.size)
        assertEquals(347L, model2?.accounts?.get(0)?.accountId)
        assertEquals(348L, model2?.accounts?.get(1)?.accountId)

        tearDown()
    }

    @Test
    fun testRefreshProviderAccounts() {
        initSetup()

        val body = readStringFromJson(app, R.raw.provider_accounts_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == AggregationAPI.URL_PROVIDER_ACCOUNTS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshProviderAccounts { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchProviderAccounts().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(4, models?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals(AggregationAPI.URL_PROVIDER_ACCOUNTS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshProviderAccountsByIdFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.refreshProviderAccounts { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshProviderAccountByID() {
        initSetup()

        val body = readStringFromJson(app, R.raw.provider_account_id_123)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "aggregation/provideraccounts/123") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshProviderAccount(123L) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchProviderAccount(123L).test()
            testObserver.awaitValue()
            val model = testObserver.value().data
            assertNotNull(model)
            assertEquals(123L, model?.providerAccountId)
        }

        val request = mockServer.takeRequest()
        assertEquals("aggregation/provideraccounts/123", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshProviderAccountByIdFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.refreshProviderAccount(123L) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testCreateProviderAccount() {
        initSetup()

        val body = readStringFromJson(app, R.raw.provider_account_id_123)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == AggregationAPI.URL_PROVIDER_ACCOUNTS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.createProviderAccount(providerId = 4078, loginForm = loginFormFilledData()) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)
            assertEquals(123L, resource.data)

            val testObserver = aggregation.fetchProviderAccounts().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(1, models?.size)
            assertEquals(123L, models?.get(0)?.providerAccountId)
        }

        val request = mockServer.takeRequest()
        assertEquals(AggregationAPI.URL_PROVIDER_ACCOUNTS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testCreateProviderAccountFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.createProviderAccount(providerId = 4078, loginForm = loginFormFilledData()) { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (resource.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testDeleteProviderAccount() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "aggregation/provideraccounts/12345") {
                    return MockResponse()
                            .setResponseCode(204)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val data = testProviderAccountResponseData(providerAccountId = 12345)
        database.providerAccounts().insert(data.toProviderAccount())

        aggregation.deleteProviderAccount(12345) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            wait(1)

            val testObserver = aggregation.fetchProviderAccount(12345).test()
            testObserver.awaitValue()
            val model = testObserver.value().data
            assertNull(model)
        }

        val request = mockServer.takeRequest()
        assertEquals("aggregation/provideraccounts/12345", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testDeleteProviderAccountFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.deleteProviderAccount(12345) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateProviderAccount() {
        initSetup()

        val body = readStringFromJson(app, R.raw.provider_account_id_123)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "aggregation/provideraccounts/123") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.updateProviderAccount(loginForm = loginFormFilledData(), providerAccountId = 123) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchProviderAccounts().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(1, models?.size)
            assertEquals(123L, models?.get(0)?.providerAccountId)
        }

        val request = mockServer.takeRequest()
        assertEquals("aggregation/provideraccounts/123", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateProviderAccountFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.updateProviderAccount(loginForm = loginFormFilledData(), providerAccountId = 123) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testProviderAccountsFetchMissingProviders() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == AggregationAPI.URL_PROVIDER_ACCOUNTS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.provider_accounts_valid))
                } else if (request?.trimmedPath == "aggregation/providers/12345") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.provider_id_12345))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshProviderAccounts { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchProviderAccounts().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(4, models?.size)
        }

        wait(3)

        val testObserver2 = aggregation.fetchProviders().test()
        testObserver2.awaitValue()
        val models2 = testObserver2.value().data
        assertNotNull(models2)
        assertEquals(1, models2?.size)
        assertEquals(12345L, models2?.get(0)?.providerId)

        tearDown()
    }

    // Account Tests

    @Test
    fun testFetchAccountByID() {
        initSetup()

        val data = testAccountResponseData()
        val list = mutableListOf(testAccountResponseData(), data, testAccountResponseData())
        database.accounts().insertAll(*list.map { it.toAccount() }.toList().toTypedArray())

        val testObserver = aggregation.fetchAccount(data.accountId).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(data.accountId, testObserver.value().data?.accountId)

        tearDown()
    }

    @Test
    fun testFetchAccounts() {
        initSetup()

        val data1 = testAccountResponseData(accountType = AccountType.BANK, accountRefreshStatus = AccountRefreshStatus.NEEDS_ACTION)
        val data2 = testAccountResponseData(accountType = AccountType.BANK, accountRefreshStatus = AccountRefreshStatus.FAILED)
        val data3 = testAccountResponseData(accountType = AccountType.CREDIT_CARD, accountRefreshStatus = AccountRefreshStatus.NEEDS_ACTION)
        val data4 = testAccountResponseData(accountType = AccountType.BANK, accountRefreshStatus = AccountRefreshStatus.NEEDS_ACTION)
        val list = mutableListOf(data1, data2, data3, data4)

        database.accounts().insertAll(*list.map { it.toAccount() }.toList().toTypedArray())

        val testObserver = aggregation.fetchAccounts(accountType = AccountType.BANK, refreshStatus = AccountRefreshStatus.NEEDS_ACTION).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchAccountsByProviderAccountId() {
        initSetup()

        val data1 = testAccountResponseData(providerAccountId = 1)
        val data2 = testAccountResponseData(providerAccountId = 2)
        val data3 = testAccountResponseData(providerAccountId = 1)
        val data4 = testAccountResponseData(providerAccountId = 1)
        val list = mutableListOf(data1, data2, data3, data4)

        database.accounts().insertAll(*list.map { it.toAccount() }.toList().toTypedArray())

        val testObserver = aggregation.fetchAccounts(providerAccountId = 1).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(3, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchAccountByIDWithRelation() {
        initSetup()

        database.providers().insert(testProviderResponseData(providerId = 123).toProvider())
        database.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 234, providerId = 123).toProviderAccount())
        database.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        database.transactions().insert(testTransactionResponseData(transactionId = 456, accountId = 345).toTransaction())
        database.transactions().insert(testTransactionResponseData(transactionId = 457, accountId = 345).toTransaction())

        val testObserver = aggregation.fetchAccountWithRelation(accountId = 345).test()
        testObserver.awaitValue()

        val model = testObserver.value().data

        assertEquals(345L, model?.account?.accountId)
        assertEquals(234L, model?.providerAccount?.providerAccount?.providerAccountId)
        assertEquals(2, model?.transactions?.size)
        assertEquals(456L, model?.transactions?.get(0)?.transactionId)
        assertEquals(457L, model?.transactions?.get(1)?.transactionId)

        tearDown()
    }

    @Test
    fun testFetchAccountsWithRelation() {
        initSetup()

        database.providers().insert(testProviderResponseData(providerId = 123).toProvider())
        database.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 234, providerId = 123).toProviderAccount())
        database.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        database.transactions().insert(testTransactionResponseData(transactionId = 456, accountId = 345).toTransaction())
        database.transactions().insert(testTransactionResponseData(transactionId = 457, accountId = 345).toTransaction())

        val testObserver = aggregation.fetchAccountsWithRelation().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(1, testObserver.value().data?.size)

        val model = testObserver.value().data?.get(0)

        assertEquals(345L, model?.account?.accountId)
        assertEquals(234L, model?.providerAccount?.providerAccount?.providerAccountId)
        assertEquals(2, model?.transactions?.size)
        assertEquals(456L, model?.transactions?.get(0)?.transactionId)
        assertEquals(457L, model?.transactions?.get(1)?.transactionId)

        tearDown()
    }

    @Test
    fun testFetchAccountsByProviderAccountIdWithRelation() {
        initSetup()

        database.providers().insert(testProviderResponseData(providerId = 123).toProvider())
        database.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 234, providerId = 123).toProviderAccount())
        database.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        database.transactions().insert(testTransactionResponseData(transactionId = 456, accountId = 345).toTransaction())
        database.transactions().insert(testTransactionResponseData(transactionId = 457, accountId = 345).toTransaction())
        database.accounts().insert(testAccountResponseData(accountId = 346, providerAccountId = 234).toAccount())
        database.transactions().insert(testTransactionResponseData(transactionId = 458, accountId = 346).toTransaction())
        database.transactions().insert(testTransactionResponseData(transactionId = 459, accountId = 346).toTransaction())

        val testObserver = aggregation.fetchAccountsWithRelation(providerAccountId = 234).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        val model1 = testObserver.value().data?.get(0)

        assertEquals(345L, model1?.account?.accountId)
        assertEquals(234L, model1?.providerAccount?.providerAccount?.providerAccountId)
        assertEquals(2, model1?.transactions?.size)
        assertEquals(456L, model1?.transactions?.get(0)?.transactionId)
        assertEquals(457L, model1?.transactions?.get(1)?.transactionId)

        val model2 = testObserver.value().data?.get(1)

        assertEquals(346L, model2?.account?.accountId)
        assertEquals(234L, model2?.providerAccount?.providerAccount?.providerAccountId)
        assertEquals(2, model2?.transactions?.size)
        assertEquals(458L, model2?.transactions?.get(0)?.transactionId)
        assertEquals(459L, model2?.transactions?.get(1)?.transactionId)

        tearDown()
    }

    @Test
    fun testRefreshAccounts() {
        initSetup()

        val body = readStringFromJson(app, R.raw.accounts_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == AggregationAPI.URL_ACCOUNTS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshAccounts { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchAccounts().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(8, models?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals(AggregationAPI.URL_ACCOUNTS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshAccountsFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.refreshAccounts { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshAccountByID() {
        initSetup()

        val body = readStringFromJson(app, R.raw.account_id_542)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "aggregation/accounts/542") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshAccount(542L) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchAccount(542L).test()
            testObserver.awaitValue()
            val model = testObserver.value().data
            assertNotNull(model)
            assertEquals(542L, model?.accountId)
        }

        val request = mockServer.takeRequest()
        assertEquals("aggregation/accounts/542", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshAccountByIdFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.refreshAccount(542) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateAccountValid() {
        initSetup()

        val body = readStringFromJson(app, R.raw.account_id_542)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "aggregation/accounts/542") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.updateAccount(
                accountId = 542,
                hidden = false,
                included = true,
                favourite = randomBoolean(),
                accountSubType = AccountSubType.SAVINGS,
                nickName = randomUUID()) { result ->

            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchAccounts().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(1, models?.size)
            assertEquals(542L, models?.get(0)?.accountId)
            assertEquals(867L, models?.get(0)?.providerAccountId)
        }

        val request = mockServer.takeRequest()
        assertEquals("aggregation/accounts/542", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateAccountFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.updateAccount(
                accountId = 542,
                hidden = false,
                included = true,
                favourite = randomBoolean(),
                accountSubType = AccountSubType.SAVINGS,
                nickName = randomUUID()) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateAccountInvalid() {
        initSetup()

        val body = readStringFromJson(app, R.raw.account_id_542)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "aggregation/accounts/542") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.updateAccount(
                accountId = 542,
                hidden = true,
                included = true,
                favourite = randomBoolean(),
                accountSubType = AccountSubType.SAVINGS,
                nickName = randomUUID()) { result ->

            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertTrue(result.error is DataError)
            assertEquals(DataErrorType.API, (result.error as DataError).type)
            assertEquals(DataErrorSubType.INVALID_DATA, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    // Transaction Tests

    @Test
    fun testFetchTransactionByID() {
        initSetup()

        val data = testTransactionResponseData()
        val list = mutableListOf(testTransactionResponseData(), data, testTransactionResponseData())
        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val testObserver = aggregation.fetchTransaction(data.transactionId).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(data.transactionId, testObserver.value().data?.transactionId)

        tearDown()
    }

    @Test
    fun testFetchTransactions() {
        initSetup()

        val data1 = testTransactionResponseData(included = false)
        val data2 = testTransactionResponseData(included = true)
        val data3 = testTransactionResponseData(included = true)
        val data4 = testTransactionResponseData(included = false)
        val list = mutableListOf(data1, data2, data3, data4)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val testObserver = aggregation.fetchTransactions(included = true).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchTransactionByIds() {
        initSetup()

        val data1 = testTransactionResponseData(transactionId = 100)
        val data2 = testTransactionResponseData(transactionId = 101)
        val data3 = testTransactionResponseData(transactionId = 102)
        val data4 = testTransactionResponseData(transactionId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val testObserver = aggregation.fetchTransactions(longArrayOf(101, 103)).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchTransactionsByAccountId() {
        initSetup()

        val data1 = testTransactionResponseData(accountId = 1)
        val data2 = testTransactionResponseData(accountId = 2)
        val data3 = testTransactionResponseData(accountId = 1)
        val data4 = testTransactionResponseData(accountId = 1)
        val list = mutableListOf(data1, data2, data3, data4)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val testObserver = aggregation.fetchTransactions(accountId = 1).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(3, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchTransactionsByQuery() {
        initSetup()

        val data1 = testTransactionResponseData(transactionId = 100)
        val data2 = testTransactionResponseData(transactionId = 101)
        val data3 = testTransactionResponseData(transactionId = 102)
        val data4 = testTransactionResponseData(transactionId = 103)
        val list = mutableListOf(data1, data2, data3, data4)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val query = SimpleSQLiteQuery("SELECT * FROM transaction_model WHERE transaction_id IN (101,102,103)")

        val testObserver = aggregation.fetchTransactions(query).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(3, testObserver.value().data?.size)
        assertTrue(testObserver.value().data?.map { it.transactionId }?.toList()?.containsAll(listOf(101L, 102L, 103L)) == true)

        tearDown()
    }

    @Test
    fun testFetchTransactionsByTags() {
        initSetup()

        val data1 = testTransactionResponseData(transactionId = 100, userTags = listOf("abc", "def"))
        val data2 = testTransactionResponseData(transactionId = 101, userTags = listOf("abc2", "def2"))
        val data3 = testTransactionResponseData(transactionId = 102, userTags = listOf("hello", "how"))
        val data4 = testTransactionResponseData(transactionId = 103, userTags = listOf("why", "are"))
        val data5 = testTransactionResponseData(transactionId = 104, userTags = listOf("why", "are"))
        val data6 = testTransactionResponseData(transactionId = 105, userTags = listOf("why", "are"))
        val list = mutableListOf(data1, data2, data3, data4, data5, data6)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val tagList = listOf("why", "are")
        val liveDataObj = aggregation.fetchTransactions(userTags = tagList).test()
        val transactionList = liveDataObj.value()
        assertNotNull(transactionList.data)
        assert(transactionList.data?.isNotEmpty()!!)
        assertEquals(3, transactionList.data?.size!!)

        tearDown()
    }

    @Test
    fun testFetchTransactionsByUserTagsWithRelation() {
        initSetup()

        database.transactions().insert(testTransactionResponseData(transactionId = 122, accountId = 234, categoryId = 567, merchantId = 678, userTags = listOf("why", "are")).toTransaction())
        database.transactions().insert(testTransactionResponseData(transactionId = 123, accountId = 234, categoryId = 567, merchantId = 678, userTags = listOf("why", "are")).toTransaction())
        database.transactions().insert(testTransactionResponseData(transactionId = 124, accountId = 235, categoryId = 567, merchantId = 678, userTags = listOf("why", "are")).toTransaction())
        database.transactions().insert(testTransactionResponseData(transactionId = 125, accountId = 235, categoryId = 567, merchantId = 678, userTags = listOf("whyasdas", "areasdasd")).toTransaction())
        database.accounts().insert(testAccountResponseData(accountId = 234, providerAccountId = 345).toAccount())
        database.accounts().insert(testAccountResponseData(accountId = 235, providerAccountId = 345).toAccount())
        database.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 345, providerId = 456).toProviderAccount())
        database.providers().insert(testProviderResponseData(providerId = 456).toProvider())
        database.transactionCategories().insert(testTransactionCategoryResponseData(transactionCategoryId = 567).toTransactionCategory())
        database.merchants().insert(testMerchantResponseData(merchantId = 678).toMerchant())

        val tagList = listOf("why", "are")
        val testObserver = aggregation.fetchTransactionsWithRelation(userTags = tagList).test()
        testObserver.awaitValue()

        val list = testObserver.value().data

        assertTrue(list?.isNotEmpty()!!)
        assertEquals(3, list.size)

        val model1 = list[0]

        assertEquals(122L, model1.transaction?.transactionId)
        assertEquals(678L, model1.merchant?.merchantId)
        assertEquals(567L, model1.transactionCategory?.transactionCategoryId)
        assertEquals(234L, model1.account?.account?.accountId)

        val model2 = list[1]

        assertEquals(123L, model2.transaction?.transactionId)
        assertEquals(678L, model2.merchant?.merchantId)
        assertEquals(567L, model2.transactionCategory?.transactionCategoryId)
        assertEquals(234L, model2.account?.account?.accountId)

        tearDown()
    }

    @Test
    fun testFetchTransactionByIDWithRelation() {
        initSetup()

        database.transactions().insert(testTransactionResponseData(transactionId = 123, accountId = 234, categoryId = 567, merchantId = 678).toTransaction())
        database.accounts().insert(testAccountResponseData(accountId = 234, providerAccountId = 345).toAccount())
        database.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 345, providerId = 456).toProviderAccount())
        database.providers().insert(testProviderResponseData(providerId = 456).toProvider())
        database.transactionCategories().insert(testTransactionCategoryResponseData(transactionCategoryId = 567).toTransactionCategory())
        database.merchants().insert(testMerchantResponseData(merchantId = 678).toMerchant())

        val testObserver = aggregation.fetchTransactionWithRelation(transactionId = 123).test()
        testObserver.awaitValue()

        val model = testObserver.value().data

        assertEquals(123L, model?.transaction?.transactionId)
        assertEquals(678L, model?.merchant?.merchantId)
        assertEquals(567L, model?.transactionCategory?.transactionCategoryId)
        assertEquals(234L, model?.account?.account?.accountId)

        tearDown()
    }

    @Test
    fun testFetchTransactionsWithRelation() {
        initSetup()

        database.transactions().insert(testTransactionResponseData(transactionId = 123, accountId = 234, categoryId = 567, merchantId = 678).toTransaction())
        database.accounts().insert(testAccountResponseData(accountId = 234, providerAccountId = 345).toAccount())
        database.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 345, providerId = 456).toProviderAccount())
        database.providers().insert(testProviderResponseData(providerId = 456).toProvider())
        database.transactionCategories().insert(testTransactionCategoryResponseData(transactionCategoryId = 567).toTransactionCategory())
        database.merchants().insert(testMerchantResponseData(merchantId = 678).toMerchant())

        val testObserver = aggregation.fetchTransactionsWithRelation().test()
        testObserver.awaitValue()

        assertNotNull(testObserver.value().data)
        assertEquals(1, testObserver.value().data?.size)

        val model = testObserver.value().data?.get(0)

        assertEquals(123L, model?.transaction?.transactionId)
        assertEquals(678L, model?.merchant?.merchantId)
        assertEquals(567L, model?.transactionCategory?.transactionCategoryId)
        assertEquals(234L, model?.account?.account?.accountId)

        tearDown()
    }

    @Test
    fun testFetchTransactionByIdsWithRelation() {
        initSetup()

        database.transactions().insert(testTransactionResponseData(transactionId = 122, accountId = 234, categoryId = 567, merchantId = 678).toTransaction())
        database.transactions().insert(testTransactionResponseData(transactionId = 123, accountId = 234, categoryId = 567, merchantId = 678).toTransaction())
        database.accounts().insert(testAccountResponseData(accountId = 234, providerAccountId = 345).toAccount())
        database.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 345, providerId = 456).toProviderAccount())
        database.providers().insert(testProviderResponseData(providerId = 456).toProvider())
        database.transactionCategories().insert(testTransactionCategoryResponseData(transactionCategoryId = 567).toTransactionCategory())
        database.merchants().insert(testMerchantResponseData(merchantId = 678).toMerchant())

        val testObserver = aggregation.fetchTransactionsWithRelation(transactionIds = longArrayOf(122, 123)).test()
        testObserver.awaitValue()

        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        val model1 = testObserver.value().data?.get(0)

        assertEquals(122L, model1?.transaction?.transactionId)
        assertEquals(678L, model1?.merchant?.merchantId)
        assertEquals(567L, model1?.transactionCategory?.transactionCategoryId)
        assertEquals(234L, model1?.account?.account?.accountId)

        val model2 = testObserver.value().data?.get(1)

        assertEquals(123L, model2?.transaction?.transactionId)
        assertEquals(678L, model2?.merchant?.merchantId)
        assertEquals(567L, model2?.transactionCategory?.transactionCategoryId)
        assertEquals(234L, model2?.account?.account?.accountId)

        tearDown()
    }

    @Test
    fun testFetchTransactionsByAccountIdWithRelation() {
        initSetup()

        database.transactions().insert(testTransactionResponseData(transactionId = 122, accountId = 234, categoryId = 567, merchantId = 678).toTransaction())
        database.transactions().insert(testTransactionResponseData(transactionId = 123, accountId = 234, categoryId = 567, merchantId = 678).toTransaction())
        database.accounts().insert(testAccountResponseData(accountId = 234, providerAccountId = 345).toAccount())
        database.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 345, providerId = 456).toProviderAccount())
        database.providers().insert(testProviderResponseData(providerId = 456).toProvider())
        database.transactionCategories().insert(testTransactionCategoryResponseData(transactionCategoryId = 567).toTransactionCategory())
        database.merchants().insert(testMerchantResponseData(merchantId = 678).toMerchant())

        val testObserver = aggregation.fetchTransactionsWithRelation(accountId = 234).test()
        testObserver.awaitValue()

        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        val model1 = testObserver.value().data?.get(0)

        assertEquals(122L, model1?.transaction?.transactionId)
        assertEquals(678L, model1?.merchant?.merchantId)
        assertEquals(567L, model1?.transactionCategory?.transactionCategoryId)
        assertEquals(234L, model1?.account?.account?.accountId)

        val model2 = testObserver.value().data?.get(1)

        assertEquals(123L, model2?.transaction?.transactionId)
        assertEquals(678L, model2?.merchant?.merchantId)
        assertEquals(567L, model2?.transactionCategory?.transactionCategoryId)
        assertEquals(234L, model2?.account?.account?.accountId)

        tearDown()
    }

    @Test
    fun testFetchTransactionsByQueryWithRelation() {
        initSetup()

        database.transactions().insert(testTransactionResponseData(transactionId = 122, accountId = 234, categoryId = 567, merchantId = 678).toTransaction())
        database.transactions().insert(testTransactionResponseData(transactionId = 123, accountId = 234, categoryId = 567, merchantId = 678).toTransaction())
        database.transactions().insert(testTransactionResponseData(transactionId = 124, accountId = 235, categoryId = 567, merchantId = 678).toTransaction())
        database.transactions().insert(testTransactionResponseData(transactionId = 125, accountId = 235, categoryId = 567, merchantId = 678).toTransaction())
        database.accounts().insert(testAccountResponseData(accountId = 234, providerAccountId = 345).toAccount())
        database.accounts().insert(testAccountResponseData(accountId = 235, providerAccountId = 345).toAccount())
        database.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 345, providerId = 456).toProviderAccount())
        database.providers().insert(testProviderResponseData(providerId = 456).toProvider())
        database.transactionCategories().insert(testTransactionCategoryResponseData(transactionCategoryId = 567).toTransactionCategory())
        database.merchants().insert(testMerchantResponseData(merchantId = 678).toMerchant())

        val query = SimpleSQLiteQuery("SELECT * FROM transaction_model WHERE account_id = 235")

        val testObserver = aggregation.fetchTransactionsWithRelation(query).test()
        testObserver.awaitValue()

        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        val model1 = testObserver.value().data?.get(0)

        assertEquals(124L, model1?.transaction?.transactionId)
        assertEquals(678L, model1?.merchant?.merchantId)
        assertEquals(567L, model1?.transactionCategory?.transactionCategoryId)
        assertEquals(235L, model1?.account?.account?.accountId)

        val model2 = testObserver.value().data?.get(1)

        assertEquals(125L, model2?.transaction?.transactionId)
        assertEquals(678L, model2?.merchant?.merchantId)
        assertEquals(567L, model2?.transactionCategory?.transactionCategoryId)
        assertEquals(235L, model2?.account?.account?.accountId)

        tearDown()
    }

    @Test
    fun testRefreshTransactions() {
        initSetup()

        val body = readStringFromJson(app, R.raw.transactions_2018_08_01_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS}?from_date=2018-06-01&to_date=2018-08-08&skip=0&count=200") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransactions(fromDate = "2018-06-01", toDate = "2018-08-08") { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchTransactions().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(111, models?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals("${AggregationAPI.URL_TRANSACTIONS}?from_date=2018-06-01&to_date=2018-08-08&skip=0&count=200", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshTransactionsFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.refreshTransactions(fromDate = "2018-06-01", toDate = "2018-08-08") { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshPaginatedTransactions() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS}?from_date=2018-08-01&to_date=2018-08-31&skip=0&count=200") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transactions_2018_12_04_count_200_skip_0))
                } else if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS}?from_date=2018-08-01&to_date=2018-08-31&skip=200&count=200") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transactions_2018_12_04_count_200_skip_200))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransactions(fromDate = "2018-08-01", toDate = "2018-08-31") { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchTransactions().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(311, models?.size)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshTransactionByID() {
        initSetup()

        val body = readStringFromJson(app, R.raw.transaction_id_194630)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "aggregation/transactions/194630") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransaction(194630L) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchTransaction(194630L).test()
            testObserver.awaitValue()
            val model = testObserver.value().data
            assertNotNull(model)
            assertEquals(194630L, model?.transactionId)
        }

        val request = mockServer.takeRequest()
        assertEquals("aggregation/transactions/194630", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshTransactionByIdFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.refreshTransaction(194630L) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshTransactionsByIds() {
        initSetup()

        val body = readStringFromJson(app, R.raw.transactions_2018_08_01_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS}?transaction_ids=1%2C2%2C3%2C4%2C5") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransactions(longArrayOf(1, 2, 3, 4, 5)) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchTransactions().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(111, models?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals("${AggregationAPI.URL_TRANSACTIONS}?transaction_ids=1%2C2%2C3%2C4%2C5", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshTransactionByIdsFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.refreshTransactions(longArrayOf(1, 2, 3, 4, 5)) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testExcludeTransaction() {
        initSetup()

        val body = readStringFromJson(app, R.raw.transaction_id_194630_excluded)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "aggregation/transactions/194630") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val transaction = testTransactionResponseData(transactionId = 194630, included = true).toTransaction()
        database.transactions().insert(transaction)

        aggregation.excludeTransaction(transactionId = 194630, excluded = true, applyToAll = true) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchTransactions().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(1, models?.size)
            assertEquals(194630L, models?.get(0)?.transactionId)
            assertTrue(models?.get(0)?.included == false)
        }

        val request = mockServer.takeRequest()
        assertEquals("aggregation/transactions/194630", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testExcludeTransactionFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.excludeTransaction(transactionId = 194630, excluded = true, applyToAll = true) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRecategoriseTransaction() {
        initSetup()

        val body = readStringFromJson(app, R.raw.transaction_id_194630)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "aggregation/transactions/194630") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val transaction = testTransactionResponseData(transactionId = 194630, categoryId = 123).toTransaction()
        database.transactions().insert(transaction)

        aggregation.recategoriseTransaction(transactionId = 194630, transactionCategoryId = 77, applyToAll = true) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchTransactions().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(1, models?.size)
            assertEquals(194630L, models?.get(0)?.transactionId)
            assertEquals(77L, models?.get(0)?.categoryId)
        }

        val request = mockServer.takeRequest()
        assertEquals("aggregation/transactions/194630", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRecategoriseTransactionFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.recategoriseTransaction(transactionId = 194630, transactionCategoryId = 77, applyToAll = true) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateTransaction() {
        initSetup()

        val body = readStringFromJson(app, R.raw.transaction_id_194630)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "aggregation/transactions/194630") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val transaction = testTransactionResponseData().toTransaction()

        aggregation.updateTransaction(194630, transaction) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchTransactions().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(1, models?.size)
            assertEquals(194630L, models?.get(0)?.transactionId)
            assertEquals(939L, models?.get(0)?.accountId)
        }

        val request = mockServer.takeRequest()
        assertEquals("aggregation/transactions/194630", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateTransactionFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.updateTransaction(194630, testTransactionResponseData().toTransaction()) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testTransactionSearch() {
        initSetup()

        val searchTerm = "Travel"
        val requestPath = "${AggregationAPI.URL_TRANSACTIONS_SEARCH}?search_term=$searchTerm&skip=0&count=200"

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transactions_2018_08_01_valid))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.transactionSearch(searchTerm = searchTerm) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val transactionIds = resource.data
            assertNotNull(transactionIds)
            assertEquals(111, transactionIds?.size)

            val testObserver = aggregation.fetchTransactions(transactionIds = transactionIds!!).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(111, models?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testTransactionSearchFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.transactionSearch(searchTerm = "Travel") { result ->
            assertEquals(Resource.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testSearchPaginatedTransactions() {
        initSetup()

        val searchTerm = "Travel"
        val requestPath1 = "${AggregationAPI.URL_TRANSACTIONS_SEARCH}?search_term=$searchTerm&from_date=2018-08-01&to_date=2018-08-31&skip=0&count=200"
        val requestPath2 = "${AggregationAPI.URL_TRANSACTIONS_SEARCH}?search_term=$searchTerm&from_date=2018-08-01&to_date=2018-08-31&skip=200&count=200"

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath1) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transactions_2018_12_04_count_200_skip_0))
                } else if (request?.trimmedPath == requestPath2) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transactions_2018_12_04_count_200_skip_200))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.transactionSearch(searchTerm = searchTerm, page = 0, fromDate = "2018-08-01", toDate = "2018-08-31") { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val transactionIds = resource.data
            assertNotNull(transactionIds)
            assertEquals(200, transactionIds?.size)

            val testObserver = aggregation.fetchTransactions(transactionIds = transactionIds!!).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(200, models?.size)
        }

        wait(3)

        aggregation.transactionSearch(searchTerm = searchTerm, page = 1, fromDate = "2018-08-01", toDate = "2018-08-31") { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val transactionIds = resource.data
            assertNotNull(transactionIds)
            assertEquals(111, transactionIds?.size)

            val testObserver = aggregation.fetchTransactions(transactionIds = transactionIds!!).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(111, models?.size)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testTransactionsFetchMissingMerchants() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS}?from_date=2018-06-01&to_date=2018-08-08&skip=0&count=200") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.transactions_2018_08_01_valid))
                } else if (request?.trimmedPath?.contains(AggregationAPI.URL_MERCHANTS) == true) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.merchants_by_id))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransactions(fromDate = "2018-06-01", toDate = "2018-08-08") { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchTransactions().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(111, models?.size)
        }

        wait(3)

        val testObserver2 = aggregation.fetchMerchants().test()
        testObserver2.awaitValue()
        val models2 = testObserver2.value().data
        assertNotNull(models2)
        assertEquals(2, models2?.size)
        assertEquals(238L, models2?.get(0)?.merchantId)

        tearDown()
    }

    // Transaction Summary Tests

    @Test
    fun testFetchTransactionsSummary() {
        initSetup()

        val body = readStringFromJson(app, R.raw.transactions_summary_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS_SUMMARY}?from_date=2018-06-01&to_date=2018-08-08") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.fetchTransactionsSummary(fromDate = "2018-06-01", toDate = "2018-08-08") { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            assertNotNull(resource.data)
            assertEquals(166L, resource.data?.count)
            assertEquals((-1039.0).toBigDecimal(), resource.data?.sum)
        }

        val request = mockServer.takeRequest()
        assertEquals("${AggregationAPI.URL_TRANSACTIONS_SUMMARY}?from_date=2018-06-01&to_date=2018-08-08", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchTransactionsSummaryFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.fetchTransactionsSummary(fromDate = "2018-06-01", toDate = "2018-08-08") { result ->
            assertEquals(Resource.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchTransactionsSummaryByIDs() {
        initSetup()

        val body = readStringFromJson(app, R.raw.transactions_summary_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS_SUMMARY}?transaction_ids=1%2C2%2C3%2C4%2C5") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.fetchTransactionsSummary(transactionIds = longArrayOf(1, 2, 3, 4, 5)) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            assertNotNull(resource.data)
            assertEquals(166L, resource.data?.count)
            assertEquals((-1039.0).toBigDecimal(), resource.data?.sum)
        }

        val request = mockServer.takeRequest()
        assertEquals("${AggregationAPI.URL_TRANSACTIONS_SUMMARY}?transaction_ids=1%2C2%2C3%2C4%2C5", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchTransactionsSummaryByIDsFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.fetchTransactionsSummary(transactionIds = longArrayOf(1, 2, 3, 4, 5)) { result ->
            assertEquals(Resource.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    // Transaction Tags Tests

    @Test
    fun testFetchTagsForTransaction() {
        initSetup()

        val body = readStringFromJson(app, R.raw.transaction_update_tag)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS}/12345/tags") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.fetchTagsForTransaction(transactionId = 12345) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            assertNotNull(resource.data)
            assertEquals(4, resource.data?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals("${AggregationAPI.URL_TRANSACTIONS}/12345/tags", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchTagsForTransactionFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.fetchTagsForTransaction(transactionId = 12345) { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (resource.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testAddTagsToTransaction() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS}/12345/tags") {
                    return MockResponse()
                            .setResponseCode(200)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val data = testTransactionResponseData(transactionId = 12345, userTags = listOf("tagone", "tagfive"))
        database.transactions().insert(data.toTransaction())

        val tagPairs = arrayOf(
                TagApplyAllPair("tagone", true),
                TagApplyAllPair("tagtwo", true),
                TagApplyAllPair("tagthree", true),
                TagApplyAllPair("tagfour", true))

        aggregation.addTagsToTransaction(transactionId = 12345, tagApplyAllPairs = tagPairs) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchTransaction(transactionId = 12345).test()
            testObserver.awaitValue()
            val model = testObserver.value().data
            assertNotNull(model)
            assertEquals(5, model?.userTags?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals("${AggregationAPI.URL_TRANSACTIONS}/12345/tags", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testAddTagsToTransactionFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.addTagsToTransaction(transactionId = 12345, tagApplyAllPairs = arrayOf(TagApplyAllPair("tag1", true))) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testAddTagsToTransactionFailsIfEmptyTags() {
        initSetup()

        aggregation.addTagsToTransaction(transactionId = 12345, tagApplyAllPairs = arrayOf()) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.API, (result.error as DataError).type)
            assertEquals(DataErrorSubType.INVALID_DATA, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRemoveTagsFromTransaction() {
        initSetup()

        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_TRANSACTIONS}/12345/tags") {
                    return MockResponse()
                            .setResponseCode(200)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val data = testTransactionResponseData(transactionId = 12345, userTags = listOf("tagone", "tagtwo", "tagfive"))
        database.transactions().insert(data.toTransaction())

        val tagPairs = arrayOf(
                TagApplyAllPair("tagone", true),
                TagApplyAllPair("tagtwo", true),
                TagApplyAllPair("tagthree", true),
                TagApplyAllPair("tagfour", true))

        aggregation.removeTagsFromTransaction(transactionId = 12345, tagApplyAllPairs = tagPairs) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchTransaction(transactionId = 12345).test()
            testObserver.awaitValue()
            val model = testObserver.value().data
            assertNotNull(model)
            assertEquals(1, model?.userTags?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals("${AggregationAPI.URL_TRANSACTIONS}/12345/tags", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRemoveTagsFromTransactionFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.removeTagsFromTransaction(transactionId = 12345, tagApplyAllPairs = arrayOf(TagApplyAllPair("tag1", true))) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRemoveTagsFromTransactionFailsIfEmptyTags() {
        initSetup()

        aggregation.removeTagsFromTransaction(transactionId = 12345, tagApplyAllPairs = arrayOf()) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.API, (result.error as DataError).type)
            assertEquals(DataErrorSubType.INVALID_DATA, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchTransactionUserTags() {
        initSetup()

        val data1 = testTransactionTagData("tag1", createdAt = "2019-03-03")
        val data2 = testTransactionTagData("tag2", createdAt = "2019-03-09")
        val data3 = testTransactionTagData("pub", createdAt = "2019-03-02")
        val data4 = testTransactionTagData("TaG6", createdAt = "2019-03-01")
        val list = mutableListOf(data1, data2, data3, data4)
        database.userTags().insertAll(list)

        val testObserver = aggregation.fetchTransactionUserTags(searchTerm = "tag", sortBy = TagsSortType.NAME, orderBy = OrderType.ASC).test()
        testObserver.awaitValue()
        val list2 = testObserver.value().data!!
        assertEquals(3, list2.size)
        tearDown()

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchTransactionUserTagsByQuery() {
        initSetup()

        val data1 = testTransactionTagData("tag1", createdAt = "2019-03-03")
        val data2 = testTransactionTagData("tag2", createdAt = "2019-03-09")
        val data3 = testTransactionTagData("tag4", createdAt = "2019-03-02")
        val data4 = testTransactionTagData("tag3", createdAt = "2019-03-01")
        val list = mutableListOf(data1, data2, data3, data4)
        database.userTags().insertAll(list)

        val fromDate = "2019-03-03"
        val endDate = "2019-03-07"

        val sql = "SELECT * FROM transaction_user_tags where created_at between Date('$fromDate') and Date('$endDate')"
        val query = SimpleSQLiteQuery(sql)
        val testObserver = aggregation.fetchTransactionUserTags(query).test()
        testObserver.awaitValue()
        val list2 = testObserver.value().data!!
        assertEquals(1, list2.size)
        tearDown()

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshTransactionUserTags() {
        initSetup()

        val body = readStringFromJson(app, R.raw.transactions_user_tags)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == AggregationAPI.URL_USER_TAGS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransactionUserTags { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchTransactionUserTags().test()
            val model = testObserver.value().data
            assertNotNull(model)
            assertEquals("cafe", model?.get(0)?.name)
            assertEquals(model?.size, 5)
        }

        val request = mockServer.takeRequest()
        assertEquals(AggregationAPI.URL_USER_TAGS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshTransactionUserTagsFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.refreshTransactionUserTags { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchSuggestedTransactionTags() {
        initSetup()
        val body = readStringFromJson(app, R.raw.transactions_user_tags)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath!!.contains(AggregationAPI.URL_SUGGESTED_TAGS)) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.fetchTransactionSuggestedTags("ca") {
            assertEquals(Resource.Status.SUCCESS, it.status)
            assertNull(it.error)
            val model = it.data!!
            assertNotNull(model)
            assertEquals("pub_lunch", model[0].name)
            assertEquals(model.size, 5)
        }
        wait(3)
        tearDown()
    }

    @Test
    fun testFetchSuggestedTransactionTagsFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.fetchTransactionSuggestedTags("ca") { result ->
            assertEquals(Resource.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    // Transaction Category Tests

    @Test
    fun testFetchTransactionCategoryByID() {
        initSetup()

        val data = testTransactionCategoryResponseData()
        val list = mutableListOf(testTransactionCategoryResponseData(), data, testTransactionCategoryResponseData())
        database.transactionCategories().insertAll(*list.map { it.toTransactionCategory() }.toList().toTypedArray())

        val testObserver = aggregation.fetchTransactionCategory(data.transactionCategoryId).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(data.transactionCategoryId, testObserver.value().data?.transactionCategoryId)

        tearDown()
    }

    @Test
    fun testFetchTransactionCategories() {
        initSetup()

        val data1 = testTransactionCategoryResponseData()
        val data2 = testTransactionCategoryResponseData()
        val data3 = testTransactionCategoryResponseData()
        val data4 = testTransactionCategoryResponseData()
        val list = mutableListOf(data1, data2, data3, data4)

        database.transactionCategories().insertAll(*list.map { it.toTransactionCategory() }.toList().toTypedArray())

        val testObserver = aggregation.fetchTransactionCategories().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(4, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testRefreshTransactionCategories() {
        initSetup()

        val body = readStringFromJson(app, R.raw.transaction_categories_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == AggregationAPI.URL_TRANSACTION_CATEGORIES) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransactionCategories { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchTransactionCategories().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(63, models?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals(AggregationAPI.URL_TRANSACTION_CATEGORIES, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshTransactionCategoriesFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.refreshTransactionCategories { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    // Merchant Tests

    @Test
    fun testFetchMerchantByID() {
        initSetup()

        val data = testMerchantResponseData()
        val list = mutableListOf(testMerchantResponseData(), data, testMerchantResponseData())
        database.merchants().insertAll(*list.map { it.toMerchant() }.toList().toTypedArray())

        val testObserver = aggregation.fetchMerchant(data.merchantId).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(data.merchantId, testObserver.value().data?.merchantId)

        tearDown()
    }

    @Test
    fun testFetchMerchants() {
        initSetup()

        val data1 = testMerchantResponseData()
        val data2 = testMerchantResponseData()
        val data3 = testMerchantResponseData()
        val data4 = testMerchantResponseData()
        val list = mutableListOf(data1, data2, data3, data4)

        database.merchants().insertAll(*list.map { it.toMerchant() }.toList().toTypedArray())

        val testObserver = aggregation.fetchMerchants().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(4, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testRefreshMerchants() {
        initSetup()

        val body = readStringFromJson(app, R.raw.merchants_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == AggregationAPI.URL_MERCHANTS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshMerchants { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchMerchants().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(1200, models?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals(AggregationAPI.URL_MERCHANTS, request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshMerchantsFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.refreshMerchants { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshMerchantByID() {
        initSetup()

        val body = readStringFromJson(app, R.raw.merchant_id_197)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "aggregation/merchants/197") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshMerchant(197L) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchMerchant(197L).test()
            testObserver.awaitValue()
            val model = testObserver.value().data
            assertNotNull(model)
            assertEquals(197L, model?.merchantId)
        }

        val request = mockServer.takeRequest()
        assertEquals("aggregation/merchants/197", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshMerchantByIDFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.refreshMerchant(197L) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshMerchantsByIds() {
        initSetup()

        val body = readStringFromJson(app, R.raw.merchants_by_id)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "${AggregationAPI.URL_MERCHANTS}?merchant_ids=22%2C30%2C31%2C106%2C691") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshMerchants(longArrayOf(22, 30, 31, 106, 691)) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchMerchants().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(2, models?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals("${AggregationAPI.URL_MERCHANTS}?merchant_ids=22%2C30%2C31%2C106%2C691", request.trimmedPath)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshMerchantsByIdsFailsIfLoggedOut() {
        initSetup()

        preferences.loggedIn = false

        aggregation.refreshMerchants(longArrayOf(22, 30, 31, 106, 691)) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.LOGGED_OUT, (result.error as DataError).subType)
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testLinkingRemoveCachedCascade() {
        initSetup()

        val body = readStringFromJson(app, R.raw.providers_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == AggregationAPI.URL_PROVIDERS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        database.providers().insert(testProviderResponseData(providerId = 123).toProvider())
        database.providerAccounts().insert(testProviderAccountResponseData(providerAccountId = 234, providerId = 123).toProviderAccount())
        database.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        database.transactions().insert(testTransactionResponseData(transactionId = 456, accountId = 345).toTransaction())

        val testObserver1 = aggregation.fetchProvider(providerId = 123).test()
        testObserver1.awaitValue()
        assertEquals(123L, testObserver1.value().data?.providerId)

        val testObserver2 = aggregation.fetchProviderAccount(providerAccountId = 234).test()
        testObserver2.awaitValue()
        assertEquals(234L, testObserver2.value().data?.providerAccountId)

        val testObserver3 = aggregation.fetchAccount(accountId = 345).test()
        testObserver3.awaitValue()
        assertEquals(345L, testObserver3.value().data?.accountId)

        val testObserver4 = aggregation.fetchTransaction(transactionId = 456).test()
        testObserver4.awaitValue()
        assertEquals(456L, testObserver4.value().data?.transactionId)

        aggregation.refreshProviders { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver5 = aggregation.fetchProviders().test()
            testObserver5.awaitValue()
            val models = testObserver5.value().data
            assertNotNull(models)
            assertEquals(311, models?.size)

            val testObserver6 = aggregation.fetchProvider(providerId = 123).test()
            testObserver6.awaitValue()
            assertNull(testObserver6.value().data)

            val testObserver7 = aggregation.fetchProviderAccount(providerAccountId = 234).test()
            testObserver7.awaitValue()
            assertNull(testObserver7.value().data)

            val testObserver8 = aggregation.fetchAccount(accountId = 345).test()
            testObserver8.awaitValue()
            assertNull(testObserver8.value().data)

            val testObserver9 = aggregation.fetchTransaction(transactionId = 456).test()
            testObserver9.awaitValue()
            assertNull(testObserver9.value().data)
        }

        val request = mockServer.takeRequest()
        assertEquals(AggregationAPI.URL_PROVIDERS, request.trimmedPath)

        wait(3)

        tearDown()
    }
}