package us.frollo.frollosdk

import android.app.Application
import android.os.Handler
import androidx.core.os.bundleOf
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import us.frollo.frollosdk.aggregation.Aggregation
import us.frollo.frollosdk.auth.Authentication
import us.frollo.frollosdk.auth.AuthenticationStatus
import us.frollo.frollosdk.core.ACTION.ACTION_AUTHENTICATION_CHANGED
import us.frollo.frollosdk.core.ARGUMENT.ARG_AUTHENTICATION_STATUS
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.data.local.SDKDatabase
import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.events.Events
import us.frollo.frollosdk.extensions.notify
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.messages.Messages
import us.frollo.frollosdk.notifications.Notifications
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.version.Version
import java.lang.Exception
import java.util.*

/**
 * Frollo SDK manager and main instantiation. Responsible for managing the lifecycle and coordination of the SDK
 */
object FrolloSDK {

    private const val TAG = "FrolloSDK"
    private const val CACHE_EXPIRY = 120000L // 2 minutes

    /**
     * Indicates if the SDK has completed setup or not
     */
    val isSetup: Boolean
        get() = _setup

    /**
     * Authentication - All authentication and user related data see [Authentication] for details
     */
    val authentication: Authentication
        get() =_authentication ?: throw IllegalAccessException("SDK not setup")

    /**
     * Aggregation - All account and transaction related data see [Aggregation] for details
     */
    val aggregation: Aggregation
        get() =_aggregation ?: throw IllegalAccessException("SDK not setup")

    /**
     * Messages - All messages management. See [Messages] for details
     */
    val messages: Messages
        get() =_messages ?: throw IllegalAccessException("SDK not setup")

    /**
     * Events - Triggering and handling of events. See [Events] for details
     */
    val events: Events
        get() =_events ?: throw IllegalAccessException("SDK not setup")

    /**
     * Notifications - Registering and handling of push notifications
     */
    val notifications: Notifications
        get() =_notifications ?: throw IllegalAccessException("SDK not setup")

    private var _setup = false
    private var _authentication: Authentication? = null
    private var _aggregation: Aggregation? = null
    private var _messages: Messages? = null
    private var _events: Events? = null
    private var _notifications: Notifications? = null
    private lateinit var keyStore: Keystore
    private lateinit var preferences: Preferences
    private lateinit var version: Version
    private lateinit var network: NetworkService
    private lateinit var database: SDKDatabase
    internal var refreshTimer: Timer? = null
        private set
    private var deviceLastUpdated: LocalDateTime? = null

    internal lateinit var app: Application

    /**
     * Setup the SDK
     *
     * Sets up the SDK for use by performing any database migrations or other underlying setup needed. Must be called and completed before using the SDK.
     *
     * @param application Application instance of the client app.
     * @param setupParams Parameters to configure SDK setup. See [SetupParams] for details.
     * @param completion Completion handler with optional error if something goes wrong during the setup process.
     *
     * @throws FrolloSDKError if SDK is already setup or Server URL is empty.
     */
    @Throws(FrolloSDKError::class)
    fun setup(application: Application, setupParams: SetupParams, completion: OnFrolloSDKCompletionListener) {
        this.app = application

        if (_setup) throw FrolloSDKError("SDK already setup")
        if (setupParams.serverUrl.isBlank()) throw FrolloSDKError("Server URL cannot be empty")

        try {
            // 1. Initialize ThreeTenABP
            initializeThreeTenABP()
            // 2. Setup Keystore
            keyStore = Keystore()
            keyStore.setup()
            // 3. Setup Preferences
            preferences = Preferences(application.applicationContext)
            // 4. Setup Database
            database = SDKDatabase.getInstance(application)
            // 5. Setup Version Manager
            version = Version(preferences)
            // 6. Setup Network Stack
            network = NetworkService(setupParams.serverUrl, keyStore, preferences)
            // 7. Setup Logger
            Log.network = network // Initialize Log.network before Log.logLevel as Log.logLevel is dependant on Log.network
            Log.logLevel = setupParams.logLevel
            // 8. Setup Authentication
            _authentication = Authentication(DeviceInfo(application.applicationContext), network, database, preferences)
            // 9. Setup Aggregation
            _aggregation = Aggregation(network, database)
            // 10. Setup Messages
            _messages = Messages(network, database)
            // 11. Setup Events
            _events = Events(network)
            // 12. Setup Notifications
            _notifications = Notifications(authentication, events, messages)

            if (version.migrationNeeded()) {
                version.migrateVersion()
            }

            _setup = true
            completion.invoke(null)
        } catch (e: Exception) {
            completion.invoke(FrolloSDKError("Setup failed : ${e.message}"))
        }
    }

