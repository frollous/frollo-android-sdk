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