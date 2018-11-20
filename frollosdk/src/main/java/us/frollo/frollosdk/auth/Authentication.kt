package us.frollo.frollosdk.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.jetbrains.anko.doAsync
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.data.local.SDKDatabase
import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.data.remote.endpoints.UserEndpoint
import us.frollo.frollosdk.mapping.toUser
import us.frollo.frollosdk.model.api.user.UserLoginRequest
import us.frollo.frollosdk.model.coredata.user.User

class Authentication(private val di: DeviceInfo, network: NetworkService, private val db: SDKDatabase) {

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
            Resource.fromApiResponse(it).map { response ->
                response?.let { model ->
                    doAsync { db.users().insert(model) }
                    model.toUser()
                }
            }
        }.apply { (this as? MutableLiveData<Resource<User>>)?.value = Resource.loading(null) }
    }
}