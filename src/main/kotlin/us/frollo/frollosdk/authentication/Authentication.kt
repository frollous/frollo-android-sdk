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

package us.frollo.frollosdk.authentication

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import okhttp3.Request
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.ACTION
import us.frollo.frollosdk.core.ACTION.ACTION_USER_UPDATED
import us.frollo.frollosdk.core.ARGUMENT
import us.frollo.frollosdk.core.DeviceInfo
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.error.OAuth2Error
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.notify
import us.frollo.frollosdk.extensions.toString
import us.frollo.frollosdk.extensions.updateRequest
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.DeviceAPI
import us.frollo.frollosdk.network.api.UserAPI
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toUser
import us.frollo.frollosdk.model.api.device.DeviceUpdateRequest
import us.frollo.frollosdk.model.api.user.UserChangePasswordRequest
import us.frollo.frollosdk.model.api.user.UserRegisterRequest
import us.frollo.frollosdk.model.api.user.UserResetPasswordRequest
import us.frollo.frollosdk.model.api.user.UserResponse
import us.frollo.frollosdk.model.api.user.UserUpdateRequest
import us.frollo.frollosdk.model.coredata.user.Address
import us.frollo.frollosdk.model.coredata.user.Attribution
import us.frollo.frollosdk.model.coredata.user.User
import us.frollo.frollosdk.network.ErrorResponseType
import us.frollo.frollosdk.network.api.TokenAPI
import us.frollo.frollosdk.preferences.Preferences
import java.util.Date
import java.util.TimeZone

/**
 * Manages authentication, login, registration, logout and the user profile.
 */
class Authentication(private val oAuth: OAuth, private val di: DeviceInfo, private val network: NetworkService, private val db: SDKDatabase, private val pref: Preferences) {

    companion object {
        private const val TAG = "Authentication"

        /** Request Code for the Authorization Intent for Web based login */
        const val RC_AUTH = 100
    }

    /**
     * Indicates if the user is currently authorised with Frollo
     */
    var loggedIn: Boolean
        get() = pref.loggedIn
        private set(value) { pref.loggedIn = value }

    private val userAPI: UserAPI = network.create(UserAPI::class.java)
    private val deviceAPI: DeviceAPI = network.create(DeviceAPI::class.java)
    private val tokenAPI: TokenAPI = network.createAuth(TokenAPI::class.java)

    private var codeVerifier: String? = null

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
     * Login a user via WebView
     *
     * Initiate the authorization code login flow using a WebView
     *
     * @param activity Activity from which the ChromeTabs/Browser should be launched
     * @param completedIntent PendingIntent of an Activity to which the completed response from the ChromeTabs/Browser is delivered
     * @param cancelledIntent PendingIntent of an Activity to which the cancelled response from the ChromeTabs/Browser is delivered
     * @param toolBarColor Color of the CustomTabs toolbar using getColor() method
     *
     * NOTE: When using this method you need to call [handleWebLoginResponse]
     * in the onCreate() of the pending intent activity
     */
    @Throws(DataError::class)
    fun loginUserUsingWeb(activity: Activity, completedIntent: PendingIntent, cancelledIntent: PendingIntent, toolBarColor: Int? = null) {
        if (!oAuth.config.validForAuthorizationCodeFlow()) {
            throw DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)
        }

        val authRequest = oAuth.getAuthorizationRequest()

        codeVerifier = authRequest.codeVerifier

        val authService = AuthorizationService(activity)
        val authIntent = oAuth.getCustomTabsIntent(authService, authRequest, toolBarColor)

