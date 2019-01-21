package us.frollo.frollosdk

import android.app.Application
import androidx.core.os.bundleOf
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber
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
import us.frollo.frollosdk.extensions.notify
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.messages.Messages
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.version.Version

object FrolloSDK {

    val isSetup: Boolean
        get() = _setup

    val authentication: Authentication
        get() =_authentication ?: throw IllegalAccessException("SDK not setup")

    val messages: Messages
        get() =_messages ?: throw IllegalAccessException("SDK not setup")

    private var _setup = false
    private var _authentication: Authentication? = null
    private var _messages: Messages? = null
    private lateinit var keyStore: Keystore
    private lateinit var preferences: Preferences
    private lateinit var version: Version
    private lateinit var network: NetworkService
    private lateinit var database: SDKDatabase

    internal lateinit var app: Application

    @Throws(FrolloSDKError::class)
    fun setup(application: Application, params: SetupParams, callback: ((FrolloSDKError?) -> Unit)) {
        this.app = application

        registerTimber()

        if (_setup) throw FrolloSDKError("SDK already setup")
        if (params.serverUrl.isBlank()) throw FrolloSDKError("Server URL cannot be empty")

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
        // 7. Setup Authentication
        _authentication = Authentication(DeviceInfo(application.applicationContext), network, database, preferences)
        _messages = Messages(network, database)

        if (version.migrationNeeded()) {
            version.migrateVersion()
        }

        _setup = true
        callback.invoke(null)
    }

    private fun registerTimber() {
        // TODO: May be handle more levels during Logging task
        Timber.plant(Timber.DebugTree())
    }

    private fun initializeThreeTenABP() {
        AndroidThreeTen.init(app)
    }

    fun refreshData() {
        //TODO: incomplete implementation
        refreshSystem()
    }

    private fun refreshSystem() {
        //TODO: incomplete implementation
        authentication.updateDevice()
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

    internal fun forcedLogout() {
        if (authentication.loggedIn)
            reset()
    }

    @Throws(IllegalAccessException::class)
    fun reset(completion: OnFrolloSDKCompletionListener? = null) {
        if (!_setup) throw IllegalAccessException("SDK not setup")

        // TODO: Pause scheduled refreshing
        // NOTE: Keystore reset is not required as we do not store any data in there. Just keys.
        authentication.reset()
        preferences.reset()
        database.clearAllTables()
        completion?.invoke(null)

        notify(ACTION_AUTHENTICATION_CHANGED,
                bundleOf(Pair(ARG_AUTHENTICATION_STATUS, AuthenticationStatus.LOGGED_OUT)))
    }
}