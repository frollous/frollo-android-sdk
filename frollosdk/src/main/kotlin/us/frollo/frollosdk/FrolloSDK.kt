package us.frollo.frollosdk

import android.app.Application
import android.os.Handler
import androidx.core.os.bundleOf
import com.jakewharton.threetenabp.AndroidThreeTen
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

object FrolloSDK {

    private const val TAG = "FrolloSDK"

    val isSetup: Boolean
        get() = _setup

    val authentication: Authentication
        get() =_authentication ?: throw IllegalAccessException("SDK not setup")

    val messages: Messages
        get() =_messages ?: throw IllegalAccessException("SDK not setup")

    val events: Events
        get() =_events ?: throw IllegalAccessException("SDK not setup")

    val notifications: Notifications
        get() =_notifications ?: throw IllegalAccessException("SDK not setup")

    private var _setup = false
    private var _authentication: Authentication? = null
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

    internal lateinit var app: Application

    @Throws(FrolloSDKError::class)
    fun setup(application: Application, params: SetupParams, completion: OnFrolloSDKCompletionListener) {
        this.app = application

        if (_setup) throw FrolloSDKError("SDK already setup")
        if (params.serverUrl.isBlank()) throw FrolloSDKError("Server URL cannot be empty")

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
            network = NetworkService(params.serverUrl, keyStore, preferences)
            // 7. Setup Logger
            Log.network = network // Initialize Log.network before Log.logLevel as Log.logLevel is dependant on Log.network
            Log.logLevel = params.logLevel
            // 8. Setup Authentication
            _authentication = Authentication(DeviceInfo(application.applicationContext), network, database, preferences)
            // 9. Setup Messages
            _messages = Messages(network, database)
            // 10. Setup Events
            _events = Events(network)
            // 11. Setup Notifications
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

    fun logout(completion: OnFrolloSDKCompletionListener? = null) {
        authentication.logoutUser {
            reset(completion)
        }
    }

    fun deleteUser(completion: OnFrolloSDKCompletionListener? = null) {
        authentication.deleteUser { error ->
            if (error != null) completion?.invoke(error)
            else reset(completion)
        }
    }

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

    fun onAppBackgrounded() {
        pauseScheduledRefreshing()
    }

    fun onAppForegrounded() {
        resumeScheduledRefreshing()
    }

    fun refreshData() {
        refreshPrimary()
        Handler().postDelayed({ refreshSecondary() }, 3000)
        Handler().postDelayed({ refreshSystem() }, 20000)

        resumeScheduledRefreshing()
    }

    private fun refreshPrimary() {
        //TODO: Refresh Provider Accounts
        //TODO: Refresh Accounts
        //TODO: Refresh Transactions
        authentication.refreshUser()
        messages.refreshUnreadMessages()
    }

    private fun refreshSecondary() {
        //TODO: Refresh Bill Payments
    }

    private fun refreshSystem() {
        //TODO: Refresh Providers
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
                120000, // Initial delay set to 2 minutes, as refreshData() would have already run refreshPrimary() once.
                120000) // Repeat every 2 minutes
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