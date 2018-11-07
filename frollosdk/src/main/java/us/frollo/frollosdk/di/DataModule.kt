package us.frollo.frollosdk.di

import dagger.Module
import dagger.Provides
import us.frollo.frollosdk.core.SystemInfo
import us.frollo.frollosdk.data.remote.IApiProvider
import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.data.repo.UserRepo
import javax.inject.Singleton

@Module
class DataModule {
    @Provides
    @Singleton
    fun provideServiceCreator(si: SystemInfo): IApiProvider = NetworkService(si)

    @Provides
    @Singleton
    fun provideUserRepo(service: IApiProvider) = UserRepo(service)
}