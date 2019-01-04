package us.frollo.frollosdk.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.jetbrains.anko.doAsync
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.data.local.SDKDatabase
import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.data.remote.api.UserAPI
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.extensions.fetchTokens
import us.frollo.frollosdk.extensions.stripTokens
import us.frollo.frollosdk.mapping.toUser
import us.frollo.frollosdk.model.api.user.UserLoginRequest
import us.frollo.frollosdk.model.api.user.UserResponse
import us.frollo.frollosdk.model.coredata.user.User
import us.frollo.frollosdk.preferences.Preferences

class Authentication(private val di: DeviceInfo, private val network: NetworkService, private val db: SDKDatabase, private val pref: Preferences) {

    var user: User? = null
        private set

    //TODO: Review - This returns a new LiveData object on each call. Maybe the app should implement a MediatorLiveData and change source in ViewModel.
    var userLiveData: LiveData<Resource<User>>? = null
        get() = fetchUserAsLiveData()
        private set

    var loggedIn: Boolean
        get() = pref.loggedIn
        private set(value) { pref.loggedIn = value }

    private val userAPI: UserAPI = network.create(UserAPI::class.java)

    fun loginUser(method: AuthType, email: String? = null, password: String? = null, userId: String? = null, userToken: String? = null): LiveData<Resource<User>> {
        val request = UserLoginRequest(
                email = email,
                password = password,
                deviceId = di.deviceId,
                deviceName = di.deviceName,
                deviceType = di.deviceType,
                authType = method,
                userId = userId,
                userToken = userToken
        )

        return if (request.valid()) {
            Transformations.map(userAPI.login(request)) {
                Resource.fromApiResponse(it).map { response ->
                    val tokens = response?.fetchTokens()
                    tokens?.let { tokenResponse -> network.handleTokens(tokenResponse) }

                    val userResponse = response?.stripTokens()
                    handleUserResponse(userResponse)

                    user = userResponse?.toUser()
                    user
                }
            }.apply { (this as? MutableLiveData<Resource<User>>)?.value = Resource.loading(null) }
        } else {
            MutableLiveData<Resource<User>>().apply {
                value = Resource.error(DataError(type = DataErrorType.API, subType = DataErrorSubType.INVALID_DATA))
            }
        }
    }

    fun refreshUser() {
        // TODO: To be implemented
        // TODO: update "user" variable
    }

    fun updateUser() {
        // TODO: To be implemented
        // TODO: update "user" variable
    }

    fun deleteUser() {
        // TODO: To be implemented
    }

    fun logoutUser() {
        // TODO: To be implemented
    }

    private fun fetchUserAsLiveData(): LiveData<Resource<User>> =
            Transformations.map(db.users().loadAsLiveData()) {
                Resource.success(it?.toUser())
            }.apply { (this as? MutableLiveData<Resource<User>>)?.value = Resource.loading(null) }

    private fun handleUserResponse(userResponse: UserResponse?) {
        userResponse?.let {
            loggedIn = true

            it.features?.let { features -> pref.features = features }

            doAsync {
                db.users().insert(it)
                // TODO: Notify user updated
            }
        }
    }

    internal fun reset() {
        loggedIn = false
        user = null
        network.reset()
    }
}