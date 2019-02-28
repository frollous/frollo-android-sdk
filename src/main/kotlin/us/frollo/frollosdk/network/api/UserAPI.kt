package us.frollo.frollosdk.network.api

import retrofit2.Call
import retrofit2.http.*
import us.frollo.frollosdk.model.api.user.*

internal interface UserAPI {
    companion object {
        const val URL_REGISTER = "user/register"
        const val URL_PASSWORD_RESET = "user/reset"
        const val URL_USER_DETAILS = "user/details"
        const val URL_LOGOUT = "user/logout"
        const val URL_CHANGE_PASSWORD = "user"
        const val URL_DELETE_USER = "user"
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