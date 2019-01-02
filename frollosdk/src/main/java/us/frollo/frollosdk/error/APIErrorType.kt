package us.frollo.frollosdk.error

import android.content.Context
import androidx.annotation.StringRes
import us.frollo.frollosdk.R

enum class APIErrorType(@StringRes val textResource: Int) {
    DEPRECATED(R.string.Error_API_DeprecatedError),

    MAINTENANCE(R.string.Error_API_Maintenance),
    NOT_IMPLEMENTED(R.string.Error_API_NotImplemented),
    RATE_LIMIT(R.string.Error_API_RateLimit),
    SERVER_ERROR(R.string.Error_API_ServerError),

    BAD_REQUEST(R.string.Error_API_BadRequest),
    UNAUTHORISED(R.string.Error_API_Unauthorised),
    NOT_FOUND(R.string.Error_API_NotFound),
    ALREADY_EXISTS(R.string.Error_API_UserAlreadyExists),
    PASSWORD_MUST_BE_DIFFERENT(R.string.Error_API_PasswordMustBeDifferent),

    INVALID_ACCESS_TOKEN(R.string.Error_API_InvalidAccessToken),
    INVALID_REFRESH_TOKEN(R.string.Error_API_InvalidRefreshToken),
    INVALID_USERNAME_PASSWORD(R.string.Error_API_InvalidUsernamePassword),
    SUSPENDED_DEVICE(R.string.Error_API_SuspendedDevice),
    SUSPENDED_USER(R.string.Error_API_SuspendedUser),
    OTHER_AUTHORISATION(R.string.Error_API_UnknownAuthorisation),

    UNKNOWN(R.string.Error_API_UnknownError);

    fun toLocalizedString(context: Context, arg1: String? = null): String =
            context.resources.getString(textResource, arg1)
}