package us.frollo.frollosdk

import android.app.Application
import timber.log.Timber
import us.frollo.frollosdk.auth.Authentication
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.di.Injector
import us.frollo.frollosdk.error.FrolloSDKError

object FrolloSDK {

    private var setup = false
    private lateinit var authentication: Authentication

    internal lateinit var serverUrl: String
    internal lateinit var app: Application

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun setup(application: Application, params: SetupParams, callback: ((FrolloSDKError?) -> Unit)) {
        if (setup) throw IllegalStateException("SDK already setup")
        if (params.serverUrl.isBlank()) throw IllegalArgumentException("Server URL cannot be empty")

        this.app = application
        serverUrl = params.serverUrl

        registerTimber()
        initializeDagger(app)

        authentication = Authentication()

        setup = true
        callback(null)
    }

    private fun initializeDagger(app: Application) {
        Injector.buildComponent(app)
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