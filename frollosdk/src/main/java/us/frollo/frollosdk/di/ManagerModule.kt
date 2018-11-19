package us.frollo.frollosdk.di

import android.content.Context
import dagger.Module
import dagger.Provides
import us.frollo.frollosdk.preferences.Preferences
import us.frollo.frollosdk.version.Version
import javax.inject.Singleton

@Module
internal class ManagerModule {
    @Provides
    @Singleton
    fun provideSharedPreferenceManager(context: Context) = Preferences(context)

    @Provides
    @Singleton
    fun provideVersionManager(pref: Preferences) = Version(pref)
}