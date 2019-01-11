package us.frollo.frollosdk

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber
import us.frollo.frollosdk.auth.Authentication
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.data.local.SDKDatabase
import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.version.Version

object FrolloSDK {

    val isSetup: Boolean
        get() = _setup

    val authentication: Authentication
        get() =_authentication ?: throw IllegalAccessException("SDK not setup")

    private var _setup = false
    private var _authentication: Authentication? = null
    private lateinit var keyStore: Keystore
    private lateinit var preferences: Preferences
    private lateinit var version: Version
    private lateinit var network: NetworkService
    private lateinit var database: SDKDatabase

    internal lateinit var app: Application

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun setup(application: Application, params: SetupParams, callback: ((FrolloSDKError?) -> Unit)) {
        registerTimber()

        if (_setup) throw IllegalStateException("SDK already setup")
        if (params.serverUrl.isBlank()) throw IllegalArgumentException("Server URL cannot be empty")

        this.app = application

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

        if (version.migrationNeeded()) {
            version.migrateVersion()
        }

        _setup = true
        callback(null)
    }

    private fun registerTimber() {
        // TODO: May be handle more levels during Logging task
        Timber.plant(Timber.DebugTree())
    }

    private fun initializeThreeTenABP() {
        AndroidThreeTen.init(app)
    }

    fun logout() {
        authentication.logoutUser()
        reset()
    }

    internal fun forcedLogout() {
        //if (authentication.loggedIn) // TODO: This is creating problem for tests. Refactor and enable later.
            reset()
    }

    fun reset() {
        // TODO: Pause scheduled refreshing
        // NOTE: Keystore reset is not required as we do not store any data in there. Just keys.
        authentication.reset()
        preferences.reset()
        database.reset()
        // TODO: Need to send any notify anything?
    }
}