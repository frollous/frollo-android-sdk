/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.frollo.frollosdk.error

import android.content.Context
import androidx.annotation.StringRes
import us.frollo.frollosdk.R

/** Indicates the issue of the error */
enum class LoginFormErrorType(
    /** Localized string resource id */
    @StringRes val textResource: Int
) {

    /** A required multiple choice field has not been selected */
    FIELD_CHOICE_NOT_SELECTED(R.string.FrolloSDK_Error_LoginForm_FieldChoiceNotSelectedFormat),
    /** Maximum length of the field has been exceeded */
    MISSING_REQUIRED_FIELD(R.string.FrolloSDK_Error_LoginForm_MissingRequiredFieldFormat),
    /** A required field is missing a value */
    MAX_LENGTH_EXCEEDED(R.string.FrolloSDK_Error_LoginForm_MaxLengthExceededFormat),
    /** Regex validation has failed for a field */
    VALIDATION_FAILED(R.string.FrolloSDK_Error_LoginForm_ValidationFailedFormat),
    /** Unknown error */
    UNKNOWN(R.string.FrolloSDK_Error_LoginForm_UnknownError);

    /** Enum to localized message */
    fun toLocalizedString(context: Context?, arg1: String? = null): String? =
            context?.resources?.getString(textResource, arg1)
}