package us.frollo.frollosdk.error

import android.content.Context
import androidx.annotation.StringRes
import us.frollo.frollosdk.R

enum class LoginFormErrorType(@StringRes val textResource: Int) {
    FIELD_CHOICE_NOT_SELECTED(R.string.FrolloSDK_Error_LoginForm_FieldChoiceNotSelectedFormat),
    MISSING_REQUIRED_FIELD(R.string.FrolloSDK_Error_LoginForm_MissingRequiredFieldFormat),
    MAX_LENGTH_EXCEEDED(R.string.FrolloSDK_Error_LoginForm_MaxLengthExceededFormat),
    VALIDATION_FAILED( R.string.FrolloSDK_Error_LoginForm_ValidationFailedFormat),
    UNKNOWN(R.string.FrolloSDK_Error_LoginForm_UnknownError);

    fun toLocalizedString(context: Context?, arg1: String? = null): String? =
            context?.resources?.getString(textResource, arg1)
}