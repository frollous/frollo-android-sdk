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

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.jakewharton.threetenabp.AndroidThreeTen
import okhttp3.ResponseBody
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import us.frollo.frollosdk.aggregation.Aggregation
import us.frollo.frollosdk.authentication.AuthenticationStatus
import us.frollo.frollosdk.authentication.AuthenticationType.Custom
import us.frollo.frollosdk.authentication.AuthenticationType.OAuth2
import us.frollo.frollosdk.authentication.OAuth2Authentication
import us.frollo.frollosdk.authentication.OAuth2Helper
import us.frollo.frollosdk.authentication.TokenInjector
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.bills.Bills
import us.frollo.frollosdk.budgets.Budgets
import us.frollo.frollosdk.cards.Cards
import us.frollo.frollosdk.contacts.Contacts
import us.frollo.frollosdk.core.ACTION.ACTION_AUTHENTICATION_CHANGED
import us.frollo.frollosdk.core.ARGUMENT.ARG_AUTHENTICATION_STATUS
import us.frollo.frollosdk.core.AppInfo
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.core.FrolloSDKConfiguration
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.events.Events
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.notify
import us.frollo.frollosdk.extensions.toString
import us.frollo.frollosdk.goals.Goals
import us.frollo.frollosdk.images.Images
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.kyc.KYC
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.logging.LogManager
import us.frollo.frollosdk.managedproducts.ManagedProducts
import us.frollo.frollosdk.messages.Messages
import us.frollo.frollosdk.model.coredata.bills.BillPayment
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.ResponseDataAPI
import us.frollo.frollosdk.network.api.TokenAPI
import us.frollo.frollosdk.notifications.Notifications
import us.frollo.frollosdk.paydays.Paydays
import us.frollo.frollosdk.payments.Payments
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.reports.Reports
import us.frollo.frollosdk.surveys.Surveys
import us.frollo.frollosdk.user.UserManagement
import us.frollo.frollosdk.version.Version
import java.util.Timer
import java.util.TimerTask

/**
 * Frollo SDK manager and main instantiation. Responsible for managing the lifecycle and coordination of the SDK
 */
object FrolloSDK {

    private const val TAG = "FrolloSDK"
    private const val CACHE_EXPIRY = 120000L // 2 minutes
    internal const val SDK_NOT_SETUP = "SDK not setup. Reason: Either the client app crashed due to some error or SDK has not been initialized properly by the app."

    /**
     * Indicates if the SDK has completed setup or not
     */
    val isSetup: Boolean
        get() = _setup

    /**
     * Default OAuth2 Authentication - Returns the default OAuth2 based authentication if no custom one has been applied
     */
    var oAuth2Authentication: OAuth2Authentication? = null
        private set

    /**
     * Aggregation - All account and transaction related data see [Aggregation] for details
     */
    val aggregation: Aggregation
        get() = _aggregation ?: throw IllegalAccessException(SDK_NOT_SETUP)

    /**
     * Messages - All messages management. See [Messages] for details
     */
    val messages: Messages
        get() = _messages ?: throw IllegalAccessException(SDK_NOT_SETUP)

    /**
     * Events - Triggering and handling of events. See [Events] for details
     */
    val events: Events
        get() = _events ?: throw IllegalAccessException(SDK_NOT_SETUP)

    /**
     * Notifications - Registering and handling of push notifications. See [Notifications] for details
     */
    val notifications: Notifications
        get() = _notifications ?: throw IllegalAccessException(SDK_NOT_SETUP)

    /**
     * Surveys - Surveys management. See [Surveys] for details
     */
    val surveys: Surveys
        get() = _surveys ?: throw IllegalAccessException(SDK_NOT_SETUP)

    /**
     * Reports - Aggregation data reports. See [Reports] for details
     */
    val reports: Reports
        get() = _reports ?: throw IllegalAccessException(SDK_NOT_SETUP)

    /**
     * Bills - All bills and bill payments. See [Bills] for details
     */
    val bills: Bills
        get() = _bills ?: throw IllegalAccessException(SDK_NOT_SETUP)

    /**
     * Goals - Tracking and managing goals. See [Goals] for details
     */
    val goals: Goals
        get() = _goals ?: throw IllegalAccessException(SDK_NOT_SETUP)

    /**
     * Budgets - Tracking and managing budgets. See [Budgets] for details
     */
    val budgets: Budgets
        get() = _budgets ?: throw IllegalAccessException(SDK_NOT_SETUP)

    /**
     * Images - Tracking and managing images. See [Images] for details
     */
    val images: Images
        get() = _images ?: throw IllegalAccessException(SDK_NOT_SETUP)

    /**
     * User - User management. See [UserManagement] for details
     */
    val userManagement: UserManagement
        get() = _userManagement ?: throw IllegalAccessException(SDK_NOT_SETUP)

