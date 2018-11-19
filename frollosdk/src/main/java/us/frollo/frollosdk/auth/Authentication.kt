package us.frollo.frollosdk.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.data.remote.endpoints.UserEndpoint
import us.frollo.frollosdk.model.api.user.UserLoginRequest
import us.frollo.frollosdk.model.coredata.user.User

class Authentication(private val di: DeviceInfo, network: NetworkService) {

    private val userEndpoint: UserEndpoint = network.create(UserEndpoint::class.java)

    fun loginUser(method: AuthType, email: String? = null, password: String? = null, userId: String? = null, userToken: String? = null): LiveData<Resource<User>> {
        val request = UserLoginRequest(
                email = email,
                password = password,
                deviceId = di.deviceId,
                deviceName = di.deviceName,
                deviceType = di.deviceType,
                authType = method,
                userId = userId,
                userToken = userToken
        )

        return Transformations.map(userEndpoint.login(request)) {
            Resource.fromApiResponse(it).map { res ->
                res?.let { model -> User(model.id, model.firstName) }
            }
        }.apply { (this as? MutableLiveData<Resource<User>>)?.value = Resource.loading(null) }
    }
}