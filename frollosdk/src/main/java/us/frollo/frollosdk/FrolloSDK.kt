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
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.version.Version

object FrolloSDK {

    val setup: Boolean
        get() = _setup

    val authentication: Authentication
        get() =_authentication ?: throw IllegalAccessException("SDK not setup")

    private var _setup = false
    private var _authentication: Authentication? = null
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

        preferences = Preferences(application.applicationContext)
        database = SDKDatabase.getInstance(application)
        version = Version(preferences)
        network = NetworkService(SystemInfo(application))
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
}