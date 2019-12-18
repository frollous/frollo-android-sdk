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

package us.frollo.frollosdk.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import okhttp3.Request
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.ACTION
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.notify
import us.frollo.frollosdk.extensions.toString
import us.frollo.frollosdk.extensions.updateRequest
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toUser
import us.frollo.frollosdk.model.api.device.DeviceUpdateRequest
import us.frollo.frollosdk.model.api.user.UserChangePasswordRequest
import us.frollo.frollosdk.model.api.user.UserMigrationRequest
import us.frollo.frollosdk.model.api.user.UserRegisterRequest
import us.frollo.frollosdk.model.api.user.UserResetPasswordRequest
import us.frollo.frollosdk.model.api.user.UserResponse
import us.frollo.frollosdk.model.api.user.UserUpdateRequest
import us.frollo.frollosdk.model.coredata.user.Address
import us.frollo.frollosdk.model.coredata.user.Attribution
import us.frollo.frollosdk.model.coredata.user.User
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.DeviceAPI
import us.frollo.frollosdk.network.api.UserAPI
import us.frollo.frollosdk.preferences.Preferences
import java.util.Date
import java.util.TimeZone

/**
 * Manages the user details and device
 */
class UserManagement(
    private val di: DeviceInfo,
    private val network: NetworkService,
    private val clientId: String,
    private val db: SDKDatabase,
    private val pref: Preferences
) {

    companion object {
        private const val TAG = "UserManagement"
    }

    private val userAPI: UserAPI = network.create(UserAPI::class.java)
    private val deviceAPI: DeviceAPI = network.create(DeviceAPI::class.java)

    /**
     * Fetch the first available user model from the cache
     *
     * @return LiveData object of Resource<User> which can be observed using an Observer for future changes as well.
     */
    fun fetchUser(): LiveData<Resource<User>> =
            Transformations.map(db.users().load()) {
                Resource.success(it)
            }

    /**
     * Refreshes the latest details of the user from the server. This should be called on app launch and resuming after a set period of time if the user is already logged in. This returns the same data as login and register.
     *
     * @param completion A completion handler once the API has returned and the cache has been updated. Returns any error that occurred during the process. (Optional)
     */
    fun refreshUser(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        userAPI.fetchUser().enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshUser", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleUserResponse(resource.data, completion)
                }
            }
        }
    }

    /**
     * Register a user by email and password
     *
     * @param firstName Given name of the user
     * @param lastName Family name of the user, if provided (optional)
     * @param mobileNumber Mobile phone number of the user, if provided (optional)
     * @param postcode Postcode of the user, if provided (optional)
     * @param dateOfBirth Date of birth of the user, if provided (optional)
     * @param email Email address of the user
     * @param password Password for the user
     * @param completion Completion handler with any error that occurred
     */
    fun registerUser(
        firstName: String,
        lastName: String? = null,
        mobileNumber: String? = null,
        postcode: String? = null,
        dateOfBirth: Date? = null,
        email: String,
        password: String,
        completion: OnFrolloSDKCompletionListener<Result>
    ) {
        // Create the user on the server and at the authorization endpoint
        val request = UserRegisterRequest(
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password,
                currentAddress = if (postcode?.isNotBlank() == true) Address(postcode = postcode) else null,
                mobileNumber = mobileNumber,
                dateOfBirth = dateOfBirth?.toString("yyyy-MM"),
                clientId = clientId)

        userAPI.register(request).enqueue { userResource ->
            when (userResource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#registerUser", userResource.error?.localizedDescription)
                    completion.invoke(Result.error(userResource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleUserResponse(userResource.data, completion)
                }
            }
        }
    }

    /**
     * Updates the user details on the server. This should be called whenever details or statistics about a user are to be altered, e.g. changing email.
     *
     * @param completion A completion handler once the API has returned and the cache has been updated. Returns any error that occurred during the process.
     */
    fun updateUser(user: User, completion: OnFrolloSDKCompletionListener<Result>) {
        userAPI.updateUser(user.updateRequest()).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateUser", resource.error?.localizedDescription)
                    completion.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleUserResponse(resource.data, completion)
                }
            }
        }
    }

    /**
     * Updates the user attribution on the server. This should be called whenever user attribution are to be updated.
     *
     * @param completion A completion handler once the API has returned and the cache has been updated. Returns any error that occurred during the process.
     */
    fun updateAttribution(attribution: Attribution, completion: OnFrolloSDKCompletionListener<Result>) {
        userAPI.updateUser(UserUpdateRequest(attribution = attribution)).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateAttribution", resource.error?.localizedDescription)
                    completion.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleUserResponse(resource.data, completion)
                }
            }
        }
    }

    /**
     * Delete the user account and complete logout activities on success
     *
     * @param completion Completion handler with any error that occurred
     */
    fun deleteUser(completion: OnFrolloSDKCompletionListener<Result>) {
        userAPI.deleteUser().enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#deleteUser", resource.error?.localizedDescription)
                    completion.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    reset(completion)
                }
            }
        }
    }

    /**
     * Change the password for the user. Current password is not needed for users who signed up using a 3rd party and never set a password. Check for [User.validPassword] on the user profile to determine this.
     *
     * @param currentPassword Current password to validate the user (optional)
     * @param newPassword New password for the user - must be at least 8 characters
     * @param completion Completion handler with any error that occurred
     */
    fun changePassword(currentPassword: String?, newPassword: String, completion: OnFrolloSDKCompletionListener<Result>) {
        val request = UserChangePasswordRequest(
                currentPassword = currentPassword, // currentPassword can be for Facebook login only
                newPassword = newPassword)

        if (request.valid()) {
            userAPI.changePassword(request).enqueue { resource ->
                when (resource.status) {
                    Resource.Status.ERROR -> {
                        Log.e("$TAG#changePassword", resource.error?.localizedDescription)
                        completion.invoke(Result.error(resource.error))
                    }
                    Resource.Status.SUCCESS -> {
                        completion.invoke(Result.success())
                    }
                }
            }
        } else {
            val error = DataError(type = DataErrorType.API, subType = DataErrorSubType.PASSWORD_TOO_SHORT)
            completion.invoke(Result.error(error))
        }
    }

    /**
     * Reset the password for the specified email.
     *
     * Sends an email to the address provided if an account exists with instructions on resetting the password.
     *
     * @param email Email address of the account to begin resetting the password for.
     * @param completion A completion handler once the API has returned and the cache has been updated. Returns any error that occurred during the process.
     */
    fun resetPassword(email: String, completion: OnFrolloSDKCompletionListener<Result>) {
        userAPI.resetPassword(UserResetPasswordRequest(
                email = email,
                clientId = clientId
        )).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#resetPassword", resource.error?.localizedDescription)
                    completion.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    completion.invoke(Result.success())
                }
            }
        }
    }

    /**
     * Update the compliance status of the current device. Use this to indicate a rooted device for example.
     *
     * @param compliant Indicates if the device is compliant or not
     * @param completion Completion handler with any error that occurred (optional)
     */
    fun updateDeviceCompliance(compliant: Boolean, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        updateDevice(compliant = compliant, completion = completion)
    }

    /**
     * Update information about the current device. Updates the current device name and timezone automatically.
     *
     * @param compliant Indicates if the device is compliant or not (optional)
     * @param notificationToken Push notification token for the device (optional)
     * @param completion Completion handler with any error that occurred (optional)
     */
    fun updateDevice(compliant: Boolean? = null, notificationToken: String? = null, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        val request = DeviceUpdateRequest(
                deviceId = di.deviceId,
                deviceType = di.deviceType,
                deviceName = di.deviceName,
                notificationToken = notificationToken,
                timezone = TimeZone.getDefault().id,
                compliant = compliant)

        deviceAPI.updateDevice(request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateDevice", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    completion?.invoke(Result.success())
                }
            }
        }
    }

    /**
     * Migrates a user from one identity provider to another if available. The user will then be logged out and need to be authenticated again.
     *
     * @param password The new password for the migrated user
     * @param completion: Completion handler with any error that occurred
     */
    fun migrateUser(password: String, completion: OnFrolloSDKCompletionListener<Result>) {
        val refreshToken = network.authToken.getRefreshToken()

        if (refreshToken == null) {
            val error = DataError(DataErrorType.AUTHENTICATION, DataErrorSubType.MISSING_REFRESH_TOKEN)
            Log.e("$TAG#migrateUser", error.localizedMessage)
            completion.invoke(Result.error(error))

            reset()

            return
        }

        val request = UserMigrationRequest(password = password)

        if (request.valid()) {
            userAPI.migrateUser(UserMigrationRequest(password = password)).enqueue { resource ->
                when (resource.status) {
                    Resource.Status.SUCCESS -> {
                        // Force logout the user as this refresh token is no longer valid
                        reset(completion)
                    }
                    Resource.Status.ERROR -> {
                        Log.e("$TAG#migrateUser", resource.error?.localizedDescription)
                        completion.invoke(Result.error(resource.error))
                    }
                }
            }
        } else {
            val error = DataError(type = DataErrorType.API, subType = DataErrorSubType.PASSWORD_TOO_SHORT)
            completion.invoke(Result.error(error))
        }
    }

    /**
     * Authenticate a web request
     *
     * Allows authenticating a Request manually with the user's current access token. For advanced usage such as authenticating calls to web content.
     *
     * @param request URL Request to be authenticated and provided the access token
     */
    fun authenticateRequest(request: Request) =
            network.authenticateRequest(request)

    private fun handleUserResponse(userResponse: UserResponse?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        userResponse?.let {
            it.features?.let { features -> pref.features = features }

            doAsync {
                db.users().insert(it.toUser())

                uiThread {
                    completion?.invoke(Result.success())
                    notify(ACTION.ACTION_USER_UPDATED)
                }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    internal fun reset(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (FrolloSDK.isSetup) FrolloSDK.reset(completion)
    }
}