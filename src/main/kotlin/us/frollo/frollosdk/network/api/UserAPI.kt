/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.frollo.frollosdk.network.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap
import us.frollo.frollosdk.model.api.user.AddressAutocomplete
import us.frollo.frollosdk.model.api.user.UserChangePasswordRequest
import us.frollo.frollosdk.model.api.user.UserConfirmDetailsRequest
import us.frollo.frollosdk.model.api.user.UserMigrationRequest
import us.frollo.frollosdk.model.api.user.UserOTPRequest
import us.frollo.frollosdk.model.api.user.UserRegisterRequest
import us.frollo.frollosdk.model.api.user.UserResetPasswordRequest
import us.frollo.frollosdk.model.api.user.UserResponse
import us.frollo.frollosdk.model.api.user.UserUnconfirmedDetailsResponse
import us.frollo.frollosdk.model.api.user.UserUpdateRequest
import us.frollo.frollosdk.model.api.user.payid.UserPayIdAccountResponse
import us.frollo.frollosdk.model.api.user.payid.UserPayIdOTPRequest
import us.frollo.frollosdk.model.api.user.payid.UserPayIdOTPResponse
import us.frollo.frollosdk.model.api.user.payid.UserPayIdRegisterRequest
import us.frollo.frollosdk.model.api.user.payid.UserPayIdRemoveRequest
import us.frollo.frollosdk.model.api.user.payid.UserPayIdResponse
import us.frollo.frollosdk.model.coredata.user.Address
import us.frollo.frollosdk.network.NetworkHelper

internal interface UserAPI {
    companion object {
        const val URL_REGISTER = "user/register"
        const val URL_PASSWORD_RESET = "user/reset"
        const val URL_USER_DETAILS = "user/details"
        const val URL_CHANGE_PASSWORD = "user"
        const val URL_DELETE_USER = "user"
        const val URL_MIGRATE_USER = "user/migrate"
        const val URL_REQUEST_OTP = "user/otp"
        const val URL_CONFIRM_DETAILS = "user/details/confirm"
        const val URL_PAYID = "user/payid"
        const val URL_PAYID_REMOVE = "user/payid/remove"
        const val URL_PAYID_OTP = "user/payid/otp"
        const val URL_PAYID_ACCOUNT = "user/payid/account/{account_id}"
        const val URL_ADDRESS_AUTOCOMPLETE = "user/addresses/autocomplete"
        const val URL_ADDRESS = "user/addresses/autocomplete/{address_id}"
    }

    @POST(URL_REGISTER)
    fun register(@Body request: UserRegisterRequest): Call<UserResponse>

    @GET(URL_USER_DETAILS)
    fun fetchUser(): Call<UserResponse>

    @PUT(URL_USER_DETAILS)
    fun updateUser(@Body request: UserUpdateRequest, @Header(NetworkHelper.HEADER_OTP) otp: String? = null): Call<UserResponse>

    @POST(URL_PASSWORD_RESET)
    fun resetPassword(@Body request: UserResetPasswordRequest): Call<Void>

    @PUT(URL_CHANGE_PASSWORD)
    fun changePassword(@Body request: UserChangePasswordRequest): Call<Void>

    @DELETE(URL_DELETE_USER)
    fun deleteUser(): Call<Void>

    @POST(URL_MIGRATE_USER)
    fun migrateUser(@Body request: UserMigrationRequest): Call<Void>

    @POST(URL_REQUEST_OTP)
    fun requestOtp(@Body request: UserOTPRequest): Call<Void>

    @GET(URL_CONFIRM_DETAILS)
    fun fetchUnconfirmedUserDetails(): Call<UserUnconfirmedDetailsResponse>

    @PUT(URL_CONFIRM_DETAILS)
    fun confirmUserDetails(@Body request: UserConfirmDetailsRequest, @Header(NetworkHelper.HEADER_OTP) otp: String?): Call<Void>

    @GET(URL_PAYID)
    fun fetchPayIDs(): Call<List<UserPayIdResponse>>

    @POST(URL_PAYID_OTP)
    fun requestPayIdOtp(@Body request: UserPayIdOTPRequest): Call<UserPayIdOTPResponse>

    @POST(URL_PAYID)
    fun registerPayId(@Body request: UserPayIdRegisterRequest): Call<Void>

    @POST(URL_PAYID_REMOVE)
    fun removePayId(@Body request: UserPayIdRemoveRequest): Call<Void>

    @GET(URL_PAYID_ACCOUNT)
    fun fetchPayIdsForAccount(@Path("account_id") id: Long): Call<List<UserPayIdAccountResponse>>

    @POST(URL_ADDRESS_AUTOCOMPLETE)
    fun addressAutocomplete(@QueryMap queryParams: Map<String, String>): Call<List<AddressAutocomplete>>

    @GET(URL_ADDRESS)
    fun fetchAddress(@Path("address_id") addressId: String): Call<Address>
}
