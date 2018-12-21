package us.frollo.frollosdk.error

import android.content.Context
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.R

open class FrolloSDKError(errorMessage: String? = null) {

    protected val context: Context = FrolloSDK.app.applicationContext

    open val localizedDescription: String =
            errorMessage ?: context.resources.getString(R.string.Error_Generic_UnknownError)
    open val debugDescription: String =
            errorMessage ?: context.resources.getString(R.string.Error_Generic_UnknownError)
}