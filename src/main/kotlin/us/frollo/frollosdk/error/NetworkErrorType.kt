package us.frollo.frollosdk.error

import android.content.Context
import androidx.annotation.StringRes
import us.frollo.frollosdk.R

/**
 * Indicates the type of error
 */
enum class NetworkErrorType(
        /** Localized string resource id */
        @StringRes val textResource: Int) {

    /** Connection failure - usually poor connectivity */
    CONNECTION_FAILURE(R.string.FrolloSDK_Error_Network_ConnectionFailure),
    /** Invalid SSL - TLS public key pinning has failed or the certificate provided is invalid. Usually indicates a MITM attack */
    INVALID_SSL(R.string.FrolloSDK_Error_Network_InvalidSSL),
    /** Unknown error */
    UNKNOWN(R.string.FrolloSDK_Error_Network_UnknownError);

    /** Enum to localized message */
    fun toLocalizedString(context: Context?, arg1: String? = null): String? =
            context?.resources?.getString(textResource, arg1)
}