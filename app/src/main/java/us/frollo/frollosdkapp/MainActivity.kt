package us.frollo.frollosdkapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.auth.AuthType
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.ACTION.ACTION_USER_UPDATED
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.error.APIError
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.model.coredata.user.User
import java.util.*

class MainActivity : AppCompatActivity() {

    private var user: User? = null
    private var userLiveData: LiveData<Resource<User>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (!FrolloSDK.isSetup) {
            FrolloSDK.setup(application, SetupParams.Builder().serverUrl("https://api-sandbox.frollo.us").build()) { error ->
                if (error == null) {
                    initView()
                }
            }
        }
    }

    private fun initView() {
        btn_register.setOnClickListener { register() }
        btn_login.setOnClickListener { login() }
        btn_fetch_user.setOnClickListener { fetchUser() }
        btn_refresh_user.setOnClickListener { refreshUser() }
        btn_update_user.setOnClickListener { user?.let(::updateUser) }
        btn_reset_password.setOnClickListener { resetPassword() }
        btn_change_password.setOnClickListener { changePassword() }
        btn_delete_user.setOnClickListener { deleteUser() }
        btn_logout.setOnClickListener { logout() }
    }

    private fun login() {
        Timber.d("Logging in...")

        FrolloSDK.authentication.loginUser(
                method = AuthType.EMAIL,
                email = "testtest1@frollo.us",
                password = "pass1234") { error ->

            if (error != null) handleError(error)
            else fetchUser()
        }
    }

    private fun fetchUser() {
        userLiveData = FrolloSDK.authentication.fetchUser()
        userLiveData?.observe(this) {
            when (it?.status) {
                Resource.Status.SUCCESS -> {
                    user = it.data
                    Timber.d("*** Hello ${ user?.firstName } ${ user?.lastName }")
                    Timber.d("*** Email: ${ user?.email }")
                }
                Resource.Status.ERROR -> Timber.d("Error fetching user from cache")
                Resource.Status.LOADING -> Timber.d("Fetching user from cache...")
            }
        }
    }

    private fun refreshUser() {
        val localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.registerReceiver(userRefreshReceiver, IntentFilter(ACTION_USER_UPDATED))

        FrolloSDK.authentication.refreshUser()
    }

    private val userRefreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d("*** User refreshed")
        }
    }

    private fun updateUser(user: User) {
        user.lastName = Random().nextInt().toString()

        FrolloSDK.authentication.updateUser(user) { error ->
            if (error != null) handleError(error)
            else Timber.d("*** Updated last name")
        }
    }

    private fun register() {
        Timber.d("Registering...")

        FrolloSDK.authentication.registerUser(
                firstName = "test first",
                lastName = "test last",
                mobileNumber = "0411111111",
                postcode = "2050",
                dateOfBirth = Date(),
                email = "testtest1@frollo.us",
                password = "pass1234") { error ->

            if (error != null) handleError(error)
            else fetchUser()
        }
    }

    private fun resetPassword() {
        FrolloSDK.authentication.resetPassword(email = "testtest1@frollo.us") { error ->
            if (error != null) handleError(error)
            else Timber.d("*** Password is reset")
        }
    }

    private fun changePassword() {
        FrolloSDK.authentication.changePassword(currentPassword = "pass1234", newPassword = "P123") { error ->
            if (error != null) handleError(error)
            else Timber.d("*** Password is changed")
        }
    }

    private fun logout() {
        userLiveData?.removeObservers(this)
        FrolloSDK.logout {
            Timber.d("*** User logged out")
        }
    }

    private fun deleteUser() {
        userLiveData?.removeObservers(this)
        FrolloSDK.deleteUser { error ->
            if (error != null) handleError(error)
            else Timber.d("*** User deleted")
        }
    }

    private fun handleError(error: FrolloSDKError) {
        when (error) {
            is APIError -> Timber.d("*** Error: ${error.errorCode} - ${error.localizedDescription}")
            is DataError -> Timber.d("*** Error: ${error.type} - ${error.localizedDescription}")
            else -> Timber.d("*** Error: ${error.localizedDescription}")
        }
    }
}
