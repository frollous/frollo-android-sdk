package us.frollo.frollosdk.data.remote.endpoints

import androidx.lifecycle.LiveData
import retrofit2.http.*
import us.frollo.frollosdk.data.remote.ApiResponse
import us.frollo.frollosdk.model.api.user.UserLoginRequest
import us.frollo.frollosdk.model.api.user.UserResponse

internal interface UserEndpoint {
    companion object {
        const val URL_LOGIN = "/api/v1/user/login"
    }

    @POST(URL_LOGIN)
    fun login(@Body request: UserLoginRequest): LiveData<ApiResponse<UserResponse>>
}