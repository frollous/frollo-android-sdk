package us.frollo.frollosdkapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import timber.log.Timber
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

        if (!FrolloSDK.isSetup) {
            FrolloSDK.setup(application, SetupParams.Builder().serverUrl("https://api-sandbox.frollo.us").build()) { error ->
                if (error == null) login()
            }
        }
    }

    private fun login() {
        FrolloSDK.authentication.loginUser(AuthType.EMAIL, "deepak@frollo.us", "pass1234").observe(this, observer)
    }

    private val observer = Observer<Resource<User>> {
        when (it?.status) {
            Resource.Status.SUCCESS -> {
                val user = FrolloSDK.authentication.user
                Timber.d("Hello ${ user?.firstName }")
            }
            Resource.Status.ERROR -> Timber.d("Error logging in: " +
                    if (it.error is APIError) (it.error as APIError).statusCode
                    else (it.error as FrolloSDKError).localizedDescription
            )
            Resource.Status.LOADING -> Timber.d("Logging in...")
        }
    }
}
