package us.frollo.frollosdk

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.jraska.livedata.test
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.data.local.SDKDatabase
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.model.testUserResponseData
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.testutils.wait

class FrolloSDKAndroidUnitTest {

    @get:Rule val testRule = InstantTaskExecutorRule()

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private lateinit var preferences: Preferences
    private lateinit var database: SDKDatabase
    private lateinit var mockServer: MockWebServer
    private lateinit var baseUrl: HttpUrl

    @Before
    fun resetSingletonByReflection() {
        val setup = FrolloSDK::class.java.getDeclaredField("_setup")
        setup.isAccessible = true
        setup.setBoolean(null, false)
    }

    private fun initSetup() {
        mockServer = MockWebServer()
        mockServer.start()
        baseUrl = mockServer.url("/")

        preferences = Preferences(app)
        database = SDKDatabase.getInstance(app)
    }

    private fun tearDown() {
        mockServer.shutdown()
        preferences.resetAll()
        database.clearAllTables()
    }

    @Test
    fun testSDKInitFailIfServerURLNotSet() {
        assertFalse(FrolloSDK.isSetup)

        try {
            FrolloSDK.setup(app, SetupParams.Builder().build()) { }
        } catch (e: FrolloSDKError) {
            assertEquals("Server URL cannot be empty", e.localizedMessage)
        }
    }

    @Test
    fun testSDKSetupSuccess() {
        val url = "https://api.example.com"

        assertFalse(FrolloSDK.isSetup)

        FrolloSDK.setup(app, SetupParams.Builder().serverUrl(url).build()) { error ->
            assertNull(error)

            assertTrue(FrolloSDK.isSetup)
            assertNotNull(FrolloSDK.authentication)
            assertNotNull(FrolloSDK.aggregation)
            assertNotNull(FrolloSDK.messages)
            assertNotNull(FrolloSDK.events)
            assertNotNull(FrolloSDK.notifications)
        }
    }

    @Test
    fun testSDKAuthenticationThrowsErrorBeforeSetup() {
        assertFalse(FrolloSDK.isSetup)

        try {
            FrolloSDK.authentication
        } catch (e: IllegalAccessException) {
            assertEquals("SDK not setup", e.localizedMessage)
        }
    }

    @Test
    fun testSDKAggregationThrowsErrorBeforeSetup() {
        assertFalse(FrolloSDK.isSetup)

        try {
            FrolloSDK.aggregation
        } catch (e: IllegalAccessException) {
            assertEquals("SDK not setup", e.localizedMessage)
        }
    }

    @Test
    fun testSDKMessagesThrowsErrorBeforeSetup() {
        assertFalse(FrolloSDK.isSetup)

        try {
            FrolloSDK.messages
        } catch (e: IllegalAccessException) {
            assertEquals("SDK not setup", e.localizedMessage)
        }
    }

    @Test
    fun testSDKEventsThrowsErrorBeforeSetup() {
        assertFalse(FrolloSDK.isSetup)

        try {
            FrolloSDK.events
        } catch (e: IllegalAccessException) {
            assertEquals("SDK not setup", e.localizedMessage)
        }
    }

    @Test
    fun testSDKNotificationsThrowsErrorBeforeSetup() {
        assertFalse(FrolloSDK.isSetup)

        try {
            FrolloSDK.notifications
        } catch (e: IllegalAccessException) {
            assertEquals("SDK not setup", e.localizedMessage)
        }
    }

    @Test
    fun testPauseScheduledRefresh() {
        val url = "https://api.example.com"

        FrolloSDK.setup(app, SetupParams.Builder().serverUrl(url).build()) { error ->
            assertNull(error)

            FrolloSDK.onAppBackgrounded()
            assertNull(FrolloSDK.refreshTimer)
        }
    }

    @Test
    fun testResumeScheduledRefresh() {
        val url = "https://api.example.com"

        FrolloSDK.setup(app, SetupParams.Builder().serverUrl(url).build()) { error ->
            assertNull(error)

            FrolloSDK.onAppForegrounded()
            assertNotNull(FrolloSDK.refreshTimer)
        }
    }

