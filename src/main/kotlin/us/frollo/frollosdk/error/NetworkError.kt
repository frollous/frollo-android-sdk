package us.frollo.frollosdk.error

import java.io.IOException
import java.security.GeneralSecurityException
import javax.net.ssl.SSLException

/**
 * Error occuring at the network layer
 */
class NetworkError(private val t: Throwable? = null) : FrolloSDKError(t?.message) {

    /** Type of error for common scenarios */
    val type: NetworkErrorType
        get() {
            return if (t is SSLException || t is GeneralSecurityException) {
                NetworkErrorType.INVALID_SSL
            } else if (t is IOException) {
                NetworkErrorType.CONNECTION_FAILURE
            } else {
                NetworkErrorType.UNKNOWN
            }
        }

    /** Localized description */
    override val localizedDescription: String?
        get() {
            var msg = type.toLocalizedString(context)
            t?.let {  msg = msg.plus(" | ${it.message}") }
            return msg
        }

    /** Debug description */
    override val debugDescription: String?
        get() = "NetworkError: ${ type.name }: $localizedDescription"
}