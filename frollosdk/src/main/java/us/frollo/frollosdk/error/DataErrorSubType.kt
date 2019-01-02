package us.frollo.frollosdk.error

import android.content.Context
import androidx.annotation.StringRes
import us.frollo.frollosdk.R

enum class DataErrorSubType(val type: DataErrorType, @StringRes val textResource: Int) {
    INVALID_DATA(DataErrorType.API, R.string.Error_Data_API_InvalidData),
    PASSWORD_TOO_SHORT(DataErrorType.API, R.string.Error_Data_API_PasswordTooShort),

    MISSING_ACCESS_TOKEN(DataErrorType.AUTHENTICATION, R.string.Error_Data_Authentication_MissingAccessToken),
    MISSING_REFRESH_TOKEN(DataErrorType.AUTHENTICATION, R.string.Error_Data_Authentication_MissingRefreshToken),

    CORRUPT(DataErrorType.DATABASE, R.string.Error_Data_Database_Corrupted),
    DISK_FULL(DataErrorType.DATABASE, R.string.Error_Data_Database_DiskFullError),
    MIGRATION_FAILED(DataErrorType.DATABASE, R.string.Error_Data_Database_MigrationFailed),
    NOT_FOUND(DataErrorType.DATABASE, R.string.Error_Data_Database_NotFound),

    UNKNOWN(DataErrorType.UNKNOWN, R.string.Error_Generic_UnknownError);

    fun toLocalizedString(context: Context, arg1: String? = null): String =
            context.resources.getString(textResource, arg1)
}