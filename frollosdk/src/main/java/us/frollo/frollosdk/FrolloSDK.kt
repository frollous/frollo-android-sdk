package us.frollo.frollosdk

import android.app.Application
import timber.log.Timber
import us.frollo.frollosdk.auth.Authentication
import us.frollo.frollosdk.core.SdkError
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.di.Injector

object FrolloSDK {

    private var setup = false
    private lateinit var authentication: Authentication

    lateinit var serverUrl: String

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun setup(app: Application, params: SetupParams, callback: ((SdkError?) -> Unit)) {
        if (setup) throw IllegalStateException("SDK already setup")
        if (params.serverUrl.isBlank()) throw IllegalArgumentException("Server URL cannot be empty")

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