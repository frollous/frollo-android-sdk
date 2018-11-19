package us.frollo.frollosdk

import android.app.Application
import timber.log.Timber
import us.frollo.frollosdk.auth.Authentication
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.core.SystemInfo
import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.error.FrolloSDKError

object FrolloSDK {

    private var setup = false
    private lateinit var network: NetworkService
    private lateinit var authentication: Authentication

    internal lateinit var app: Application
    internal lateinit var serverUrl: String

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun setup(application: Application, params: SetupParams, callback: ((FrolloSDKError?) -> Unit)) {
        registerTimber()

        if (setup) throw IllegalStateException("SDK already setup")
        if (params.serverUrl.isBlank()) throw IllegalArgumentException("Server URL cannot be empty")

        this.app = application
        serverUrl = params.serverUrl

        network = NetworkService(SystemInfo(application))
        authentication = Authentication(DeviceInfo(application.applicationContext), network)

        setup = true
        callback(null)
    }

    private fun registerTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    @Throws(IllegalAccessException::class)
    fun getAuthentication(): Authentication {
        if (setup) {
            return authentication
        } else {
            throw IllegalAccessException("SDK not setup")
        }
    }
}