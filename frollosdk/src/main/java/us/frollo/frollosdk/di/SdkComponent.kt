package us.frollo.frollosdk.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import us.frollo.frollosdk.auth.Authentication
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, HelperModule::class, DataModule::class])
internal interface SdkComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun build(): SdkComponent
    }
    fun inject(app: Application)
    fun inject(authentication: Authentication)
}