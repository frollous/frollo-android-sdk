package us.frollo.frollosdkapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.auth.AuthType
import us.frollo.frollosdk.base.api.Resource
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.model.api.user.UserResponse

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        FrolloSDK.setup(application, SetupParams.Builder().serverUrl("https://api-sandbox.frollo.us").build()) {
            when (it.status) {
                Resource.Status.SUCCESS -> login()
                Resource.Status.ERROR -> Log.d("MainActivity", "Setup failed")
                Resource.Status.LOADING -> Log.d("MainActivity", "Setup in progress")
            }
        }
    }

    private fun login() {
        val liveData = FrolloSDK.getAuthentication().loginUser(AuthType.EMAIL, "deepak@frollo.us", "pass1234")
        liveData.observe(this, observer)
    }

    private val observer = Observer<Resource<UserResponse>> {
        when (it?.status) {
            Resource.Status.SUCCESS -> {
                val user = it.data as UserResponse
                Log.d("MainActivity", "Hello ${ user.firstName }")
            }
            Resource.Status.ERROR -> Log.d("MainActivity", "Error logging in: " + it.message)
            Resource.Status.LOADING -> Log.d("MainActivity", "Logging in...")
        }
    }
}
