package us.frollo.frollosdk.preferences

import android.content.Context

internal class Preferences(context: Context) {
    companion object {
        private const val PREFERENCES = "pref_frollosdk"
        private const val KEY_SDK_VERSION = "key_frollosdk_version_current"
        private const val KEY_SDK_VERSION_HISTORY = "key_frollosdk_version_history"
        private const val KEY_USER_LOGGED_IN = "key_frollosdk_user_logged_in"
    }

    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    /** SDK Version */
    var sdkVersion: String?
        get() = preferences.getString(KEY_SDK_VERSION, null)
        set(value) = preferences.edit().putString(KEY_SDK_VERSION, value).apply()

    /** SDK Version History */
    var sdkVersionHistory: MutableList<String>
        get() = preferences.getString(KEY_SDK_VERSION_HISTORY, null)?.split(",")?.toMutableList() ?: mutableListOf()
        set(value) = preferences.edit().putString(KEY_SDK_VERSION_HISTORY, value.joinToString(",")).apply()

    /** User Logged In */
    var loggedIn: Boolean
        get() = preferences.getBoolean(KEY_USER_LOGGED_IN, false)
        set(value) = preferences.edit().putBoolean(KEY_USER_LOGGED_IN, value).apply()
    fun resetLoggedIn() = preferences.edit().remove(KEY_USER_LOGGED_IN).apply()

    fun resetEssential() {
        resetLoggedIn()
    }

    fun resetAll() {
        preferences.edit().clear().apply()
    }
}