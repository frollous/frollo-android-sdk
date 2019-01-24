package us.frollo.frollosdk.error

import java.io.IOException
import java.security.GeneralSecurityException
import javax.net.ssl.SSLException

class NetworkError(private val t: Throwable? = null) : FrolloSDKError(t?.message) {

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

    override val localizedDescription: String?
        get() = type.toLocalizedString(context)

    override val debugDescription: String?
        get() {
            var debug = "NetworkError: ${ type.name }: $localizedDescription"
            t?.let {  debug = debug.plus(" | ${it.message}") }
            return debug
        }
}