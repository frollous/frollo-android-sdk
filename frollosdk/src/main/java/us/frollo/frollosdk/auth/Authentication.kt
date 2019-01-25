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

class Authentication(private val di: DeviceInfo, private val network: NetworkService, private val db: SDKDatabase, private val pref: Preferences) {

    companion object {
        private const val TAG = "Authentication"
    }

    var loggedIn: Boolean
        get() = pref.loggedIn
        private set(value) { pref.loggedIn = value }

    private val userAPI: UserAPI = network.create(UserAPI::class.java)
    private val deviceAPI: DeviceAPI = network.create(DeviceAPI::class.java)

    //TODO: Review - This returns a new LiveData object ON EACH call. Maybe the app should implement a MediatorLiveData and change source in ViewModel.
    fun fetchUser(): LiveData<Resource<User>> =
            Transformations.map(db.users().load()) {
                Resource.success(it?.toUser())
            }.apply { (this as? MutableLiveData<Resource<User>>)?.value = Resource.loading(null) }

    @Throws(FrolloSDKError::class)
    fun loginUser(method: AuthType, email: String? = null, password: String? = null, userId: String? = null, userToken: String? = null, completion: OnFrolloSDKCompletionListener) {
        if (loggedIn) throw FrolloSDKError("Multiple call to login when there is already an user logged in.")

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

    @Throws(FrolloSDKError::class)
    fun registerUser(firstName: String, lastName: String? = null, mobileNumber: String? = null, postcode: String? = null, dateOfBirth: Date? = null, email: String, password: String, completion: OnFrolloSDKCompletionListener) {
        if (loggedIn) throw FrolloSDKError("Multiple call to register when there is already an user logged in.")

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

    fun resetPassword(email: String, completion: OnFrolloSDKCompletionListener) {
        userAPI.resetPassword(UserResetPasswordRequest(email)).enqueue { _, error ->
            if (error != null)
                Log.e("$TAG#resetPassword", error.localizedDescription)
            completion.invoke(error)
        }
    }

    fun refreshUser(completion: OnFrolloSDKCompletionListener? = null) {
        userAPI.fetchUser().enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#refreshUser", error.localizedDescription)
                completion?.invoke(error)
            }
            else handleUserResponse(response, completion)
        }
    }

    fun updateUser(user: User, completion: OnFrolloSDKCompletionListener) {
        userAPI.updateUser(user.updateRequest()).enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#updateUser", error.localizedDescription)
                completion.invoke(error)
            }
            else handleUserResponse(response, completion)
        }
    }

    fun updateAttribution(attribution: Attribution, completion: OnFrolloSDKCompletionListener) {
        userAPI.updateUser(UserUpdateRequest(attribution = attribution)).enqueue { response, error ->
            if (error != null) {
                Log.e("$TAG#updateAttribution", error.localizedDescription)
                completion.invoke(error)
            }
            else handleUserResponse(response, completion)
        }
    }

    fun changePassword(currentPassword: String?, newPassword: String, completion: OnFrolloSDKCompletionListener) {
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

    internal fun deleteUser(completion: OnFrolloSDKCompletionListener) {
        userAPI.deleteUser().enqueue { _, error ->
            if (error != null) Log.e("$TAG#deleteUser", error.localizedDescription)
            else reset()

            completion.invoke(error)
        }
    }

    fun refreshTokens() {
        network.refreshTokens()
    }

    fun authenticateRequest(request: Request) =
            network.authenticateRequest(request)

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

    internal fun logoutUser(completion: OnFrolloSDKCompletionListener? = null) {
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