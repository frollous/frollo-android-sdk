package us.frollo.frollosdkapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.auth.AuthType
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.error.APIError
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.model.coredata.user.User

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        FrolloSDK.setup(application, SetupParams.Builder().serverUrl("https://api-sandbox.frollo.us").build()) { error ->
            if (error == null) login()
        }
    }

    private fun login() {
        val liveData = FrolloSDK.getAuthentication().loginUser(AuthType.EMAIL, "deepak@frollo.us", "pass1234")
        liveData.observe(this, observer)
    }

    private val observer = Observer<Resource<User>> {
        when (it?.status) {
            Resource.Status.SUCCESS -> {
                val user = it.data as User
                Log.d("MainActivity", "Hello ${ user.firstName }")
            }
            Resource.Status.ERROR -> Log.d("MainActivity", "Error logging in: " +
                    if (it.error is APIError) (it.error as APIError).statusCode
                    else (it.error as FrolloSDKError).localizedDescription
            )
            Resource.Status.LOADING -> Log.d("MainActivity", "Logging in...")
        }
    }
}