    /**
     * Payments - Managing payments. See [Payments] for details
     */
    val payments: Payments
        get() = _payments ?: throw IllegalAccessException(SDK_NOT_SETUP)

    /**
     * Contacts - Managing contacts. See [Contacts] for details
     */
    val contacts: Contacts
        get() = _contacts ?: throw IllegalAccessException(SDK_NOT_SETUP)

    /**
     * KYC - Managing user KYC. See [KYC] for details
     */
    val kyc: KYC
        get() = _kyc ?: throw IllegalAccessException(SDK_NOT_SETUP)

    /**
     * Managed Products - Manages Products. See [ManagedProducts] for details
     */
    val managedProducts: ManagedProducts
        get() = _managedProducts ?: throw IllegalAccessException(SDK_NOT_SETUP)

    /**
     * Cards - Managing all aspects of cards. See [Cards] for details
     */
    val cards: Cards
        get() = _cards ?: throw IllegalAccessException(SDK_NOT_SETUP)

    /**
     * Paydays - Managing all aspects of payday. See [Paydays] for details
     */
    val paydays: Paydays
        get() = _paydays ?: throw IllegalAccessException(SDK_NOT_SETUP)

    /**
     * Log Manager - Manages logging to the host. See [LogManager] for details
     */
    val logger: LogManager
        get() = _logger ?: throw IllegalAccessException(SDK_NOT_SETUP)

    private var _setup = false
    private var _logger: LogManager? = null
    private var _aggregation: Aggregation? = null
    private var _messages: Messages? = null
    private var _events: Events? = null
    private var _notifications: Notifications? = null
    private var _surveys: Surveys? = null
    private var _reports: Reports? = null
    private var _bills: Bills? = null
    private var _goals: Goals? = null
    private var _budgets: Budgets? = null
    private var _images: Images? = null
    private var _userManagement: UserManagement? = null
    private var _payments: Payments? = null
    private var _contacts: Contacts? = null
    private var _kyc: KYC? = null
    private var _managedProducts: ManagedProducts? = null
    private var _cards: Cards? = null
    private var _paydays: Paydays? = null
    private lateinit var keyStore: Keystore
    private lateinit var preferences: Preferences
    private lateinit var version: Version
    internal lateinit var network: NetworkService
    private lateinit var database: SDKDatabase
    private var tokenInjector: TokenInjector? = null
    internal var refreshTimer: Timer? = null
        private set
    private var deviceLastUpdated: LocalDateTime? = null
    private var responseDataAPI: ResponseDataAPI? = null

