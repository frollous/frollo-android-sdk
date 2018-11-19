package us.frollo.frollosdk.preferences

import android.content.Context

internal class Preferences(context: Context) {
    companion object {
        private const val PREFERENCES = "pref_frollosdk"
        private const val KEY_USER_LOGGED_IN = "key_user_logged_in"
        private const val KEY_APP_VERSION_LAST = "key_app_version_last"
    }

    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

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