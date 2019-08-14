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

package us.frollo.frollosdk

import android.app.Application
import android.os.Handler
import androidx.core.os.bundleOf
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.TemporalAdjusters
import us.frollo.frollosdk.aggregation.Aggregation
import us.frollo.frollosdk.authentication.Authentication
import us.frollo.frollosdk.authentication.AuthenticationCallback
import us.frollo.frollosdk.authentication.AuthenticationStatus
import us.frollo.frollosdk.authentication.AuthenticationType.Custom
import us.frollo.frollosdk.authentication.AuthenticationType.OAuth2
import us.frollo.frollosdk.authentication.OAuth2Helper
import us.frollo.frollosdk.authentication.OAuth2Authentication
import us.frollo.frollosdk.authentication.TokenInjector
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.bills.Bills
import us.frollo.frollosdk.core.ACTION.ACTION_AUTHENTICATION_CHANGED
import us.frollo.frollosdk.core.ARGUMENT.ARG_AUTHENTICATION_STATUS
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.core.FrolloSDKConfiguration
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.events.Events
import us.frollo.frollosdk.extensions.notify
import us.frollo.frollosdk.extensions.toString
import us.frollo.frollosdk.goals.Goals
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.messages.Messages
import us.frollo.frollosdk.model.coredata.aggregation.transactions.Transaction
import us.frollo.frollosdk.model.coredata.bills.BillPayment
import us.frollo.frollosdk.network.api.TokenAPI
import us.frollo.frollosdk.notifications.Notifications
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.reports.Reports
import us.frollo.frollosdk.surveys.Surveys
import us.frollo.frollosdk.user.UserManagement
import us.frollo.frollosdk.version.Version
import java.lang.Exception
import java.util.Timer
import java.util.TimerTask

/**
 * Frollo SDK manager and main instantiation. Responsible for managing the lifecycle and coordination of the SDK
 */
object FrolloSDK : AuthenticationCallback {

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
        get() = _authentication ?: throw IllegalAccessException("SDK not setup")

    /**
     * Default OAuth2 Authentication - Returns the default OAuth2 based authentication if no custom one has been applied
     */
    val defaultAuthentication: OAuth2Authentication?
        get() = _authentication as? OAuth2Authentication

    /**
     * Aggregation - All account and transaction related data see [Aggregation] for details
     */
    val aggregation: Aggregation
        get() = _aggregation ?: throw IllegalAccessException("SDK not setup")

    /**
     * Messages - All messages management. See [Messages] for details
     */
    val messages: Messages
        get() = _messages ?: throw IllegalAccessException("SDK not setup")

    /**
     * Events - Triggering and handling of events. See [Events] for details
     */
    val events: Events
        get() = _events ?: throw IllegalAccessException("SDK not setup")

    /**
     * Notifications - Registering and handling of push notifications. See [Notifications] for details
     */
    val notifications: Notifications
        get() = _notifications ?: throw IllegalAccessException("SDK not setup")

    /**
     * Surveys - Surveys management. See [Surveys] for details
     */
    val surveys: Surveys
        get() = _surveys ?: throw IllegalAccessException("SDK not setup")

    /**
     * Reports - Aggregation data reports. See [Reports] for details
     */
    val reports: Reports
        get() = _reports ?: throw IllegalAccessException("SDK not setup")

    /**
     * Bills - All bills and bill payments. See [Bills] for details
     */
    val bills: Bills
        get() = _bills ?: throw IllegalAccessException("SDK not setup")

    /**
     * Goals - Tracking and managing goals. See [Goals] for details
     */
    val goals: Goals
        get() = _goals ?: throw IllegalAccessException("SDK not setup")

    /**
     * User - User management. See [UserManagement] for details
     */
    val userManagement: UserManagement
        get() = _userManagement ?: throw IllegalAccessException("SDK not setup")

