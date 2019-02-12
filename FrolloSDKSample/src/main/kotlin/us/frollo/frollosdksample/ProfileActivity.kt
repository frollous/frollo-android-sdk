package us.frollo.frollosdksample

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_profile.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdksample.base.BaseStackActivity

class ProfileActivity : BaseStackActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)

        btn_logout.setOnClickListener { logout() }
    }

    private fun logout() {
        alert("Are you sure you want to logout?", "Logout") {
            positiveButton("Yes") {
                FrolloSDK.logout()
                startActivity<LoginActivity>()
                finishAffinity()
            }
            negativeButton("No") {}
        }.showThemed()
    }
}
