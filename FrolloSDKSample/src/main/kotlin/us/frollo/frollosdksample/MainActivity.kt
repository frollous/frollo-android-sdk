package us.frollo.frollosdksample

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import us.frollo.frollosdk.FrolloSDK

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val DEFAULT_PAGE = R.id.nav_accounts
    }

    private val messagesFragment = MessagesFragment()
    private val accountsFragment = ProviderAccountsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        registerPushNotification()

        navigation.setOnNavigationItemSelectedListener {
            bottomNavSelected(it.itemId)
            true
        }

        bottomNavSelected(DEFAULT_PAGE)
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
        val fragment = when (itemId) {
            R.id.nav_messages -> messagesFragment
            R.id.nav_accounts -> accountsFragment
            else -> null
        }

        fragment?.let {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container, it)
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
            R.id.menu_profile -> {
                startActivity<ProfileActivity>()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
