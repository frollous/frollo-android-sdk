package us.frollo.frollosdk.extensions

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import us.frollo.frollosdk.FrolloSDK

class UtilityExtensionAndroidTest {

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    private var notifyFlag = false

    @Before
    fun setup() {
        FrolloSDK.app = app
    }

    @Test
    fun testNotify() {
        val lbm = LocalBroadcastManager.getInstance(app)
        lbm.registerReceiver(receiver, IntentFilter("ACTION_NOTIFY"))

        notify("ACTION_NOTIFY")

        Thread.sleep(2000)

        assertTrue(notifyFlag)
        lbm.unregisterReceiver(receiver)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            notifyFlag = true
        }
    }
}