package us.frollo.frollosdk.error

import net.openid.appauth.AuthorizationException
import us.frollo.frollosdk.mapping.toOAuthErrorType

/**
 * Represents errors that can be returned from the authorization flow
 */
class OAuthError(private val exception: AuthorizationException?) : FrolloSDKError(exception?.message) {

    /** Type of OAuth Error */
    val type : OAuthErrorType
        get() = exception?.toOAuthErrorType() ?: OAuthErrorType.UNKNOWN

    /** Localized description */
    override val localizedDescription: String?
        get() = type.toLocalizedString(context)

    /** Debug description */
    override val debugDescription: String?
        get() {
            var debug = "OAuthError: Type [${ type.name }] "
            debug = debug.plus(localizedDescription)
            return debug
        }
}