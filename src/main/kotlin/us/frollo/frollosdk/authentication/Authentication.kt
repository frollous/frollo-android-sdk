package us.frollo.frollosdk.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import okhttp3.Request
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.ACTION.ACTION_USER_UPDATED
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.DeviceAPI
import us.frollo.frollosdk.network.api.UserAPI
import us.frollo.frollosdk.error.*
import us.frollo.frollosdk.extensions.*
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toUser
import us.frollo.frollosdk.model.api.device.DeviceUpdateRequest
import us.frollo.frollosdk.model.api.user.*
import us.frollo.frollosdk.model.coredata.user.Address
import us.frollo.frollosdk.model.coredata.user.Attribution
import us.frollo.frollosdk.model.coredata.user.User
import us.frollo.frollosdk.preferences.Preferences
import java.util.*

/**
 * Manages authentication, login, registration, logout and the user profile.
 */
class Authentication(private val di: DeviceInfo, private val network: NetworkService, private val db: SDKDatabase, private val pref: Preferences) {

    companion object {
        private const val TAG = "Authentication"
    }

    /**
     * Indicates if the user is currently authorised with Frollo
     */
    var loggedIn: Boolean
        get() = pref.loggedIn
        private set(value) { pref.loggedIn = value }

    private val userAPI: UserAPI = network.create(UserAPI::class.java)
    private val deviceAPI: DeviceAPI = network.create(DeviceAPI::class.java)

    /**
     * Fetch the first available user model from the cache
     *
     * @return LiveData object of Resource<User> which can be observed using an Observer for future changes as well.
     */
    fun fetchUser(): LiveData<Resource<User>> =
            Transformations.map(db.users().load()) {
                Resource.success(it?.toUser())
            }

    /**
     * Login a user using various authentication methods
     *
     * @param method Login method to be used. See [AuthType] for details
     * @param email Email address of the user (optional)
     * @param password Password for the user (optional)
     * @param userId Unique identifier for the user depending on authentication method (optional)
     * @param userToken Token for the user depending on authentication method (optional)
     * @param completion: Completion handler with any error that occurred
     */
    fun loginUser(method: AuthType, email: String? = null, password: String? = null, userId: String? = null,
                  userToken: String? = null, completion: OnFrolloSDKCompletionListener<Result>) {
        if (loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.ALREADY_LOGGED_IN)
            completion.invoke(Result.error(error))
            return
        }

        val request = UserLoginRequest(
                deviceId = di.deviceId,
                deviceName = di.deviceName,
                deviceType = di.deviceType,
                email = email,
                password = password,
                authType = method,
                userId = userId,
                userToken = userToken)

