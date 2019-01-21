package us.frollo.frollosdkapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.core.SetupParams

class MainActivity : AppCompatActivity() {

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
        btn_authentication.setOnClickListener { startActivity<AuthenticationActivity>() }
        btn_messages.setOnClickListener { startActivity<MessagesActivity>() }
    }
}
