package us.frollo.frollosdk.data.remote

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.data.remote.api.TokenAPI
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.preferences.Preferences

class NetworkInterceptorTest {

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    private lateinit var mockServer: MockWebServer
    private lateinit var keystore: Keystore
    private lateinit var preferences: Preferences
    private lateinit var network: NetworkService

    @Before
    fun setUp() {
        mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url(TokenAPI.URL_TOKEN_REFRESH)

        FrolloSDK.app = app
        keystore = Keystore()
        keystore.setup()
        preferences = Preferences(app)
        network = NetworkService(baseUrl.toString(), keystore, preferences)
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
        preferences.reset()
    }

    @Test
    fun testRequestHeaders() {

    }

    @Test
    fun testInvalidDomainRaisesNetworkError() {

    }
}