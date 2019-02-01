package us.frollo.frollosdk.error

import android.content.Context
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.R

/**
 * Conforms to localizable error and debug description
 */
open class FrolloSDKError(errorMessage: String? = null) : Error(errorMessage) {

    internal val context: Context? = FrolloSDK.app.applicationContext

    /** Localized description */
    open val localizedDescription: String? =
            errorMessage ?: context?.resources?.getString(R.string.FrolloSDK_Error_Generic_UnknownError)

    /** Debug description */
    open val debugDescription: String? =
            errorMessage ?: context?.resources?.getString(R.string.FrolloSDK_Error_Generic_UnknownError)
}