        authService.performAuthorizationRequest(authRequest, completedIntent, cancelledIntent, authIntent)
    }

    /**
     * Login a user via WebView
     *
     * Initiate the authorization code login flow using a WebView
     *
     * @param activity Activity from which the ChromeTabs/Browser should be launched
     * @param toolBarColor Color of the CustomTabs toolbar using getColor() method
     *
     * NOTE: When using this method you need to call [handleWebLoginResponse]
     * in the onActivityResult() of the activity from which you call this method
     */
    @Throws(DataError::class)
    fun loginUserUsingWeb(activity: Activity, toolBarColor: Int? = null) {
        if (!oAuth.config.validForAuthorizationCodeFlow()) {
            throw DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)
        }

        val authRequest = oAuth.getAuthorizationRequest()

        codeVerifier = authRequest.codeVerifier

        val authService = AuthorizationService(activity)
        val tabsIntent = oAuth.getCustomTabsIntent(authService, authRequest, toolBarColor)
        val authIntent = authService.getAuthorizationRequestIntent(authRequest, tabsIntent)

        activity.startActivityForResult(authIntent, RC_AUTH)
    }

    /**
     * Process the authorization response to continue WebView login flow
     *
     * @param authIntent Response intent received from WebView in onActivityResult or in onCreate of the pending intent Activity
     * @param completion: Completion handler with any error that occurred
     */
    fun handleWebLoginResponse(authIntent: Intent?, completion: OnFrolloSDKCompletionListener<Result>) {
        authIntent?.let {
            val response = AuthorizationResponse.fromIntent(authIntent)
            val exception = AuthorizationException.fromIntent(authIntent)

            val authorizationCode = response?.authorizationCode

            if (authorizationCode != null) {
                exchangeAuthorizationCode(code = authorizationCode, codeVerifier = codeVerifier, completion = completion)
            } else {
                completion.invoke(Result.error(OAuth2Error(exception = exception)))
            }
        } ?: run {
            completion.invoke(Result.error(DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)))
        }
    }

    /**
     * Login a user using email and password
     *
     * @param email Email address of the user
     * @param password Password for the user
     * @param completion: Completion handler with any error that occurred
     */
    fun loginUser(email: String, password: String, completion: OnFrolloSDKCompletionListener<Result>) {
        if (loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.ALREADY_LOGGED_IN)
            completion.invoke(Result.error(error))
            return
        }

        if (!oAuth.config.validForROPC()) {
            completion.invoke(Result.error(DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)))
            return
        }

        val request = oAuth.getLoginRequest(username = email, password = password)
        if (!request.valid) {
            completion.invoke(Result.error(DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)))
            return
        }

        // Authorize the user
        tokenAPI.refreshTokens(request).enqueue(ErrorResponseType.OAUTH2) { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#loginUser.refreshTokens", resource.error?.localizedDescription)

                    completion.invoke(Result.error(resource.error))
                }

                Resource.Status.SUCCESS -> {
                    resource.data?.let { response ->
                        network.handleTokens(response)

                        // Fetch core details about the user. Fail and logout if we don't get necessary details
                        userAPI.fetchUser().enqueue { resource ->
                            when (resource.status) {
                                Resource.Status.ERROR -> {
                                    network.reset()

                                    Log.e("$TAG#loginUser.fetchUser", resource.error?.localizedDescription)
                                    completion.invoke(Result.error(resource.error))
                                }

                                Resource.Status.SUCCESS -> {
                                    handleUserResponse(resource.data, completion)
                                    updateDevice()
                                }
                            }
                        }
                    } ?: run {
                        completion.invoke(Result.error(DataError(DataErrorType.AUTHENTICATION, DataErrorSubType.MISSING_ACCESS_TOKEN)))
                    }
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
        if (loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.ALREADY_LOGGED_IN)
            completion.invoke(Result.error(error))
            return
        }

        if (!oAuth.config.validForROPC()) {
            completion.invoke(Result.error(DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)))
            return
        }

        // Create the user on the server and at the authorization endpoint
        val request = UserRegisterRequest(
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password,
                currentAddress = if (postcode?.isNotBlank() == true) Address(postcode = postcode) else null,
                mobileNumber = mobileNumber,
                dateOfBirth = dateOfBirth?.toString("yyyy-MM"))

        userAPI.register(request).enqueue { userResource ->
            when (userResource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#registerUser.register", userResource.error?.localizedDescription)
                    completion.invoke(Result.error(userResource.error))
                }

                Resource.Status.SUCCESS -> {
                    // Authenticate the user at the token endpoint after creation
                    val authRequest = oAuth.getRegisterRequest(username = email, password = password)
                    if (!authRequest.valid) {
                        completion.invoke(Result.error(DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)))
                        return@enqueue
                    }

                    tokenAPI.refreshTokens(authRequest).enqueue(ErrorResponseType.OAUTH2) { authResource ->
                        when (authResource.status) {
                            Resource.Status.ERROR -> {
                                Log.e("$TAG#refreshUser.refreshTokens", authResource.error?.localizedDescription)

                                completion.invoke(Result.error(authResource.error))
                            }

                            Resource.Status.SUCCESS -> {
                                authResource.data?.let { authResponse ->
                                    network.handleTokens(authResponse)

                                    handleUserResponse(userResource.data, completion)

                                    updateDevice()
                                } ?: run {
                                    completion.invoke(Result.error(DataError(DataErrorType.AUTHENTICATION, DataErrorSubType.MISSING_ACCESS_TOKEN)))
                                }
                            }
                        }
                    }
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
            when (resource.status) {
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
            when (resource.status) {
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
            when (resource.status) {
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
            when (resource.status) {
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
                when (resource.status) {
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
            when (resource.status) {
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
     * Exchange an authorization code and code verifier for a token
     *
     * @param code Authorization code
     * @param codeVerifier Authorization code verifier for PKCE (Optional)
     * @param completion Completion handler with any error that occurred
     */
    fun exchangeAuthorizationCode(code: String, codeVerifier: String? = null, completion: OnFrolloSDKCompletionListener<Result>) {
        if (loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.ALREADY_LOGGED_IN)
            completion.invoke(Result.error(error))
            return
        }

        val request = oAuth.getExchangeAuthorizationCodeRequest(code = code, codeVerifier = codeVerifier)
        if (!request.valid) {
            completion.invoke(Result.error(DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)))
            return
        }

        // Authorize the user
        tokenAPI.refreshTokens(request).enqueue(ErrorResponseType.OAUTH2) { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#exchangeAuthorizationCode.refreshTokens", resource.error?.localizedDescription)

                    completion.invoke(Result.error(resource.error))
                }

                Resource.Status.SUCCESS -> {
                    resource.data?.let { response ->
                        network.handleTokens(response)

                        // Fetch core details about the user. Fail and logout if we don't get necessary details
                        userAPI.fetchUser().enqueue { resource ->
                            when (resource.status) {
                                Resource.Status.ERROR -> {
                                    network.reset()

                                    Log.e("$TAG#exchangeAuthorizationCode.fetchUser", resource.error?.localizedDescription)
                                    completion.invoke(Result.error(resource.error))
                                }

                                Resource.Status.SUCCESS -> {
                                    handleUserResponse(resource.data, completion)
                                    updateDevice()
                                }
                            }
                        }
                    } ?: run {
                        completion.invoke(Result.error(DataError(DataErrorType.AUTHENTICATION, DataErrorSubType.MISSING_ACCESS_TOKEN)))
                    }
                }
            }
        }
    }

    /**
     * Exchange a legacy refresh token for a new valid refresh access token pair.
     *
     * @param legacyToken Legacy refresh token to be exchanged
     * @param completion Completion handler with any error that occurred
     */
    fun exchangeLegacyToken(legacyToken: String, completion: OnFrolloSDKCompletionListener<Result>) {
        val request = oAuth.getExchangeTokenRequest(legacyToken = legacyToken)
        if (!request.valid) {
            completion.invoke(Result.error(DataError(DataErrorType.API, DataErrorSubType.INVALID_DATA)))
            return
        }

        tokenAPI.refreshTokens(request).enqueue(ErrorResponseType.OAUTH2) { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#exchangeToken.refreshTokens", resource.error?.localizedDescription)

                    network.reset()

                    completion.invoke(Result.error(resource.error))
                }

                Resource.Status.SUCCESS -> {
                    resource.data?.let { response ->
                        network.handleTokens(response)

                        // Fetch core details about the user. Fail and logout if we don't get necessary details
                        userAPI.fetchUser().enqueue { resource ->
                            when (resource.status) {
                                Resource.Status.ERROR -> {
                                    network.reset()

                                    Log.e("$TAG#loginUser.fetchUser", resource.error?.localizedDescription)
                                    completion.invoke(Result.error(resource.error))
                                }

                                Resource.Status.SUCCESS -> {
                                    handleUserResponse(resource.data, completion)
                                    updateDevice()
                                }
                            }
                        }
                    } ?: run {
                        completion.invoke(Result.error(DataError(DataErrorType.AUTHENTICATION, DataErrorSubType.MISSING_ACCESS_TOKEN)))
                    }
                }
            }
        }
    }

    /**
     * Refresh Access Token
     *
     * Forces a refresh of the access token using the refresh token if a 401 was encountered. For advanced usage only in combination with web request authentication.
     *
     * @param completion Completion handler with any error that occurred (Optional)
     */
    fun refreshTokens(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        network.refreshTokens(completion)
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
                deviceId = di.deviceId,
                deviceType = di.deviceType,
                deviceName = di.deviceName,
                notificationToken = notificationToken,
                timezone = TimeZone.getDefault().id,
                compliant = compliant)

        deviceAPI.updateDevice(request).enqueue { resource ->
            when (resource.status) {
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
     * Log out the user and reset the token storage.
     */
    internal fun logoutUser() {
        if (!loggedIn) {
            return
        }

        reset()
    }

    private fun handleUserResponse(userResponse: UserResponse?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        userResponse?.let {
            if (!loggedIn) {
                loggedIn = true

                notify(ACTION.ACTION_AUTHENTICATION_CHANGED,
                        bundleOf(Pair(ARGUMENT.ARG_AUTHENTICATION_STATUS, AuthenticationStatus.AUTHENTICATED)))
            }

            it.features?.let { features -> pref.features = features }

            doAsync {
                db.users().insert(it.toUser())

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