package us.frollo.frollosdk.authentication

import us.frollo.frollosdk.keystore.Keystore
import us.frollo.frollosdk.model.api.user.TokenResponse
import us.frollo.frollosdk.preferences.Preferences

internal class AuthToken(private val keystore: Keystore, private val pref: Preferences) {

    companion object {
        private var accessToken: String? = null
        private var refreshToken: String? = null
        private var accessTokenExpiry: Long = -1
    }

    fun getAccessToken(): String? {
        if (accessToken == null)
            accessToken = keystore.decrypt(pref.encryptedAccessToken)
        return accessToken
    }

    fun getRefreshToken(): String? {
        if (refreshToken == null)
            refreshToken = keystore.decrypt(pref.encryptedRefreshToken)
        return refreshToken
    }

    fun getAccessTokenExpiry(): Long {
        if (accessTokenExpiry == -1L)
            accessTokenExpiry = pref.accessTokenExpiry
        return accessTokenExpiry
    }

    fun saveTokens(tokenResponse: TokenResponse) {
        accessToken = tokenResponse.accessToken
        pref.encryptedAccessToken = keystore.encrypt(accessToken)
        refreshToken = tokenResponse.refreshToken
        pref.encryptedRefreshToken = keystore.encrypt(tokenResponse.refreshToken)
        accessTokenExpiry = tokenResponse.accessTokenExp
        pref.accessTokenExpiry = tokenResponse.accessTokenExp
    }

    fun clearTokens() {
        accessToken = null
        pref.resetEncryptedAccessToken()
        refreshToken = null
        pref.resetEncryptedRefreshToken()
        accessTokenExpiry = -1
        pref.resetAccessTokenExpiry()
    }
}