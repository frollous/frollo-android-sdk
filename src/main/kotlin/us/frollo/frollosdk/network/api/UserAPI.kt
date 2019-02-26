package us.frollo.frollosdk.network.api

import retrofit2.Call
import retrofit2.http.*
import us.frollo.frollosdk.network.NetworkHelper.Companion.API_VERSION_PATH
import us.frollo.frollosdk.model.api.user.*

internal interface UserAPI {
    companion object {
        const val URL_LOGIN = "$API_VERSION_PATH/user/login/"
        const val URL_REGISTER = "$API_VERSION_PATH/user/register/"
        const val URL_PASSWORD_RESET = "$API_VERSION_PATH/user/reset/"
        const val URL_USER_DETAILS = "$API_VERSION_PATH/user/details/"
        const val URL_LOGOUT = "$API_VERSION_PATH/user/logout/"
        const val URL_CHANGE_PASSWORD = "$API_VERSION_PATH/user"
        const val URL_DELETE_USER = "$API_VERSION_PATH/user"
    }

    @POST(URL_REGISTER)
    fun register(@Body request: UserRegisterRequest): Call<UserResponse>

    @GET(URL_USER_DETAILS)
    fun fetchUser(): Call<UserResponse>

    @PUT(URL_USER_DETAILS)
    fun updateUser(@Body request: UserUpdateRequest): Call<UserResponse>

    @POST(URL_PASSWORD_RESET)
    fun resetPassword(@Body request: UserResetPasswordRequest): Call<Void>

    @PUT(URL_CHANGE_PASSWORD)
    fun changePassword(@Body request: UserChangePasswordRequest): Call<Void>

    @DELETE(URL_DELETE_USER)
    fun deleteUser(): Call<Void>

    @PUT(URL_LOGOUT)
    fun logout(): Call<Void>
}