package us.frollo.frollosdk.aggregation

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.jraska.livedata.test
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.network.NetworkHelper
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.AggregationAPI
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.mapping.toAccount
import us.frollo.frollosdk.mapping.toProvider
import us.frollo.frollosdk.mapping.toProviderAccount
import us.frollo.frollosdk.mapping.toTransaction
import us.frollo.frollosdk.model.*
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountSubType
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.randomBoolean
import us.frollo.frollosdk.testutils.randomUUID
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.wait

class AggregationTest {

    @get:Rule
    val testRule = InstantTaskExecutorRule()
    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private lateinit var mockServer: MockWebServer
    private lateinit var preferences: Preferences
    private lateinit var keystore: Keystore
    private lateinit var database: SDKDatabase
    private lateinit var network: NetworkService

    private lateinit var aggregation: Aggregation

    private fun initSetup() {
        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url("/")

        if (!FrolloSDK.isSetup) FrolloSDK.setup(app, SetupParams.Builder().serverUrl(baseUrl.toString()).build()) {}

        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        database = SDKDatabase.getInstance(app)
        network = NetworkService(baseUrl.toString(), keystore, preferences)

        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        aggregation = Aggregation(network, database)
    }

    private fun tearDown() {
        mockServer.shutdown()
        preferences.resetAll()
        database.clearAllTables()
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
    fun testRefreshProviders() {
        initSetup()

        val body = readStringFromJson(app, R.raw.providers_valid)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == AggregationAPI.URL_PROVIDERS) {
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
        assertEquals(AggregationAPI.URL_PROVIDERS, request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshProviderByID() {
        initSetup()

        val body = readStringFromJson(app, R.raw.provider_id_12345)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == "${NetworkHelper.API_VERSION_PATH}/aggregation/providers/12345") {
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
        assertEquals("${NetworkHelper.API_VERSION_PATH}/aggregation/providers/12345", request.path)

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

        val data1 = testProviderAccountResponseData()
        val data2 = testProviderAccountResponseData()
        val data3 = testProviderAccountResponseData()
        val data4 = testProviderAccountResponseData()
        val list = mutableListOf(data1, data2, data3, data4)

        database.providerAccounts().insertAll(*list.map { it.toProviderAccount() }.toList().toTypedArray())

        val testObserver = aggregation.fetchProviderAccounts().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(4, testObserver.value().data?.size)

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

        val testObserver = aggregation.fetchProviderAccountsByProviderId(providerId = 1).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(3, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testRefreshProviderAccounts() {
        initSetup()

        val body = readStringFromJson(app, R.raw.provider_accounts_valid)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == AggregationAPI.URL_PROVIDER_ACCOUNTS) {
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
        assertEquals(AggregationAPI.URL_PROVIDER_ACCOUNTS, request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshProviderAccountByID() {
        initSetup()

        val body = readStringFromJson(app, R.raw.provider_account_id_123)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == "${NetworkHelper.API_VERSION_PATH}/aggregation/provideraccounts/123") {
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
        assertEquals("${NetworkHelper.API_VERSION_PATH}/aggregation/provideraccounts/123", request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testCreateProviderAccount() {
        initSetup()

        val body = readStringFromJson(app, R.raw.provider_account_id_123)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == AggregationAPI.URL_PROVIDER_ACCOUNTS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.createProviderAccount(providerId = 4078, loginForm = loginFormFilledData()) { result ->
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
        assertEquals(AggregationAPI.URL_PROVIDER_ACCOUNTS, request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testDeleteProviderAccount() {
        initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == "${NetworkHelper.API_VERSION_PATH}/aggregation/provideraccounts/12345") {
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
        assertEquals("${NetworkHelper.API_VERSION_PATH}/aggregation/provideraccounts/12345", request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateProviderAccount() {
        initSetup()

        val body = readStringFromJson(app, R.raw.provider_account_id_123)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == "${NetworkHelper.API_VERSION_PATH}/aggregation/provideraccounts/123") {
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
        assertEquals("${NetworkHelper.API_VERSION_PATH}/aggregation/provideraccounts/123", request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testProviderAccountsFetchMissingProviders() {
        //TODO: to be implemented
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

        val data1 = testAccountResponseData()
        val data2 = testAccountResponseData()
        val data3 = testAccountResponseData()
        val data4 = testAccountResponseData()
        val list = mutableListOf(data1, data2, data3, data4)

        database.accounts().insertAll(*list.map { it.toAccount() }.toList().toTypedArray())

        val testObserver = aggregation.fetchAccounts().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(4, testObserver.value().data?.size)

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

        val testObserver = aggregation.fetchAccountsByProviderAccountId(providerAccountId = 1).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(3, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testRefreshAccounts() {
        initSetup()

        val body = readStringFromJson(app, R.raw.accounts_valid)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == AggregationAPI.URL_ACCOUNTS) {
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
            assertEquals(4, models?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals(AggregationAPI.URL_ACCOUNTS, request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshAccountByID() {
        initSetup()

        val body = readStringFromJson(app, R.raw.account_id_542)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == "${NetworkHelper.API_VERSION_PATH}/aggregation/accounts/542") {
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
        assertEquals("${NetworkHelper.API_VERSION_PATH}/aggregation/accounts/542", request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateAccountValid() {
        initSetup()

        val body = readStringFromJson(app, R.raw.account_id_542)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == "${NetworkHelper.API_VERSION_PATH}/aggregation/accounts/542") {
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
        assertEquals("${NetworkHelper.API_VERSION_PATH}/aggregation/accounts/542", request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateAccountInvalid() {
        initSetup()

        val body = readStringFromJson(app, R.raw.account_id_542)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == "${NetworkHelper.API_VERSION_PATH}/aggregation/accounts/542") {
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

        val data1 = testTransactionResponseData()
        val data2 = testTransactionResponseData()
        val data3 = testTransactionResponseData()
        val data4 = testTransactionResponseData()
        val list = mutableListOf(data1, data2, data3, data4)

        database.transactions().insertAll(*list.map { it.toTransaction() }.toList().toTypedArray())

        val testObserver = aggregation.fetchTransactions().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(4, testObserver.value().data?.size)

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

        val testObserver = aggregation.fetchTransactions(longArrayOf(101,103)).test()
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

        val testObserver = aggregation.fetchTransactionsByAccountId(accountId = 1).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(3, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testRefreshTransactions() {
        initSetup()

        val body = readStringFromJson(app, R.raw.transactions_2018_08_01_valid)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == "${AggregationAPI.URL_TRANSACTIONS}?from_date=2018-06-01&to_date=2018-08-08") {
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
            assertEquals(179, models?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals("${AggregationAPI.URL_TRANSACTIONS}?from_date=2018-06-01&to_date=2018-08-08", request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshTransactionByID() {
        initSetup()

        val body = readStringFromJson(app, R.raw.transaction_id_99703)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == "${NetworkHelper.API_VERSION_PATH}/aggregation/transactions/99703") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        aggregation.refreshTransaction(99703L) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchTransaction(99703L).test()
            testObserver.awaitValue()
            val model = testObserver.value().data
            assertNotNull(model)
            assertEquals(99703L, model?.transactionId)
        }

        val request = mockServer.takeRequest()
        assertEquals("${NetworkHelper.API_VERSION_PATH}/aggregation/transactions/99703", request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testRefreshTransactionsByIds() {
        initSetup()

        val body = readStringFromJson(app, R.raw.transactions_2018_08_01_valid)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == "${AggregationAPI.URL_TRANSACTIONS}?transaction_ids=1,2,3,4,5") {
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
            assertEquals(179, models?.size)
        }

        val request = mockServer.takeRequest()
        assertEquals("${AggregationAPI.URL_TRANSACTIONS}?transaction_ids=1,2,3,4,5", request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testUpdateTransaction() {
        initSetup()

        val body = readStringFromJson(app, R.raw.transaction_id_99703)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == "${NetworkHelper.API_VERSION_PATH}/aggregation/transactions/99703") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val transaction = testTransactionResponseData().toTransaction()

        aggregation.updateTransaction(99703, transaction) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = aggregation.fetchTransactions().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(1, models?.size)
            assertEquals(99703L, models?.get(0)?.transactionId)
            assertEquals(543L, models?.get(0)?.accountId)
        }

        val request = mockServer.takeRequest()
        assertEquals("${NetworkHelper.API_VERSION_PATH}/aggregation/transactions/99703", request.path)

        wait(3)

        tearDown()
    }

    @Test
    fun testFetchTransactionsSummary() {
        initSetup()

        val body = readStringFromJson(app, R.raw.transactions_summary_valid)
        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == "${AggregationAPI.URL_TRANSACTIONS_SUMMARY}?from_date=2018-06-01&to_date=2018-08-08") {
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
        assertEquals("${AggregationAPI.URL_TRANSACTIONS_SUMMARY}?from_date=2018-06-01&to_date=2018-08-08", request.path)

        wait(3)

        tearDown()
    }
}