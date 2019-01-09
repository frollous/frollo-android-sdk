package us.frollo.frollosdk.preferences

import android.content.Context
import us.frollo.frollosdk.auth.FeatureType
import us.frollo.frollosdk.data.local.Converters
import us.frollo.frollosdk.model.coredata.user.FeatureFlag

class Preferences(context: Context) {
    companion object {
        private const val PREFERENCES = "pref_frollosdk"
        private const val KEY_SDK_VERSION = "key_frollosdk_version_current"
        private const val KEY_SDK_VERSION_HISTORY = "key_frollosdk_version_history"
        private const val KEY_USER_LOGGED_IN = "key_frollosdk_user_logged_in"
        private const val KEY_USER_FEATURES = "key_frollosdk_user_features"
        private const val KEY_ENCRYPTED_REFRESH_TOKEN = "key_encrypted_refresh_token"
        private const val KEY_ENCRYPTED_ACCESS_TOKEN = "key_encrypted_access_token"
        private const val KEY_ACCESS_TOKEN_EXPIRY = "key_access_token_expiry"
    }

    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    /** SDK Version */
    internal var sdkVersion: String?
        get() = preferences.getString(KEY_SDK_VERSION, null)
        set(value) = preferences.edit().putString(KEY_SDK_VERSION, value).apply()

    /** SDK Version History */
    internal var sdkVersionHistory: MutableList<String>
        get() = preferences.getString(KEY_SDK_VERSION_HISTORY, null)?.split(",")?.toMutableList() ?: mutableListOf()
        set(value) = preferences.edit().putString(KEY_SDK_VERSION_HISTORY, value.joinToString(",")).apply()

    /** User Logged In */
    internal var loggedIn: Boolean
        get() = preferences.getBoolean(KEY_USER_LOGGED_IN, false)
        set(value) = preferences.edit().putBoolean(KEY_USER_LOGGED_IN, value).apply()
    internal fun resetLoggedIn() = preferences.edit().remove(KEY_USER_LOGGED_IN).apply()

    /** User Features */
    internal var features: List<FeatureFlag>
        get() = preferences.getString(KEY_USER_FEATURES, null)?.let { Converters.instance.stringToListOfFeatureFlag(it) } ?: mutableListOf()
        set(value) = preferences.edit().putString(KEY_USER_FEATURES, Converters.instance.stringFromListOfFeatureFlag(value)).apply()

    /** Encrypted Refresh Token */
    internal var encryptedRefreshToken: String?
        get() = preferences.getString(KEY_ENCRYPTED_REFRESH_TOKEN, null)
        set(value) = preferences.edit().putString(KEY_ENCRYPTED_REFRESH_TOKEN, value).apply()
    internal fun resetEncryptedRefreshToken() = preferences.edit().remove(KEY_ENCRYPTED_REFRESH_TOKEN).apply()

    /** Encrypted Access Token */
    internal var encryptedAccessToken: String?
        get() = preferences.getString(KEY_ENCRYPTED_ACCESS_TOKEN, null)
        set(value) = preferences.edit().putString(KEY_ENCRYPTED_ACCESS_TOKEN, value).apply()
    internal fun resetEncryptedAccessToken() = preferences.edit().remove(KEY_ENCRYPTED_ACCESS_TOKEN).apply()

    /** Access Token Expiry */
    internal var accessTokenExpiry: Long
        get() = preferences.getLong(KEY_ACCESS_TOKEN_EXPIRY, -1)
        set(value) = preferences.edit().putLong(KEY_ACCESS_TOKEN_EXPIRY, value).apply()
    internal fun resetAccessTokenExpiry() = preferences.edit().remove(KEY_ACCESS_TOKEN_EXPIRY).apply()

    internal fun reset() {
        preferences.edit().clear().apply()
    }

    internal fun isFeatureEnabled(featureType: FeatureType): Boolean {
        features.forEach {
            if (it.feature == featureType.toString())
                return it.enabled
        }
        return false
    }
}