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
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.data.local.SDKDatabase
import us.frollo.frollosdk.data.remote.NetworkHelper
import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.data.remote.api.AggregationAPI
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.model.testProviderResponseData
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.test.R
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

    @Test
    fun testFetchProviderByID() {
        initSetup()

        val data = testProviderResponseData()
        val list = mutableListOf(testProviderResponseData(), data, testProviderResponseData())
        database.providers().insertAll(*list.toTypedArray())

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

        database.providers().insertAll(*list.toTypedArray())

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

        aggregation.refreshProviders { error ->
            assertNull(error)

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

        aggregation.refreshProvider(12345L) { error ->
            assertNull(error)

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
}