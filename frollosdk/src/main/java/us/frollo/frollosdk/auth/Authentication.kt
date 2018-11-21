package us.frollo.frollosdk.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.jetbrains.anko.doAsync
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.data.local.SDKDatabase
import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.data.remote.endpoints.UserEndpoint
import us.frollo.frollosdk.extensions.fetchTokens
import us.frollo.frollosdk.extensions.stripTokens
import us.frollo.frollosdk.mapping.toUser
import us.frollo.frollosdk.model.api.user.UserLoginRequest
import us.frollo.frollosdk.model.api.user.UserResponse
import us.frollo.frollosdk.model.coredata.user.User
import us.frollo.frollosdk.preferences.Preferences

class Authentication(private val di: DeviceInfo, private val network: NetworkService, private val db: SDKDatabase, private val pref: Preferences) {

    /*
     * Fetches synchronously from DB. Do not call this from main thread.
     */
    var user: User? = null
        get() = fetchUser()

    var userLiveData: LiveData<Resource<User>>? = null
        get() = fetchUserAsLiveData()

    var loggedIn: Boolean
        get() = pref.loggedIn
        private set(value) { pref.loggedIn = value }

    private val userEndpoint: UserEndpoint = network.create(UserEndpoint::class.java)

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

        return Transformations.map(userEndpoint.login(request)) {
            Resource.fromApiResponse(it).map { response ->
                val tokens = response?.fetchTokens()
                tokens?.let { tokenResponse ->  network.handleTokens(tokenResponse) }
                val user = response?.stripTokens()
                handleUserResponse(user)
                user?.toUser()
            }
        }.apply { (this as? MutableLiveData<Resource<User>>)?.value = Resource.loading(null) }
    }

    private fun fetchUser() = db.users().load().toUser()

    private fun fetchUserAsLiveData(): LiveData<Resource<User>> =
            Transformations.map(db.users().loadAsLiveData()) {
                Resource.success(it.toUser())
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

    fun logoutUser() {
        // TODO: To be implemented
    }

    internal fun reset() {
        loggedIn = false
        network.reset()
    }
}