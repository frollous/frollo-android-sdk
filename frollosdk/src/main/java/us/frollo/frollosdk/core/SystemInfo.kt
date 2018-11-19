package us.frollo.frollosdk.core

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import timber.log.Timber

class SystemInfo(private val app: Application) {
    private val packageInfo: PackageInfo?
        get() = try {
            app.packageManager.getPackageInfo(app.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.w(e)
            null
        }

    internal val appVersion: String?
        get() = packageInfo?.versionName ?: "1.0"

    internal val appCode: Int
        get() = packageInfo?.versionCode ?: 1

    internal val packageName: String?
        get() = app.packageName

    internal val osVersionName: String
        get() = Build.VERSION.RELEASE
}