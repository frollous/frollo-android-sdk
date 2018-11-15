package us.frollo.frollosdk.core

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import timber.log.Timber

internal class SystemInfo(private val app: Application) {
    private val packageInfo: PackageInfo?
        get() = try {
            app.packageManager.getPackageInfo(app.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.w(e)
            null
        }

    val appVersion: String?
        get() = packageInfo?.versionName ?: "1.0"

    val appCode: Int
        get() = packageInfo?.versionCode ?: 1

    val packageName: String?
        get() = app.packageName

    val osVersionName: String
        get() = Build.VERSION.RELEASE
}