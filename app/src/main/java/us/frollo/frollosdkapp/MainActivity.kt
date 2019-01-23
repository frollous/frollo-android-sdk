package us.frollo.frollosdkapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import timber.log.Timber
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.core.SetupParams

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (!FrolloSDK.isSetup) {
            FrolloSDK.setup(application, SetupParams.Builder().serverUrl("https://api-sandbox.frollo.us").build()) { error ->
                if (error != null) {
                    Timber.d("SDK Setup Failed: ${ error.localizedDescription }")
                    return@setup
                }

                init()
            }
        } else {
            init()
        }
    }

    private fun init() {
        registerPushNotification()
        processIntent()
        initView()
    }

    private fun initView() {
        btn_authentication.setOnClickListener { startActivity<AuthenticationActivity>() }
        btn_messages.setOnClickListener { startActivity<MessagesActivity>() }
        btn_events.setOnClickListener { startActivity<EventsActivity>() }
    }

    private fun registerPushNotification() {
        if (FrolloSDK.isSetup && FrolloSDK.authentication.loggedIn) {
            FirebaseInstanceId.getInstance().instanceId
                    .addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Timber.d("getInstanceId failed: ${ task.exception }")
                            return@OnCompleteListener
                        }

                        val token = task.result?.token
                        token?.let { FrolloSDK.notifications.registerPushNotificationToken(it) }
                    })
        }
    }

    private fun processIntent() {
        intent.extras?.let {
            // Launched via notification
            FrolloSDK.notifications.handlePushNotification(it)
        }
    }
}
