package us.frollo.frollosdk.version

import us.frollo.frollosdk.BuildConfig
import us.frollo.frollosdk.preferences.Preferences

internal class Version(private val pref: Preferences) {

    private var currentVersion = BuildConfig.VERSION_NAME
    private var previousVersion: String? = null
    private var versionHistory: MutableList<String>

    init {
        previousVersion = pref.sdkVersion
        versionHistory = pref.sdkVersionHistory
    }

    fun migrationNeeded(): Boolean {
        previousVersion?.let { prev ->
            if (prev != currentVersion) {
                return true
            }
        } ?: run {
            // First install
            initialiseVersion()
        }
        return false
    }

    fun migrateVersion() {
        if (previousVersion == null) return

        // Stubbed for future. Replace null check with let and iterate through versions

        updateVersion()
    }

    private fun initialiseVersion() {
        updateVersion()
    }

    private fun updateVersion() {
        previousVersion = currentVersion
        versionHistory.add(currentVersion)

        pref.sdkVersion = currentVersion
        pref.sdkVersionHistory = versionHistory
    }
}