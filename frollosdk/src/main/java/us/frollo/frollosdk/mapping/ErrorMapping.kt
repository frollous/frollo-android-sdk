package us.frollo.frollosdk.mapping

import com.google.gson.Gson
import us.frollo.frollosdk.error.APIErrorType
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

fun Int.toAPIErrorType(errorCode: APIErrorCode?): APIErrorType {
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