    @Test
    fun testRefreshData() {
        /*initSetup()

        mockServer.setDispatcher(object: Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.path == UserAPI.URL_LOGIN) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.user_details_complete))
                } else if (request?.path == UserAPI.URL_USER_DETAILS) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.user_details_complete))
                } else if (request?.path == MessagesAPI.URL_UNREAD) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(readStringFromJson(app, R.raw.messages_unread))
                }
                return MockResponse().setResponseCode(404)
            }
        })

        FrolloSDK.setup(app, SetupParams.Builder().serverUrl(baseUrl.toString()).build()) {

            FrolloSDK.authentication.loginUser(AuthType.EMAIL, "user@frollo.us", "password") { error ->
                assertNull(error)

                FrolloSDK.refreshData() //TODO: This never calls the enqueue callback to load data into database

                wait(6)

                val testObserver = FrolloSDK.authentication.fetchUser().test()
                testObserver.awaitValue()
                assertNotNull(testObserver.value().data)

                val testObserver2 = FrolloSDK.messages.fetchMessages(read = false).test()
                testObserver2.awaitValue()
                val models = testObserver2.value().data
                assertNotNull(models)
                assertEquals(7, models?.size)
            }
        }

        wait(8)

        tearDown()*/
    }

    @Test
    fun testLogout() {
        initSetup()

        val url = "https://api.example.com"

        preferences.loggedIn = true
        preferences.encryptedAccessToken = "EncryptedAccessToken"
        preferences.encryptedRefreshToken = "EncryptedRefreshToken"
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        database.users().insert(testUserResponseData())

        val testObserver = database.users().load().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())

        FrolloSDK.setup(app, SetupParams.Builder().serverUrl(url).build()) { error ->
            assertNull(error)

            FrolloSDK.logout { err ->
                assertNull(err)

                assertFalse(preferences.loggedIn)
                assertNull(preferences.encryptedAccessToken)
                assertNull(preferences.encryptedRefreshToken)
                assertEquals(-1, preferences.accessTokenExpiry)

                val testObserver2 = database.users().load().test()
                testObserver2.awaitValue()
                assertNull(testObserver2.value())
            }
        }

        wait(3)

        tearDown()
    }

    @Test
    fun testForcedLogout() {
        initSetup()

        val url = "https://api.example.com"

        preferences.loggedIn = true
        preferences.encryptedAccessToken = "EncryptedAccessToken"
        preferences.encryptedRefreshToken = "EncryptedRefreshToken"
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        database.users().insert(testUserResponseData())

        val testObserver = database.users().load().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())

        FrolloSDK.setup(app, SetupParams.Builder().serverUrl(url).build()) { error ->
            assertNull(error)

            FrolloSDK.forcedLogout()

            wait(3)

            assertFalse(preferences.loggedIn)
            assertNull(preferences.encryptedAccessToken)
            assertNull(preferences.encryptedRefreshToken)
            assertEquals(-1, preferences.accessTokenExpiry)

            val testObserver2 = database.users().load().test()
            testObserver2.awaitValue()
            assertNull(testObserver2.value())
        }

        tearDown()
    }

    @Test
    fun testSDKResetSuccess() {
        initSetup()

        val url = "https://api.example.com"

        preferences.loggedIn = true
        preferences.encryptedAccessToken = "EncryptedAccessToken"
        preferences.encryptedRefreshToken = "EncryptedRefreshToken"
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900

        database.users().insert(testUserResponseData())

        val testObserver = database.users().load().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value())

        FrolloSDK.setup(app, SetupParams.Builder().serverUrl(url).build()) { error ->
            assertNull(error)

            FrolloSDK.reset { err ->
                assertNull(err)

                assertFalse(preferences.loggedIn)
                assertNull(preferences.encryptedAccessToken)
                assertNull(preferences.encryptedRefreshToken)
                assertEquals(-1, preferences.accessTokenExpiry)

                val testObserver2 = database.users().load().test()
                testObserver2.awaitValue()
                assertNull(testObserver2.value())
            }
        }

        wait(3)

        tearDown()
    }
}