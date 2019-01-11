package us.frollo.frollosdk.data.remote.api

import androidx.lifecycle.LiveData
import retrofit2.http.*
import us.frollo.frollosdk.data.remote.ApiResponse
import us.frollo.frollosdk.data.remote.NetworkHelper.Companion.API_VERSION_PATH
import us.frollo.frollosdk.model.api.user.UserLoginRequest
import us.frollo.frollosdk.model.api.user.UserRegisterRequest
import us.frollo.frollosdk.model.api.user.UserResponse
import us.frollo.frollosdk.model.api.user.UserUpdateRequest

internal interface UserAPI {
    companion object {
        const val URL_LOGIN = "$API_VERSION_PATH/user/login/"
        const val URL_REGISTER = "$API_VERSION_PATH/user/register/"
        const val URL_PASSWORD_RESET = "$API_VERSION_PATH/user/reset/"
        const val URL_USER_DETAILS = "$API_VERSION_PATH/user/details/"
    }

    @POST(URL_REGISTER)
    fun register(@Body request: UserRegisterRequest): LiveData<ApiResponse<UserResponse>>

    @POST(URL_LOGIN)
    fun login(@Body request: UserLoginRequest): LiveData<ApiResponse<UserResponse>>

    @GET(URL_USER_DETAILS)
    fun fetchUser(): LiveData<ApiResponse<UserResponse>>

    @PUT(URL_USER_DETAILS)
    fun updateUser(@Body request: UserUpdateRequest): LiveData<ApiResponse<UserResponse>>

    //@POST(URL_PASSWORD_RESET)
    //fun resetPassword(@Body request: UserPasswordResetRequest): LiveData<ApiResponse<Void>>
}