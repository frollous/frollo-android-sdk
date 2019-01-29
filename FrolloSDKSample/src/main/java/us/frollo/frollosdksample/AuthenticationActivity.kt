package us.frollo.frollosdksample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_authentication.*
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.auth.AuthType
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.ACTION.ACTION_USER_UPDATED
import us.frollo.frollosdk.error.APIError
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.model.coredata.user.User
import java.util.*

class AuthenticationActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AuthenticationActivity"
    }

    private var user: User? = null
    private var userLiveData: LiveData<Resource<User>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_authentication)

        initView()
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
        btn_device_update.setOnClickListener { updateDevice() }
    }

    private fun login() {
        Log.d(TAG, "Logging in...")

        FrolloSDK.authentication.loginUser(
                method = AuthType.EMAIL,
                email = "deepak@frollo.us",
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
                    Log.d(TAG,"*** Hello ${ user?.firstName } ${ user?.lastName }")
                    Log.d(TAG,"*** Email: ${ user?.email }")
                }
                Resource.Status.ERROR -> Log.e(TAG,"Error fetching user from cache")
                Resource.Status.LOADING -> Log.d(TAG,"Fetching user from cache...")
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
            Log.d(TAG,"*** User refreshed")
        }
    }

    private fun updateUser(user: User) {
        user.lastName = Random().nextInt().toString()

        FrolloSDK.authentication.updateUser(user) { error ->
            if (error != null) handleError(error)
            else Log.d(TAG,"*** Updated last name")
        }
    }

    private fun register() {
        Log.i(TAG,"Registering...")

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
            else Log.d(TAG,"*** Password is reset")
        }
    }

    private fun changePassword() {
        FrolloSDK.authentication.changePassword(currentPassword = "pass1234", newPassword = "P123") { error ->
            if (error != null) handleError(error)
            else Log.d(TAG,"*** Password is changed")
        }
    }

    private fun logout() {
        userLiveData?.removeObservers(this)
        FrolloSDK.logout {
            Log.d(TAG,"*** User logged out")
        }
    }

    private fun deleteUser() {
        userLiveData?.removeObservers(this)
        /*FrolloSDK.deleteUser { error ->
            if (error != null) handleError(error)
            else Log.d(TAG, "*** User deleted")
        }*/
    }

    private fun updateDevice() {
        FrolloSDK.refreshData()
    }

    private fun handleError(error: FrolloSDKError) {
        when (error) {
            is APIError -> Log.e(TAG,"*** Error: ${error.errorCode} - ${error.localizedDescription}")
            is DataError -> Log.e(TAG,"*** Error: ${error.type} - ${error.localizedDescription}")
            else -> Log.e(TAG,"*** Error: ${error.localizedDescription}")
        }
    }
}
