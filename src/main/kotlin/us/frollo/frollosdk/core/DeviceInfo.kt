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

package us.frollo.frollosdk.core

import android.content.Context
import android.os.Build
import android.provider.Settings

/**
 * @suppress
 */
class DeviceInfo(private val context: Context) {

    internal val deviceId: String
        get() = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    internal val deviceName: String
        get() = Settings.System.getString(context.contentResolver, Settings.Global.DEVICE_NAME) ?: Build.MODEL

    internal val deviceType: String
        get() = if (Build.MODEL.startsWith(Build.MANUFACTURER)) {
            capitalize(Build.MODEL)
        } else {
            capitalize(Build.MANUFACTURER) + " " + Build.MODEL
        }

    private fun capitalize(s: String?): String {
        return if (s?.isNotEmpty() == true)
            if (Character.isLowerCase(s[0])) Character.toUpperCase(s[0]) + s.substring(1)
            else s
        else ""
    }
}