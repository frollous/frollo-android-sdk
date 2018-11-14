package us.frollo.frollosdk.auth

import androidx.lifecycle.LiveData
import us.frollo.frollosdk.base.api.Resource
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.data.repo.UserRepo
import us.frollo.frollosdk.di.Injector
import us.frollo.frollosdk.model.api.user.UserLoginRequest
import us.frollo.frollosdk.model.api.user.UserResponse
import javax.inject.Inject

class Authentication {

    @Inject internal lateinit var di: DeviceInfo
    @Inject internal lateinit var repo: UserRepo

    init {
        Injector.component.inject(this)
    }

    fun loginUser(method: AuthType, email: String? = null, password: String? = null, userId: String? = null, userToken: String? = null): LiveData<Resource<UserResponse>> {
        return repo.loginUser(UserLoginRequest(
                email = email,
                password = password,
                deviceId = di.deviceId,
                deviceName = di.deviceName,
                deviceType = di.deviceType,
                authType = method,
                userId = userId,
                userToken = userToken
        ))
    }
}