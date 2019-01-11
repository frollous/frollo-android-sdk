package us.frollo.frollosdkapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (!FrolloSDK.isSetup) {
            FrolloSDK.setup(application, SetupParams.Builder().serverUrl("https://api-sandbox.frollo.us").build()) { error ->
                if (error == null)
                    login()
                    //register()
            }
        }
    }

    private fun login() {
        FrolloSDK.authentication.loginUser(AuthType.EMAIL, "deepak@frollo.us", "pass1234").observe(this) {
            when (it?.status) {
                Resource.Status.SUCCESS -> {
                    val user = it.data
                    Timber.d("*** Hello ${ user?.firstName }")
                    fetchUser()
                    refreshUser()
                    user?.let { updateUser(user) }
                }
                Resource.Status.ERROR -> Timber.d("Error logging in: " +
                        if (it.error is APIError) (it.error as APIError).debugDescription
                        else if (it.error is DataError) (it.error as DataError).debugDescription
                        else (it.error as FrolloSDKError).debugDescription
                )
                Resource.Status.LOADING -> Timber.d("Logging in...")
            }
        }
    }

    private fun fetchUser() {
        val user = FrolloSDK.authentication.user
        Timber.d("*** Name: ${ user?.firstName } ${ user?.lastName }")
        Timber.d("*** Email: ${ user?.email }")
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
        FrolloSDK.authentication.updateUser(user).observe(this) {
            when (it?.status) {
                Resource.Status.SUCCESS -> Timber.d("*** Updated last name: ${ it.data?.lastName }")
                Resource.Status.ERROR -> Timber.d("Error updating")
                Resource.Status.LOADING -> Timber.d("Updating...")
            }
        }
    }

    /*private fun register() {
        FrolloSDK.authentication.registerUser(
                firstName = "test first",
                lastName = "test last",
                mobileNumber = "0411111111",
                postcode = "2050",
                dateOfBirth = Date(),
                email = "testtest@frollo.us",
                password = "pass1234").observe(this) {
            when (it?.status) {
                Resource.Status.SUCCESS -> Timber.d("*** Welcome ${ it.data?.firstName }")
                Resource.Status.ERROR -> Timber.d("Error registering: " +
                        if (it.error is APIError) (it.error as APIError).debugDescription
                        else if (it.error is DataError) (it.error as DataError).debugDescription
                        else (it.error as FrolloSDKError).debugDescription
                )
                Resource.Status.LOADING -> Timber.d("Registering...")
            }
        }
    }*/
}
