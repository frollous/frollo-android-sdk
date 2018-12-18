package us.frollo.frollosdk

import android.app.Application
import timber.log.Timber
import us.frollo.frollosdk.auth.Authentication
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.core.SystemInfo
import us.frollo.frollosdk.data.local.SDKDatabase
import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.version.Version

object FrolloSDK {

    val setup: Boolean
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
    internal lateinit var serverUrl: String

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun setup(application: Application, params: SetupParams, callback: ((FrolloSDKError?) -> Unit)) {
        registerTimber()

        if (_setup) throw IllegalStateException("SDK already setup")
        if (params.serverUrl.isBlank()) throw IllegalArgumentException("Server URL cannot be empty")

        this.app = application
        serverUrl = params.serverUrl

        // 1. Setup Keystore
        keyStore = Keystore()
        keyStore.setup()
        // 2. Setup Preferences
        preferences = Preferences(application.applicationContext)
        // 3. Setup Database
        database = SDKDatabase.getInstance(application)
        // 4. Setup Version Manager
        version = Version(preferences)
        // 5. Setup Network Stack
        network = NetworkService(SystemInfo(application), keyStore, preferences)
        // 6. Setup Authentication
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

    fun logout() {
        authentication.logoutUser()
        reset()
    }

    internal fun forcedLogout() {
        reset()
    }

    fun reset() {
        // TODO: Pause scheduled refreshing
        authentication.reset()
        //keyStore.reset() // Keystore reset maybe not required
        preferences.reset()
        database.reset()
        // TODO: Need to send any notify anything?
    }
}