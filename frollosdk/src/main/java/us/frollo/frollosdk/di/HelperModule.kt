package us.frollo.frollosdk.di

import android.app.Application
import dagger.Module
import dagger.Provides
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.core.SystemInfo
import javax.inject.Singleton

@Module
class HelperModule {
    @Provides
    @Singleton
    fun provideSystemInfo(app: Application) = SystemInfo(app)

    @Provides
    @Singleton
    fun provideDeviceInfo(app: Application) = DeviceInfo(app)
}