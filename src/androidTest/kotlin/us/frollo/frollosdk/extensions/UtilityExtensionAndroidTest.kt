/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.frollo.frollosdk.extensions

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.authentication.AuthenticationStatus
import us.frollo.frollosdk.core.ARGUMENT.ARG_DATA

class UtilityExtensionAndroidTest {

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    private var dataType = TestDataType.STRING
    private var notifyFlag = false
    private var serializableData = AuthenticationStatus.AUTHENTICATED
    private var longArrayData: LongArray? = longArrayOf()
    private var stringData: String? = ""
    private lateinit var lbm: LocalBroadcastManager

    @Before
    fun setup() {
        FrolloSDK.app = app
        notifyFlag = false
        lbm = LocalBroadcastManager.getInstance(app)
        lbm.registerReceiver(receiver, IntentFilter("ACTION_NOTIFY"))
    }

    @After
    fun tearDown() {
        notifyFlag = false
        lbm.unregisterReceiver(receiver)
    }

    @Test
    fun testNotifySerializable() {
        dataType = TestDataType.SERIALIZABLE

        notify(
            action = "ACTION_NOTIFY",
            extrasKey = ARG_DATA,
            extrasData = AuthenticationStatus.LOGGED_OUT
        )

        Thread.sleep(2000)

        assertTrue(notifyFlag)
        assertEquals(AuthenticationStatus.LOGGED_OUT, serializableData)
    }

    @Test
    fun testNotifyString() {
        dataType = TestDataType.STRING

        notify(
            action = "ACTION_NOTIFY",
            extrasKey = ARG_DATA,
            extrasData = "Test String"
        )

        Thread.sleep(2000)

        assertTrue(notifyFlag)
        assertEquals("Test String", stringData)
    }

    @Test
    fun testNotifyLongArray() {
        dataType = TestDataType.LONG_ARRAY

        notify(
            action = "ACTION_NOTIFY",
            extrasKey = ARG_DATA,
            extrasData = longArrayOf(100, 200)
        )

        Thread.sleep(2000)

        assertTrue(notifyFlag)
        assertEquals(100L, longArrayData?.get(0))
        assertEquals(200L, longArrayData?.get(1))
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            notifyFlag = true
            when (dataType) {
                TestDataType.SERIALIZABLE -> {
                    serializableData = intent.getSerializableExtra(ARG_DATA) as AuthenticationStatus
                }
                TestDataType.STRING -> {
                    stringData = intent.getStringExtra(ARG_DATA)
                }
                TestDataType.LONG_ARRAY -> {
                    longArrayData = intent.getLongArrayExtra(ARG_DATA)
                }
            }
        }
    }
    private enum class TestDataType {
        SERIALIZABLE, STRING, LONG_ARRAY
    }
}
