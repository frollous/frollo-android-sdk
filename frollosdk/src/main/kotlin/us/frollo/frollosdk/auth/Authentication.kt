package us.frollo.frollosdk.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import okhttp3.Request
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.ACTION.ACTION_USER_UPDATED
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.data.local.SDKDatabase
import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.data.remote.api.DeviceAPI
import us.frollo.frollosdk.data.remote.api.UserAPI
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
            }.apply { (this as? MutableLiveData<Resource<User>>)?.value = Resource.loading(null) }

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
    fun loginUser(method: AuthType, email: String? = null, password: String? = null, userId: String? = null, userToken: String? = null, completion: OnFrolloSDKCompletionListener) {
        if (loggedIn) {
            completion.invoke(DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.ALREADY_LOGGED_IN))
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
            userAPI.login(request).enqueue { response, error ->
                if (error != null) {
                    Log.e("$TAG#loginUser", error.localizedDescription)
                    completion.invoke(error)
                } else {
                    response?.fetchTokens()?.let { network.handleTokens(it) }
                    handleUserResponse(response?.stripTokens(), completion)
                }
            }
        } else {
            completion.invoke(DataError(type = DataErrorType.API, subType = DataErrorSubType.INVALID_DATA))
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
    fun registerUser(firstName: String, lastName: String? = null, mobileNumber: String? = null, postcode: String? = null, dateOfBirth: Date? = null, email: String, password: String, completion: OnFrolloSDKCompletionListener) {
        if (loggedIn) {
            completion.invoke(DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.ALREADY_LOGGED_IN))
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

        userAPI.register(request).enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#registerUser", error.localizedDescription)
                completion.invoke(error)
            } else {
                response?.fetchTokens()?.let { network.handleTokens(it) }
                handleUserResponse(response?.stripTokens(), completion)
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
    fun resetPassword(email: String, completion: OnFrolloSDKCompletionListener) {
        userAPI.resetPassword(UserResetPasswordRequest(email)).enqueue { _, error ->
            if (error != null)
                Log.e("$TAG#resetPassword", error.localizedDescription)
            completion.invoke(error)
        }
    }

    /**
     * Refreshes the latest details of the user from the server. This should be called on app launch and resuming after a set period of time if the user is already logged in. This returns the same data as login and register.
     *
     * @param completion A completion handler once the API has returned and the cache has been updated. Returns any error that occurred during the process. (Optional)
     */
    fun refreshUser(completion: OnFrolloSDKCompletionListener? = null) {
        if (!loggedIn) {
            completion?.invoke(DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT))
            return
        }

        userAPI.fetchUser().enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#refreshUser", error.localizedDescription)
                completion?.invoke(error)
            }
            else handleUserResponse(response, completion)
        }
    }

    /**
     * Updates the user details on the server. This should be called whenever details or statistics about a user are to be altered, e.g. changing email.
     *
     * @param completion A completion handler once the API has returned and the cache has been updated. Returns any error that occurred during the process.
     */
    fun updateUser(user: User, completion: OnFrolloSDKCompletionListener) {
        if (!loggedIn) {
            completion.invoke(DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT))
            return
        }

        userAPI.updateUser(user.updateRequest()).enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#updateUser", error.localizedDescription)
                completion.invoke(error)
            }
            else handleUserResponse(response, completion)
        }
    }

    /**
     * Updates the user attribution on the server. This should be called whenever user attribution are to be updated.
     *
     * @param completion A completion handler once the API has returned and the cache has been updated. Returns any error that occurred during the process.
     */
    fun updateAttribution(attribution: Attribution, completion: OnFrolloSDKCompletionListener) {
        if (!loggedIn) {
            completion.invoke(DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT))
            return
        }

        userAPI.updateUser(UserUpdateRequest(attribution = attribution)).enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#updateAttribution", error.localizedDescription)
                completion.invoke(error)
            }
            else handleUserResponse(response, completion)
        }
    }

    /**
     * Change the password for the user. Current password is not needed for users who signed up using a 3rd party and never set a password. Check for [User.validPassword] on the user profile to determine this.
     *
     * @param currentPassword Current password to validate the user (optional)
     * @param newPassword New password for the user - must be at least 8 characters
     * @param completion Completion handler with any error that occurred
     */
    fun changePassword(currentPassword: String?, newPassword: String, completion: OnFrolloSDKCompletionListener) {
        if (!loggedIn) {
            completion.invoke(DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT))
            return
        }

        val request = UserChangePasswordRequest(
                currentPassword = currentPassword, // currentPassword can be for Facebook login only
                newPassword = newPassword)

        if (request.valid()) {
            userAPI.changePassword(request).enqueue { _, error ->
                if (error != null) {
                    Log.e("$TAG#changePassword", error.localizedDescription)
                }

                completion.invoke(error)
            }
        } else {
            completion.invoke(DataError(type = DataErrorType.API, subType = DataErrorSubType.PASSWORD_TOO_SHORT))
        }
    }

    /**
     * Delete the user account and complete logout activities on success
     *
     * @param completion Completion handler with any error that occurred
     */
    internal fun deleteUser(completion: OnFrolloSDKCompletionListener) {
        if (!loggedIn) {
            completion.invoke(DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT))
            return
        }

        userAPI.deleteUser().enqueue { _, error ->
            if (error != null) Log.e("$TAG#deleteUser", error.localizedDescription)
            else reset()

            completion.invoke(error)
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
     * Update information about the current device. Updates the current device name and timezone automatically.
     *
     * @param notificationToken Push notification token for the device (optional)
     * @param completion Completion handler with any error that occurred (optional)
     */
    internal fun updateDevice(notificationToken: String? = null, completion: OnFrolloSDKCompletionListener? = null) {
        val request = DeviceUpdateRequest(
                deviceName = di.deviceName,
                notificationToken = notificationToken,
                timezone = TimeZone.getDefault().id)

        deviceAPI.updateDevice(request).enqueue { _, error ->
            if (error != null) {
                Log.e("$TAG#updateDevice", error.localizedDescription)
            }

            completion?.invoke(error)
        }
    }

    /**
     * Log out the user from the server. This revokes the refresh token for the current device if not already revoked and resets the token storage.
     *
     * @param completion Completion handler with optional error if something goes wrong during the logout process
     */
    internal fun logoutUser(completion: OnFrolloSDKCompletionListener? = null) {
        if (!loggedIn) {
            return
        }

        userAPI.logout().enqueue { _, error ->
            if (error != null)
                Log.e("$TAG#logoutUser", error.localizedDescription)

            reset()

            completion?.invoke(error)
        }
    }

    private fun handleUserResponse(userResponse: UserResponse?, completion: OnFrolloSDKCompletionListener? = null) {
        userResponse?.let {
            if (!loggedIn) loggedIn = true

            it.features?.let { features -> pref.features = features }

            doAsync {
                db.users().insert(it)

                uiThread {
                    completion?.invoke(null)
                    notify(ACTION_USER_UPDATED)
                }
            }
        } ?: run { completion?.invoke(null) } // HACK: invoke completion callback if response is null else app will never be notified if response is null.
    }

    internal fun reset() {
        loggedIn = false
        network.reset()
    }
}