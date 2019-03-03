package us.frollo.frollosdk.mapping

import com.google.gson.Gson
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationException.*
import us.frollo.frollosdk.error.APIErrorType
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.OAuthErrorType
import us.frollo.frollosdk.extensions.fromJson
import us.frollo.frollosdk.model.api.shared.APIErrorCode
import us.frollo.frollosdk.model.api.shared.APIErrorResponse
import us.frollo.frollosdk.model.api.shared.APIErrorResponseWrapper

internal fun String.toAPIErrorResponse(): APIErrorResponse? {
    return try {
        Gson().fromJson<APIErrorResponseWrapper>(this).apiErrorResponse
    } catch (e: Exception) {
        null
    }
}

internal fun Int.toAPIErrorType(errorCode: APIErrorCode?): APIErrorType {
    val statusCode = this
    return when (statusCode) {
        400 -> {
            when (errorCode) {
                APIErrorCode.INVALID_MUST_BE_DIFFERENT -> APIErrorType.PASSWORD_MUST_BE_DIFFERENT
                else -> APIErrorType.BAD_REQUEST
            }
        }
        401 -> {
            when (errorCode) {
                APIErrorCode.INVALID_ACCESS_TOKEN -> APIErrorType.INVALID_ACCESS_TOKEN
                APIErrorCode.INVALID_REFRESH_TOKEN -> APIErrorType.INVALID_REFRESH_TOKEN
                APIErrorCode.INVALID_USERNAME_PASSWORD -> APIErrorType.INVALID_USERNAME_PASSWORD
                APIErrorCode.SUSPENDED_DEVICE -> APIErrorType.SUSPENDED_DEVICE
                APIErrorCode.SUSPENDED_USER -> APIErrorType.SUSPENDED_USER
                APIErrorCode.ACCOUNT_LOCKED -> APIErrorType.ACCOUNT_LOCKED
                else -> APIErrorType.OTHER_AUTHORISATION
            }
        }
        403 -> APIErrorType.UNAUTHORISED
        404 -> APIErrorType.NOT_FOUND
        409 -> APIErrorType.ALREADY_EXISTS
        410 -> APIErrorType.DEPRECATED
        429 -> APIErrorType.RATE_LIMIT
        500, 503, 504 -> APIErrorType.SERVER_ERROR
        501 -> APIErrorType.NOT_IMPLEMENTED
        502 -> APIErrorType.MAINTENANCE
        else -> APIErrorType.UNKNOWN
    }
}

internal fun String.toDataError(): DataError? {
    return try {
        val error = Gson().fromJson<DataError>(this)
        // I know it says "is always true", BUT, this "!= null" check is NEEDED
        // because Gson().fromJson() can return object with null values for its members
        if (error.type != null) error else null
    } catch (e: Exception) {
        null
    }
}

internal fun AuthorizationException.toOAuthErrorType(): OAuthErrorType {
    val exception = this
    return when (exception) {
        AuthorizationRequestErrors.ACCESS_DENIED -> OAuthErrorType.ACCESS_DENIED
        AuthorizationRequestErrors.CLIENT_ERROR -> OAuthErrorType.CLIENT_ERROR
        AuthorizationRequestErrors.INVALID_REQUEST -> OAuthErrorType.INVALID_REQUEST
        AuthorizationRequestErrors.INVALID_SCOPE -> OAuthErrorType.INVALID_SCOPE
        AuthorizationRequestErrors.OTHER -> OAuthErrorType.OTHER_AUTHORISATION
        AuthorizationRequestErrors.SERVER_ERROR -> OAuthErrorType.SERVER_ERROR
        AuthorizationRequestErrors.TEMPORARILY_UNAVAILABLE -> OAuthErrorType.SERVER_ERROR
        AuthorizationRequestErrors.UNAUTHORIZED_CLIENT -> OAuthErrorType.UNAUTHORIZED_CLIENT
        AuthorizationRequestErrors.UNSUPPORTED_RESPONSE_TYPE -> OAuthErrorType.UNSUPPORTED_RESPONSE_TYPE

        TokenRequestErrors.INVALID_REQUEST -> OAuthErrorType.INVALID_REQUEST
        TokenRequestErrors.CLIENT_ERROR -> OAuthErrorType.CLIENT_ERROR
        TokenRequestErrors.INVALID_CLIENT -> OAuthErrorType.INVALID_CLIENT
        TokenRequestErrors.INVALID_GRANT -> OAuthErrorType.INVALID_GRANT
        TokenRequestErrors.OTHER -> OAuthErrorType.OTHER_AUTHORISATION
        TokenRequestErrors.UNAUTHORIZED_CLIENT -> OAuthErrorType.UNAUTHORIZED_CLIENT
        TokenRequestErrors.UNSUPPORTED_GRANT_TYPE -> OAuthErrorType.UNSUPPORTED_GRANT_TYPE
        TokenRequestErrors.INVALID_SCOPE -> OAuthErrorType.INVALID_SCOPE

        RegistrationRequestErrors.INVALID_REDIRECT_URI -> OAuthErrorType.INVALID_REDIRECT_URI
        RegistrationRequestErrors.INVALID_CLIENT_METADATA -> OAuthErrorType.INVALID_CLIENT_METADATA
        RegistrationRequestErrors.CLIENT_ERROR -> OAuthErrorType.CLIENT_ERROR
        RegistrationRequestErrors.INVALID_REQUEST -> OAuthErrorType.INVALID_REQUEST
        RegistrationRequestErrors.OTHER -> OAuthErrorType.OTHER_AUTHORISATION

        GeneralErrors.USER_CANCELED_AUTH_FLOW -> OAuthErrorType.USER_CANCELLED
        GeneralErrors.SERVER_ERROR -> OAuthErrorType.SERVER_ERROR
        GeneralErrors.NETWORK_ERROR -> OAuthErrorType.NETWORK_ERROR
        else -> OAuthErrorType.OTHER_AUTHORISATION
    }
}