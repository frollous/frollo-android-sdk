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

/**
 * Type of error that has occurred on the API
 */
enum class APIErrorType(
    /** Localized string resource id */
    @StringRes val textResource: Int
) {

    /** Deprecated API */
    DEPRECATED(R.string.FrolloSDK_Error_API_DeprecatedError),

    /** Server is under maintenance */
    MAINTENANCE(R.string.FrolloSDK_Error_API_Maintenance),
    /** API is not implemented */
    NOT_IMPLEMENTED(R.string.FrolloSDK_Error_API_NotImplemented),
    /** Rate limit for the API has been exceeded. Back off and try again */
    RATE_LIMIT(R.string.FrolloSDK_Error_API_RateLimit),
    /** Server has encountered a critical error */
    SERVER_ERROR(R.string.FrolloSDK_Error_API_ServerError),

    /** Bad request */
    BAD_REQUEST(R.string.FrolloSDK_Error_API_BadRequest),
    /** Unauthorised */
    UNAUTHORISED(R.string.FrolloSDK_Error_API_Unauthorised),
    /** Object not found */
    NOT_FOUND(R.string.FrolloSDK_Error_API_NotFound),
    /** Object already exists */
    ALREADY_EXISTS(R.string.FrolloSDK_Error_API_UserAlreadyExists),
    /** New password must be different from old password */
    PASSWORD_MUST_BE_DIFFERENT(R.string.FrolloSDK_Error_API_PasswordMustBeDifferent),
    /** An issue occurred while trying to migrate the user to a new identity provider */
    MIGRATION_FAILED(R.string.FrolloSDK_Error_API_MigrationFailed),

    /** Invalid access token */
    INVALID_ACCESS_TOKEN(R.string.FrolloSDK_Error_API_InvalidAccessToken),
    /** Invalid refresh token */
    INVALID_REFRESH_TOKEN(R.string.FrolloSDK_Error_API_InvalidRefreshToken),
    /** Username and/or password is wrong */
    INVALID_USERNAME_PASSWORD(R.string.FrolloSDK_Error_API_InvalidUsernamePassword),
    /** Device has been suspended */
    SUSPENDED_DEVICE(R.string.FrolloSDK_Error_API_SuspendedDevice),
    /** User has been suspended */
    SUSPENDED_USER(R.string.FrolloSDK_Error_API_SuspendedUser),
    /** User account locked */
    ACCOUNT_LOCKED(R.string.FrolloSDK_Error_API_AccountLocked),
    /** An unknown issue with authorisation has occurred */
    OTHER_AUTHORISATION(R.string.FrolloSDK_Error_API_UnknownAuthorisation),

    /** Unknown error */
    UNKNOWN(R.string.FrolloSDK_Error_API_UnknownError);

    /** Enum to localized message */
    fun toLocalizedString(context: Context?, arg1: String? = null): String? =
            context?.resources?.getString(textResource, arg1)
}