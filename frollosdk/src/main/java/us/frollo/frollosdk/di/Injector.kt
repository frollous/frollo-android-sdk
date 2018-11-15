package us.frollo.frollosdk.di

import android.app.Application

internal object Injector {
    lateinit var component: SdkComponent

    fun buildComponent(app: Application) {
        component = DaggerSdkComponent
                .builder()
                .application(app)
                .build()
        component.inject(app)
    }
}