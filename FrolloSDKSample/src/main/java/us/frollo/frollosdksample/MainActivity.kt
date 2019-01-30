package us.frollo.frollosdksample

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity
import us.frollo.frollosdk.FrolloSDK

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val messagesFragment = MessagesFragment()
    private val accountsFragment = AccountsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        registerPushNotification()

        navigation.setOnNavigationItemSelectedListener {
            bottomNavSelected(it.itemId)
            true
        }

        bottomNavSelected(R.id.nav_messages)
    }

    private fun registerPushNotification() {
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.e(TAG, "getInstanceId failed: ${ task.exception }")
                        return@OnCompleteListener
                    }

                    val token = task.result?.token
                    token?.let { FrolloSDK.notifications.registerPushNotificationToken(it) }
                })
    }

    private fun bottomNavSelected(itemId: Int) {
        loadFragment(itemId)
    }

    private fun loadFragment(itemId: Int) {
        var fragment : Fragment? = null
        when (itemId) {
            R.id.nav_messages -> {
                supportActionBar?.title = "Messages"
                fragment = messagesFragment
            }
            R.id.nav_accounts -> {
                supportActionBar?.title = "Accounts"
                fragment = accountsFragment
            }
        }

        if (fragment != null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commitAllowingStateLoss()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        alert("Are you sure you want to logout?", "Logout") {
            positiveButton("Yes") {
                FrolloSDK.logout()
                startActivity<LoginActivity>()
                finish()
            }
            negativeButton("No") {}
        }.showThemed()
    }
}
