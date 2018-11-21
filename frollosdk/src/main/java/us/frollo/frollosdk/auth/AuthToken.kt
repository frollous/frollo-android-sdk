package us.frollo.frollosdk.auth

import timber.log.Timber
import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.model.api.user.TokenResponse
import us.frollo.frollosdk.preferences.Preferences

internal class AuthToken(private val keystore: Keystore, private val pref: Preferences) {

    companion object {
        private var accessToken: String? = null
        private var refreshToken: String? = null
    }

    fun getAccessToken(): String? {
        if (accessToken == null)
            accessToken = keystore.decrypt(pref.encryptedAccessToken)
        Timber.d("SERVICE TOKEN USED WAS: $accessToken")
        return accessToken
    }

    fun getRefreshToken(): String? {
        if (refreshToken == null)
            refreshToken = keystore.decrypt(pref.encryptedRefreshToken)
        return refreshToken
    }

    fun saveTokens(tokenResponse: TokenResponse) {
        accessToken = tokenResponse.accessToken
        pref.encryptedAccessToken = keystore.encrypt(accessToken)
        refreshToken = tokenResponse.refreshToken
        pref.encryptedRefreshToken = keystore.encrypt(tokenResponse.refreshToken)
    }

    fun clearTokens() {
        accessToken = null
        pref.resetEncryptedAccessToken()
        refreshToken = null
        pref.resetEncryptedRefreshToken()
    }
}