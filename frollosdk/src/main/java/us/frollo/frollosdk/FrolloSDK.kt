package us.frollo.frollosdk

import android.app.Application
import timber.log.Timber
import us.frollo.frollosdk.auth.Authentication
import us.frollo.frollosdk.base.api.Resource
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.di.Injector

object FrolloSDK {

    private var setup = false
    private lateinit var authentication: Authentication

    lateinit var serverUrl: String

    fun setup(app: Application, params: SetupParams, callback: ((Resource<Boolean>) -> Unit)) {
        callback(Resource.loading(false))

        initializeDagger(app)

        if (params.serverUrl.isBlank()) {
            callback(Resource.error("Server URL cannot be empty", false))
            return
        }

        this.serverUrl = params.serverUrl
        authentication = Authentication()
        registerTimber()

        setup = true
        callback(Resource.success( true))
    }

    private fun initializeDagger(app: Application) {
        Injector.buildComponent(app)
    }

    private fun registerTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    @Throws(UninitializedPropertyAccessException::class)
    fun getAuthentication(): Authentication {
        if (setup) {
            return authentication
        } else {
            throw UninitializedPropertyAccessException()
        }
    }
}