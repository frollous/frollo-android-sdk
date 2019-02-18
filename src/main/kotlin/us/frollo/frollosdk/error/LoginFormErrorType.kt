package us.frollo.frollosdk.error

import android.content.Context
import androidx.annotation.StringRes
import us.frollo.frollosdk.R

/** Indicates the issue of the error */
enum class LoginFormErrorType(
        /** Localized string resource id */
        @StringRes val textResource: Int) {

    /** A required multiple choice field has not been selected */
    FIELD_CHOICE_NOT_SELECTED(R.string.FrolloSDK_Error_LoginForm_FieldChoiceNotSelectedFormat),
    /** Maximum length of the field has been exceeded */
    MISSING_REQUIRED_FIELD(R.string.FrolloSDK_Error_LoginForm_MissingRequiredFieldFormat),
    /** A required field is missing a value */
    MAX_LENGTH_EXCEEDED(R.string.FrolloSDK_Error_LoginForm_MaxLengthExceededFormat),
    /** Regex validation has failed for a field */
    VALIDATION_FAILED( R.string.FrolloSDK_Error_LoginForm_ValidationFailedFormat),
    /** Unknown error */
    UNKNOWN(R.string.FrolloSDK_Error_LoginForm_UnknownError);

    /** Enum to localized message */
    fun toLocalizedString(context: Context?, arg1: String? = null): String? =
            context?.resources?.getString(textResource, arg1)
}