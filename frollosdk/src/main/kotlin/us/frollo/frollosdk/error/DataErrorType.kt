package us.frollo.frollosdk.error

import android.content.Context
import androidx.annotation.StringRes
import us.frollo.frollosdk.R

/**
 * High level type of the error
 */
enum class DataErrorType(
        /** Localized string resource id */
        @StringRes val textResource: Int) {

    /** API data error - intercepts potential issues before being sent to API */
    API(R.string.FrolloSDK_Error_Data_API_Unknown),
    /** Authentication error */
    AUTHENTICATION(R.string.FrolloSDK_Error_Data_Authentication_Unknown),
    /** Database error - issues with the Core Data */
    DATABASE(R.string.FrolloSDK_Error_Data_Database_UnknownError),
    /** Unknown error */
    UNKNOWN(R.string.FrolloSDK_Error_Generic_UnknownError);

    /** Enum to localized message */
    fun toLocalizedString(context: Context?, arg1: String? = null): String? =
            context?.resources?.getString(textResource, arg1)
}