    /**
     * Logout the currently authenticated user from Frollo backend. Resets all caches, preferences and databases.
     * This revokes the refresh token for the current device if not already revoked and resets the token storage.
     *
     * @param completion Completion handler with optional error if something goes wrong during the logout process
     */
    fun logout(completion: OnFrolloSDKCompletionListener? = null) {
        authentication.logoutUser {
            reset(completion)
        }
    }

    /**
     * Delete the user account and complete logout activities on success
     *
     * @param completion Completion handler with any error that occurred
     */
    fun deleteUser(completion: OnFrolloSDKCompletionListener? = null) {
        authentication.deleteUser { error ->
            if (error != null) completion?.invoke(error)
            else reset(completion)
        }
    }

    /**
     * Reset the SDK. Clears all caches, databases and preferences. Called automatically from logout.
     *
     * @param completion Completion handler with option error if something goes wrong (optional)
     *
     * @throws IllegalAccessException if SDK is not setup
     */
    @Throws(IllegalAccessException::class)
    fun reset(completion: OnFrolloSDKCompletionListener? = null) {
        if (!_setup) throw IllegalAccessException("SDK not setup")

        pauseScheduledRefreshing()
        // NOTE: Keystore reset is not required as we do not store any data in there. Just keys.
        authentication.reset()
        preferences.reset()
        database.clearAllTables()
        completion?.invoke(null)

        notify(ACTION_AUTHENTICATION_CHANGED,
                bundleOf(Pair(ARG_AUTHENTICATION_STATUS, AuthenticationStatus.LOGGED_OUT)))
    }

    private fun initializeThreeTenABP() {
        AndroidThreeTen.init(app)
    }

    /**
     * Application entered the background.
     *
     * Notify the SDK of an app lifecycle change. Call this to ensure proper refreshing of cache data occurs when the app enters background or resumes.
     */
    fun onAppBackgrounded() {
        pauseScheduledRefreshing()
    }

    /**
     * Application resumed from background
     *
     * Notify the SDK of an app lifecycle change. Call this to ensure proper refreshing of cache data occurs when the app enters background or resumes.
     */
    fun onAppForegrounded() {
        resumeScheduledRefreshing()

        // Update device timezone, name and IDs regularly
        val now = LocalDateTime.now()

        var updateDevice = true

        deviceLastUpdated?.let { lastUpdated ->
            val time = Duration.between(lastUpdated, now).toMillis()
            if (time < CACHE_EXPIRY) {
                updateDevice = false
            }
        }

        if (updateDevice) {
            deviceLastUpdated = now

            authentication.updateDevice()
        }
    }

    /**
     * Refreshes all cached data in an optimised way. Fetches most urgent data first and then proceeds to update other caches if needed.
     */
    fun refreshData() {
        refreshPrimary()
        Handler().postDelayed({ refreshSecondary() }, 3000)
        Handler().postDelayed({ refreshSystem() }, 20000)

        resumeScheduledRefreshing()
    }

    /**
     * Refresh data from the most time sensitive and important APIs, e.g. accounts, transactions
     */
    private fun refreshPrimary() {
        aggregation.refreshProviderAccounts()
        aggregation.refreshAccounts()
        //TODO: Refresh Transactions
        authentication.refreshUser()
        messages.refreshUnreadMessages()
    }

    /**
     * Refresh data from other important APIs that frequently change but are less time sensitive, e.g. bill payments
     */
    private fun refreshSecondary() {
        //TODO: Refresh Bill Payments
    }

    /**
     * Refresh data from long lived sources which don't change often, e.g. transaction categories, providers
     */
    private fun refreshSystem() {
        aggregation.refreshProviders()
        //TODO: Refresh Transaction Categories
        //TODO: Refresh Merchants
        //TODO: Refresh Bills
        authentication.updateDevice()
    }

    private fun resumeScheduledRefreshing() {
        cancelRefreshTimer()

        val timerTask = object : TimerTask() {
            override fun run() {
                refreshPrimary()
            }
        }
        refreshTimer = Timer()
        refreshTimer?.schedule(
                timerTask,
                CACHE_EXPIRY, // Initial delay set to CACHE_EXPIRY minutes, as refreshData() would have already run refreshPrimary() once.
                CACHE_EXPIRY) // Repeat every CACHE_EXPIRY minutes
    }

    private fun pauseScheduledRefreshing() {
        cancelRefreshTimer()
    }

    private fun cancelRefreshTimer() {
        refreshTimer?.cancel()
        refreshTimer = null
    }

    internal fun forcedLogout() {
        if (authentication.loggedIn)
            reset()
    }
}