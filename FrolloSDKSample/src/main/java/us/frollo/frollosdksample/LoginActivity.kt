package us.frollo.frollosdksample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.startActivity
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.auth.AuthType

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        btn_login.setOnClickListener { attemptLogin() }
    }

    private fun attemptLogin() {
        val email = input_email.text.toString()
        val password = input_password.text.toString()

        if (email.isBlank() || password.isBlank())
            return

        btn_login.hide()
        progress_bar.show()

        FrolloSDK.authentication.loginUser(method = AuthType.EMAIL, email = email, password = password) { error ->
            progress_bar.hide()

            if (error != null) {
                btn_login.show()
                displayError(error.localizedDescription, "Login Failed")
            } else {
                FrolloSDK.refreshData()
                startActivity<MainActivity>()
                finish()
            }
        }
    }
}
