package us.frollo.frollosdk.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import okhttp3.Request
import org.jetbrains.anko.doAsync
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.ACTION.ACTION_USER_UPDATED
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.data.local.SDKDatabase
import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.data.remote.api.UserAPI
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.extensions.*
import us.frollo.frollosdk.mapping.toUser
import us.frollo.frollosdk.model.api.user.*
import us.frollo.frollosdk.model.coredata.user.Address
import us.frollo.frollosdk.model.coredata.user.Attribution
import us.frollo.frollosdk.model.coredata.user.User
import us.frollo.frollosdk.preferences.Preferences
import java.util.*

class Authentication(private val di: DeviceInfo, private val network: NetworkService, private val db: SDKDatabase, private val pref: Preferences) {

    var user: User? = null
        private set

    //TODO: Review - This returns a new LiveData object ON EACH call. Maybe the app should implement a MediatorLiveData and change source in ViewModel.
    var userLiveData: LiveData<Resource<User>>? = null
        get() = fetchUserAsLiveData()
        private set

    var loggedIn: Boolean
        get() = pref.loggedIn
        private set(value) { pref.loggedIn = value }

    private val userAPI: UserAPI = network.create(UserAPI::class.java)

    fun loginUser(method: AuthType, email: String? = null, password: String? = null, userId: String? = null, userToken: String? = null): LiveData<Resource<User>> {
        val request = UserLoginRequest(
                deviceId = di.deviceId,
                deviceName = di.deviceName,
                deviceType = di.deviceType,
                email = email,
                password = password,
                authType = method,
                userId = userId,
                userToken = userToken)

        return if (request.valid()) {
            Transformations.map(userAPI.login(request)) {
                Resource.fromApiResponse(it).map { response ->
                    val tokens = response?.fetchTokens()
                    tokens?.let { tokenResponse -> network.handleTokens(tokenResponse) }

                    val userResponse = response?.stripTokens()
                    handleUserResponse(userResponse)

                    updateLocalData(userResponse)
                    user
                }
            }.apply { (this as? MutableLiveData<Resource<User>>)?.value = Resource.loading(null) }
        } else {
            MutableLiveData<Resource<User>>().apply {
                value = Resource.error(DataError(type = DataErrorType.API, subType = DataErrorSubType.INVALID_DATA))
            }
        }
    }

    fun registerUser(firstName: String, lastName: String? = null, mobileNumber: String? = null, postcode: String? = null, dateOfBirth: Date? = null, email: String, password: String): LiveData<Resource<User>> {
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

        return Transformations.map(userAPI.register(request)) {
            Resource.fromApiResponse(it).map { response ->
                val tokens = response?.fetchTokens()
                tokens?.let { tokenResponse -> network.handleTokens(tokenResponse) }

                val userResponse = response?.stripTokens()
                handleUserResponse(userResponse)

                updateLocalData(userResponse)
                user
            }
        }.apply { (this as? MutableLiveData<Resource<User>>)?.value = Resource.loading(null) }
    }

    fun resetPassword(email: String): LiveData<Resource<Void>> =
            Transformations.map(userAPI.resetPassword(UserResetPasswordRequest(email))) {
                Resource.fromApiResponse(it)
            }

    fun refreshUser(): LiveData<Resource<User>> {
        return Transformations.map(userAPI.fetchUser()) {
            Resource.fromApiResponse(it).map { response ->
                handleUserResponse(response)

                updateLocalData(response)
                user
            }
        }.apply { (this as? MutableLiveData<Resource<User>>)?.value = Resource.loading(null) }
    }

    fun updateUser(user: User): LiveData<Resource<User>> {
        return Transformations.map(userAPI.updateUser(user.updateRequest())) {
            Resource.fromApiResponse(it).map { response ->
                handleUserResponse(response)

                updateLocalData(response)
                this.user
            }
        }.apply { (this as? MutableLiveData<Resource<User>>)?.value = Resource.loading(null) }
    }

    fun updateAttribution(attribution: Attribution): LiveData<Resource<User>> {
        return Transformations.map(userAPI.updateUser(UserUpdateRequest(attribution = attribution))) {
            Resource.fromApiResponse(it).map { response ->
                handleUserResponse(response)

                updateLocalData(response)
                this.user
            }
        }.apply { (this as? MutableLiveData<Resource<User>>)?.value = Resource.loading(null) }
    }

    fun deleteUser() {
        // TODO: To be implemented
    }

    fun authenticateRequest(request: Request) =
            network.authenticateRequest(request)

    fun logoutUser() {
        // TODO: To be implemented
    }

    private fun updateLocalData(response: UserResponse?) {
        user = response?.toUser()
    }

    private fun fetchUserAsLiveData(): LiveData<Resource<User>> =
            Transformations.map(db.users().loadAsLiveData()) {
                Resource.success(it?.toUser())
            }.apply { (this as? MutableLiveData<Resource<User>>)?.value = Resource.loading(null) }

    private fun handleUserResponse(userResponse: UserResponse?) {
        userResponse?.let {
            if (!loggedIn) loggedIn = true

            it.features?.let { features -> pref.features = features }

            doAsync {
                db.users().insert(it)

                notify(ACTION_USER_UPDATED)
            }
        }
    }

    internal fun reset() {
        loggedIn = false
        user = null
        network.reset()
    }
}