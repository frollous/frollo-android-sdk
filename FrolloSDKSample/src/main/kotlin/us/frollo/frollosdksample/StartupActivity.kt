package us.frollo.frollosdksample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_startup.*
import org.jetbrains.anko.startActivity
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.core.SetupParams
import us.frollo.frollosdk.logging.LogLevel

class StartupActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "StartupActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_startup)

        val serverUrl = "https://api-sandbox.frollo.us"

        val setupParams = SetupParams.Builder()
                .serverUrl(serverUrl = serverUrl)
                .logLevel(logLevel = LogLevel.DEBUG)
                .build()

        progress_bar.show()

        if (FrolloSDK.isSetup) {
            completeStartup()
        } else {
            FrolloSDK.setup(application, setupParams = setupParams) { error ->
                if (error != null)
                    Log.e(TAG, error.localizedDescription)
                else
                    completeStartup()
            }
        }
    }

    private fun completeStartup() {
        progress_bar.hide()

        if (FrolloSDK.authentication.loggedIn) {
            handleNotification()
            FrolloSDK.refreshData()
            startActivity<MainActivity>()
        } else {
            startActivity<LoginActivity>()
        }

        finish()
    }

    private fun handleNotification() {
        intent.extras?.let {
            FrolloSDK.notifications.handlePushNotification(it)
        }
    }
}