        if (request.valid()) {
            userAPI.login(request).enqueue { resource ->
                when(resource.status) {
                    Resource.Status.SUCCESS -> {
                        resource.data?.fetchTokens()?.let { network.handleTokens(it) }
                        handleUserResponse(resource.data?.stripTokens(), completion)
                    }
                    Resource.Status.ERROR -> {
                        Log.e("$TAG#loginUser", resource.error?.localizedDescription)
                        completion.invoke(Result.error(resource.error))
                    }
                }
            }
        } else {
            val error = DataError(type = DataErrorType.API, subType = DataErrorSubType.INVALID_DATA)
            completion.invoke(Result.error(error))
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
    fun registerUser(firstName: String, lastName: String? = null, mobileNumber: String? = null,
                     postcode: String? = null, dateOfBirth: Date? = null, email: String, password: String,
                     completion: OnFrolloSDKCompletionListener<Result>) {
        if (loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.ALREADY_LOGGED_IN)
            completion.invoke(Result.error(error))
            return
        }

        val request = UserRegisterRequest(
                deviceId = di.deviceId,
                deviceName = di.deviceName,
                deviceType = di.deviceType,
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password,
                currentAddress = if (postcode?.isNotBlank() == true) Address(postcode = postcode) else null,
                mobileNumber = mobileNumber,
                dateOfBirth = dateOfBirth?.toString("yyyy-MM"))

        userAPI.register(request).enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    resource.data?.fetchTokens()?.let { network.handleTokens(it) }
                    handleUserResponse(resource.data?.stripTokens(), completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#registerUser", resource.error?.localizedDescription)
                    completion.invoke(Result.error(resource.error))
                }
            }
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
        userAPI.resetPassword(UserResetPasswordRequest(email)).enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    completion.invoke(Result.success())
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#resetPassword", resource.error?.localizedDescription)
                    completion.invoke(Result.error(resource.error))
                }
            }
        }
    }

    /**
     * Refreshes the latest details of the user from the server. This should be called on app launch and resuming after a set period of time if the user is already logged in. This returns the same data as login and register.
     *
     * @param completion A completion handler once the API has returned and the cache has been updated. Returns any error that occurred during the process. (Optional)
     */
    fun refreshUser(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            completion?.invoke(Result.error(error))
            return
        }

        userAPI.fetchUser().enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleUserResponse(resource.data, completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshUser", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
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
        if (!loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            completion.invoke(Result.error(error))
            return
        }

        userAPI.updateUser(user.updateRequest()).enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleUserResponse(resource.data, completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateUser", resource.error?.localizedDescription)
                    completion.invoke(Result.error(resource.error))
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
        if (!loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            completion.invoke(Result.error(error))
            return
        }

        userAPI.updateUser(UserUpdateRequest(attribution = attribution)).enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    handleUserResponse(resource.data, completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateAttribution", resource.error?.localizedDescription)
                    completion.invoke(Result.error(resource.error))
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
        if (!loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            completion.invoke(Result.error(error))
            return
        }

        val request = UserChangePasswordRequest(
                currentPassword = currentPassword, // currentPassword can be for Facebook login only
                newPassword = newPassword)

        if (request.valid()) {
            userAPI.changePassword(request).enqueue { resource ->
                when(resource.status) {
                    Resource.Status.SUCCESS -> {
                        completion.invoke(Result.success())
                    }
                    Resource.Status.ERROR -> {
                        Log.e("$TAG#changePassword", resource.error?.localizedDescription)
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
     * Delete the user account and complete logout activities on success
     *
     * @param completion Completion handler with any error that occurred
     */
    internal fun deleteUser(completion: OnFrolloSDKCompletionListener<Result>) {
        if (!loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            completion.invoke(Result.error(error))
            return
        }

        userAPI.deleteUser().enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    reset()
                    completion.invoke(Result.success())
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#deleteUser", resource.error?.localizedDescription)
                    completion.invoke(Result.error(resource.error))
                }
            }
        }
    }

    /**
     * Refresh Access and Refresh Tokens
     *
     * Forces a refresh of the access and refresh tokens if a 401 was encountered. For advanced usage only in combination with web request authentication.
     */
    fun refreshTokens() {
        network.refreshTokens()
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
    internal fun updateDevice(compliant: Boolean? = null, notificationToken: String? = null, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            completion?.invoke(Result.error(error))
            return
        }

        val request = DeviceUpdateRequest(
                deviceName = di.deviceName,
                notificationToken = notificationToken,
                timezone = TimeZone.getDefault().id,
                compliant = compliant)

        deviceAPI.updateDevice(request).enqueue { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    completion?.invoke(Result.success())
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateDevice", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    /**
     * Log out the user from the server. This revokes the refresh token for the current device if not already revoked and resets the token storage.
     *
     * @param completion Completion handler with optional error if something goes wrong during the logout process
     */
    internal fun logoutUser(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!loggedIn) {
            return
        }

        userAPI.logout().enqueue { resource ->

            reset()

            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    completion?.invoke(Result.success())
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#logoutUser", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    private fun handleUserResponse(userResponse: UserResponse?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        userResponse?.let {
            if (!loggedIn) loggedIn = true

            it.features?.let { features -> pref.features = features }

            doAsync {
                db.users().insert(it)

                uiThread {
                    completion?.invoke(Result.success())
                    notify(ACTION_USER_UPDATED)
                }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    internal fun reset() {
        loggedIn = false
        network.reset()
    }
}