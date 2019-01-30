package us.frollo.frollosdk.error

import android.content.Context
import androidx.annotation.StringRes
import us.frollo.frollosdk.R

enum class NetworkErrorType(@StringRes val textResource: Int) {
    CONNECTION_FAILURE(R.string.FrolloSDK_Error_Network_ConnectionFailure),
    INVALID_SSL(R.string.FrolloSDK_Error_Network_InvalidSSL),
    UNKNOWN(R.string.FrolloSDK_Error_Network_UnknownError);

    fun toLocalizedString(context: Context?, arg1: String? = null): String? =
            context?.resources?.getString(textResource, arg1)
}