package us.frollo.frollosdk.error

import android.content.Context
import androidx.annotation.StringRes
import us.frollo.frollosdk.R

/**
 * Detailed type of the error
 */
enum class DataErrorSubType(
        /** [DataErrorType] for this subtype */
        val type: DataErrorType,
        /** Localized string resource id */
        @StringRes val textResource: Int) {

    /** API - Invalid Data */
    INVALID_DATA(DataErrorType.API, R.string.FrolloSDK_Error_Data_API_InvalidData),
    /** API - Password too short */
    PASSWORD_TOO_SHORT(DataErrorType.API, R.string.FrolloSDK_Error_Data_API_PasswordTooShort),

    /** Authentication - Already logged in */
    ALREADY_LOGGED_IN(DataErrorType.AUTHENTICATION, R.string.FrolloSDK_Error_Data_Authentication_AlreadyLoggedIn),
    /** Authentication - User not logged in */
    LOGGED_OUT(DataErrorType.AUTHENTICATION, R.string.FrolloSDK_Error_Data_Authentication_LoggedOut),
    /** Authentication - Missing access token from keychain */
    MISSING_ACCESS_TOKEN(DataErrorType.AUTHENTICATION, R.string.FrolloSDK_Error_Data_Authentication_MissingAccessToken),
    /** Authentication - Missing refresh token from keychain */
    MISSING_REFRESH_TOKEN(DataErrorType.AUTHENTICATION, R.string.FrolloSDK_Error_Data_Authentication_MissingRefreshToken),

    /** Database - Corrupted */
    CORRUPT(DataErrorType.DATABASE, R.string.FrolloSDK_Error_Data_Database_Corrupted),
    /** Database - Disk full, no free space to continue operating */
    DISK_FULL(DataErrorType.DATABASE, R.string.FrolloSDK_Error_Data_Database_DiskFullError),
    /** Database - Migration upgrade failed */
    MIGRATION_FAILED(DataErrorType.DATABASE, R.string.FrolloSDK_Error_Data_Database_MigrationFailed),
    /** Database - Store not found */
    NOT_FOUND(DataErrorType.DATABASE, R.string.FrolloSDK_Error_Data_Database_NotFound),

    /** Unknown error */
    UNKNOWN(DataErrorType.UNKNOWN, R.string.FrolloSDK_Error_Generic_UnknownError);

    /** Enum to localized message */
    fun toLocalizedString(context: Context?, arg1: String? = null): String? =
            context?.resources?.getString(textResource, arg1)
}