package us.frollo.frollosdk.data.remote.api

import androidx.lifecycle.LiveData
import retrofit2.http.*
import us.frollo.frollosdk.data.remote.ApiResponse
import us.frollo.frollosdk.model.api.user.UserLoginRequest
import us.frollo.frollosdk.model.api.user.UserResponse

internal interface UserAPI {
    companion object {
        const val URL_LOGIN = "/api/v1/user/login/"
        const val URL_REGISTER = "/api/v1/user/register/"
        const val URL_USER_RESET = "/api/v1/user/reset/"
    }

    @POST(URL_LOGIN)
    fun login(@Body request: UserLoginRequest): LiveData<ApiResponse<UserResponse>>
}