    private var _setup = false
    private var _authentication: Authentication? = null
    private var _aggregation: Aggregation? = null
    private var _messages: Messages? = null
    private var _events: Events? = null
    private var _notifications: Notifications? = null
    private var _surveys: Surveys? = null
    private var _reports: Reports? = null
    private var _bills: Bills? = null
    private var _goals: Goals? = null
    private var _userManagement: UserManagement? = null
    private lateinit var keyStore: Keystore
    private lateinit var preferences: Preferences
    private lateinit var version: Version
    private lateinit var network: NetworkService
    private lateinit var database: SDKDatabase
    private var tokenInjector: TokenInjector? = null
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
     * @param configuration Configuration and preferences needed to setup the SDK. See [FrolloSDKConfiguration] for details.
     * @param completion Completion handler with optional error if something goes wrong during the setup process.
     *
     * @throws FrolloSDKError if SDK is already setup or Server URL is empty.
     */
    @Throws(FrolloSDKError::class)
    fun setup(application: Application, configuration: FrolloSDKConfiguration, completion: OnFrolloSDKCompletionListener<Result>) {
        this.app = application

        if (_setup) throw FrolloSDKError("SDK already setup")
        if (configuration.serverUrl.isBlank()) throw FrolloSDKError("Server URL cannot be empty")

        val localBroadcastManager = LocalBroadcastManager.getInstance(app)

        try {
            val deviceInfo = DeviceInfo(application.applicationContext)

            // 1. Initialize ThreeTenABP
            initializeThreeTenABP()

            // 2. Setup Keystore
            keyStore = Keystore()
            keyStore.setup()

            // 3. Setup Preferences
            preferences = Preferences(application.applicationContext)
            tokenInjector = TokenInjector(keyStore, preferences)

            // 4. Setup Database
            database = SDKDatabase.getInstance(application)

            // 5. Setup Version Manager
            version = Version(preferences)

            // 6. Setup Network Stack
            val oAuth = OAuth2Helper(config = configuration)
            network = NetworkService(oAuth2Helper = oAuth, keystore = keyStore, pref = preferences)

            // 7. Setup Logger
            // Initialize Log.network, Log.deviceId, Log.deviceName and Log.deviceType
            // before Log.logLevel as Log.logLevel is dependant on Log.network
            Log.network = network
            Log.deviceId = deviceInfo.deviceId
            Log.deviceName = deviceInfo.deviceName
            Log.deviceType = deviceInfo.deviceType
            Log.logLevel = configuration.logLevel

            // 8. Setup authentication stack
            when (configuration.authenticationType) {
                is Custom -> {
                    configuration.authenticationType.authentication.authenticationCallback = this
                    configuration.authenticationType.authentication.tokenCallback = network
                    _authentication = configuration.authenticationType.authentication
                }
                is OAuth2 -> {
                    _authentication = OAuth2Authentication(oAuth, preferences, authenticationCallback = this, tokenCallback = network).apply {
                        tokenAPI = network.createAuth(TokenAPI::class.java)
                        revokeTokenAPI = network.createRevoke(TokenAPI::class.java)
                        authToken = network.authToken
                    }
                }
            }
            network.authentication = _authentication

            // 9. Setup Aggregation
            _aggregation = Aggregation(network, database, localBroadcastManager, authentication)

            // 10. Setup Messages
            _messages = Messages(network, database, authentication)

            // 11. Setup Events
            _events = Events(network, authentication)

            // 12. Setup Surveys
            _surveys = Surveys(network, authentication)

            // 13. Setup Reports
            _reports = Reports(network, database, aggregation, authentication)

            // 14. Setup Bills
            _bills = Bills(network, database, aggregation, authentication)

            // 15. Setup Goals
            _goals = Goals(network, database, authentication)

            // 16. Setup User Management
            _userManagement = UserManagement(deviceInfo, network, configuration.clientId, database, preferences, authentication, authenticationCallback = this)

            // 17. Setup Notifications
            _notifications = Notifications(userManagement, events, messages)

            if (version.migrationNeeded()) {
                version.migrateVersion()
            }

            _setup = true
            completion.invoke(Result.success())
        } catch (e: Exception) {
            val error = FrolloSDKError("Setup failed : ${e.message}")
            completion.invoke(Result.error(error))
        }
    }

