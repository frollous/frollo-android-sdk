package us.frollo.frollosdk.data.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import us.frollo.frollosdk.base.api.Resource
import us.frollo.frollosdk.data.remote.IApiProvider
import us.frollo.frollosdk.data.remote.api.UserApi
import us.frollo.frollosdk.model.api.user.UserLoginRequest
import us.frollo.frollosdk.model.api.user.UserResponse

class UserRepo(service: IApiProvider) {
    private val userApi: UserApi = service.create(UserApi::class.java)

    fun loginUser(request: UserLoginRequest): LiveData<Resource<UserResponse>> =
            Transformations.map(userApi.login(request)) {
                Resource.fromApiResponse(it)
            }.apply { (this as? MutableLiveData<Resource<UserResponse>>)?.value = Resource.loading(null) }
}