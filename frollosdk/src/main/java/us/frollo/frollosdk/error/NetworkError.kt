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
        get() {
            var msg = type.toLocalizedString(context)
            t?.let {  msg = msg.plus(" | ${it.message}") }
            return msg
        }

    override val debugDescription: String?
        get() = "NetworkError: ${ type.name }: $localizedDescription"
}