    internal lateinit var context: Context

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
    fun setup(configuration: FrolloSDKConfiguration, completion: OnFrolloSDKCompletionListener<Result>) {

        if (_setup) throw FrolloSDKError("SDK already setup")
        if (configuration.serverUrl.isBlank()) throw FrolloSDKError("Server URL cannot be empty")

        val localBroadcastManager = LocalBroadcastManager.getInstance(context)

        try {
            val deviceInfo = DeviceInfo(context)

            // 1. Initialize ThreeTenABP
            initializeThreeTenABP()

            // 2. Setup Keystore
            keyStore = Keystore()
            keyStore.setup()

            // 3. Setup Preferences
            preferences = Preferences(context)
            tokenInjector = TokenInjector(keyStore, preferences)

            // 4. Setup Database
            database = SDKDatabase.getInstance(context)

            // 5. Setup Version Manager
            version = Version(preferences)

            // 6. Setup Network Stack
            val oAuth = OAuth2Helper(config = configuration)
            network = NetworkService(oAuth2Helper = oAuth, keystore = keyStore, pref = preferences, appInfo = AppInfo(context))

            // 7. Setup Logger
            // Initialize Log.network, Log.deviceId, Log.deviceName and Log.deviceType
            // before Log.logLevel as Log.logLevel is dependant on Log.network
            Log.network = network
            Log.deviceId = deviceInfo.deviceId
            Log.deviceName = deviceInfo.deviceName
            Log.deviceType = deviceInfo.deviceType
            Log.logLevel = configuration.logLevel
            // Setup Log Manager
            _logger = LogManager()

            // 8. Setup authentication stack
            when (configuration.authenticationType) {
                is Custom -> {
                    network.accessTokenProvider = configuration.authenticationType.accessTokenProvider
                    network.authenticationCallback = configuration.authenticationType.authenticationCallback
                }
                is OAuth2 -> {
                    oAuth2Authentication = OAuth2Authentication(oAuth, preferences).apply {
                        tokenAPI = network.createAuth(TokenAPI::class.java)
                        revokeTokenAPI = network.createRevoke(TokenAPI::class.java)
                        authToken = network.authToken
                    }
                    network.accessTokenProvider = oAuth2Authentication
                    network.authenticationCallback = oAuth2Authentication
                }
            }

            // 9. Setup Aggregation
            _aggregation = Aggregation(network, database, localBroadcastManager)

            // 10. Setup Messages
            _messages = Messages(network, database)

            // 11. Setup Events
            _events = Events(network)

            // 12. Setup Surveys
            _surveys = Surveys(network)

            // 13. Setup Reports
            _reports = Reports(network, database, aggregation)

            // 14. Setup Bills
            _bills = Bills(network, database, aggregation)

            // 15. Setup Goals
            _goals = Goals(network, database)

            // 16. Setup User Management
            _userManagement = UserManagement(deviceInfo, network, configuration.clientId, database, preferences)

            // 17. Setup Notifications
            _notifications = Notifications(userManagement, events, messages)

            // 18. Setup Budgets
            _budgets = Budgets(network, database)

            // 19. Setup Images
            _images = Images(network, database)

            // 20. Setup Payments
            _payments = Payments(network)

            // 21. Setup Response Data API Service
            responseDataAPI = network.create(ResponseDataAPI::class.java)

            // 22. Setup Contacts
            _contacts = Contacts(network, database)

            // 23. Setup KYC
            _kyc = KYC(network)

            // 24. Setup Managed Products
            _managedProducts = ManagedProducts(network)

            // 25. Setup Cards
            _cards = Cards(network, database)

            // 26. Setup Paydays
            _paydays = Paydays(network, database)

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
     * Fetch Raw data response with an authenticated request
     */
    fun downloadResponseData(url: String, completion: OnFrolloSDKCompletionListener<Resource<ResponseBody>>) {
        responseDataAPI?.fetchResponseData(url)?.enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    completion.invoke(resource)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#downloadResponseData", resource.error?.localizedDescription)
                    completion.invoke(resource)
                }
            }
        }
    }

    /**
     * Get Token Injector. See [TokenInjector] for details
     */
    fun getTokenInjector(): TokenInjector =
        tokenInjector ?: throw IllegalAccessException(SDK_NOT_SETUP)

    /**
     * Reset the SDK. Clears all caches, databases, tokens, keystore and preferences.
     *
     * @param completion Completion handler with option error if something goes wrong (optional)
     *
     * @throws IllegalAccessException if SDK is not setup
     */
    @Throws(IllegalAccessException::class)
    fun reset(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        internalReset(completion)
    }

    /**
     * Internal SDK reset
     *
     * Triggers the internal cleanup of the SDK. Called from public logout/reset methods and also forced logout
     */
    private fun internalReset(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!_setup) throw IllegalAccessException(SDK_NOT_SETUP)

        pauseScheduledRefreshing()
        // NOTE: Keystore reset is not required as we do not store any data in there. Just keys.
        oAuth2Authentication?.reset()
        network.reset()
        preferences.reset()
        database.clearAllTables()
        completion?.invoke(Result.success())

        notify(
            action = ACTION_AUTHENTICATION_CHANGED,
            extrasKey = ARG_AUTHENTICATION_STATUS,
            extrasData = AuthenticationStatus.LOGGED_OUT
        )
    }

    private fun initializeThreeTenABP() {
        AndroidThreeTen.init(context)
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
        if (!isSetup || network.accessTokenProvider?.accessToken?.token != null)
            return

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
        Handler(Looper.getMainLooper()).postDelayed({ refreshSecondary() }, 3000)
        Handler(Looper.getMainLooper()).postDelayed({ refreshSystem() }, 20000)

        resumeScheduledRefreshing()
    }

    /**
     * Refresh data from the most time sensitive and important APIs, e.g. accounts, transactions
     */
    private fun refreshPrimary() {
        aggregation.refreshProviderAccounts()
        aggregation.refreshAccounts()
        aggregation.refreshTransactionsWithPagination()
        aggregation.refreshConsents()
        userManagement.refreshUser()
        messages.refreshUnreadMessages()
        budgets.refreshBudgets()
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
        cards.refreshCards()
    }

    /**
     * Refresh data from long lived sources which don't change often, e.g. transaction categories, providers
     */
    private fun refreshSystem() {
        aggregation.refreshProviders()
        aggregation.refreshTransactionCategories()
        aggregation.refreshCachedMerchants()
        bills.refreshBills()
        userManagement.updateDevice()
        images.refreshImages()
        contacts.refreshContactsWithPagination()
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
            CACHE_EXPIRY
        ) // Repeat every CACHE_EXPIRY minutes
    }

    private fun pauseScheduledRefreshing() {
        cancelRefreshTimer()
    }

    private fun cancelRefreshTimer() {
        refreshTimer?.cancel()
        refreshTimer = null
    }
}
