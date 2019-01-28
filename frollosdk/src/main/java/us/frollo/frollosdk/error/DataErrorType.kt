package us.frollo.frollosdk.error

import android.content.Context
import androidx.annotation.StringRes
import us.frollo.frollosdk.R

enum class DataErrorType(@StringRes val textResource: Int) {
    API(R.string.FrolloSDK_Error_Data_API_Unknown),
    AUTHENTICATION(R.string.FrolloSDK_Error_Data_Authentication_Unknown),
    DATABASE(R.string.FrolloSDK_Error_Data_Database_UnknownError),
    UNKNOWN(R.string.FrolloSDK_Error_Generic_UnknownError);

    fun toLocalizedString(context: Context?, arg1: String? = null): String? =
            context?.resources?.getString(textResource, arg1)
}