    /**
     * Initialize authentication callback and token callback when using Custom Authentication
     */
    fun initializeAuthenticationCallbacks(authentication: Authentication) {
        authentication.authenticationCallback = this
        authentication.tokenCallback = network
    }

    /**
     * Get Token Injector. See [TokenInjector] for details
     */
    fun getTokenInjector(): TokenInjector =
            tokenInjector ?: throw IllegalAccessException("SDK not setup")

    /**
     * Logout and reset the SDK.
     *
     * Calls [Authentication.logout] to logout the user remotely if possible then resets the SDK
     *
     * @param completion Completion handler with option error if something goes wrong (optional)
     *
     * See also [FrolloSDK.reset]
     */
    fun logout(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        authentication.logout()
        internalReset(completion)
    }

    /**
     * Reset the SDK. Clears all caches, databases, tokens, keystore and preferences.
     *
     * @param completion Completion handler with option error if something goes wrong (optional)
     *
     * @throws IllegalAccessException if SDK is not setup
     */
    @Throws(IllegalAccessException::class)
    fun reset(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        authentication.reset() // WARNING: Do not put this inside internalReset() as the reset() call goes to infinite loop if put inside.
        internalReset(completion)
    }

    /**
     * Notifies the SDK that authentication of the user is no longer valid and to reset itself
     *
     * This should not be called directly. Instead use [FrolloSDK.reset]
     *
     * This should be called when the user's authentication is no longer valid and no possible automated
     * re-authentication can be performed. For example when a refresh token has been revoked so no more
     * access tokens can be obtained.
     *
     * @param completion Completion handler with option error if something goes wrong (optional)
     */
    override fun authenticationReset(completion: OnFrolloSDKCompletionListener<Result>?) {
        if (authentication.loggedIn) { // WARNING: This check is important. Without this the authentication.reset() call goes to infinite loop.
            reset(completion)
        }
    }

    /**
     * Internal SDK reset
     *
     * Triggers the internal cleanup of the SDK. Called from public logout/reset methods and also forced logout
     */
    private fun internalReset(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!_setup) throw IllegalAccessException("SDK not setup")

        pauseScheduledRefreshing()
        // NOTE: Keystore reset is not required as we do not store any data in there. Just keys.
        network.reset()
        preferences.reset()
        database.clearAllTables()
        completion?.invoke(Result.success())

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

            userManagement.updateDevice()
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
        aggregation.refreshTransactions(
                fromDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).toString(Transaction.DATE_FORMAT_PATTERN),
                toDate = LocalDate.now().toString(Transaction.DATE_FORMAT_PATTERN))
        userManagement.refreshUser()
        messages.refreshUnreadMessages()
    }

    /**
     * Refresh data from other important APIs that frequently change but are less time sensitive, e.g. bill payments
     */
    private fun refreshSecondary() {
        val now = LocalDate.now()
        // To end of current month
        val toDate = now.withDayOfMonth(now.lengthOfMonth()).toString(BillPayment.DATE_FORMAT_PATTERN)
        // From start of last month
        val fromDate = now.minusMonths(1).withDayOfMonth(1).toString(BillPayment.DATE_FORMAT_PATTERN)
        bills.refreshBillPayments(fromDate = fromDate, toDate = toDate)
        goals.refreshGoals()
    }

    /**
     * Refresh data from long lived sources which don't change often, e.g. transaction categories, providers
     */
    private fun refreshSystem() {
        aggregation.refreshProviders()
        aggregation.refreshTransactionCategories()
        bills.refreshBills()
        userManagement.updateDevice()
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
        authenticationReset()
    }
}