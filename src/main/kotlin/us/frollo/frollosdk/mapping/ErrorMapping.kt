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

package us.frollo.frollosdk.mapping

import com.google.gson.Gson
import net.openid.appauth.AuthorizationException
import us.frollo.frollosdk.error.APIErrorType
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.OAuth2ErrorType
import us.frollo.frollosdk.extensions.fromJson
import us.frollo.frollosdk.model.api.shared.APIErrorCode
import us.frollo.frollosdk.model.api.shared.APIErrorResponseWrapper
import us.frollo.frollosdk.model.oauth.OAuth2ErrorResponse

internal fun String.toAPIErrorResponse() =
        Gson().fromJson<APIErrorResponseWrapper>(this)?.apiErrorResponse

internal fun Int.toAPIErrorType(errorCode: APIErrorCode?): APIErrorType {
    return when (this) {
        400 -> {
            when (errorCode) {
                APIErrorCode.INVALID_MUST_BE_DIFFERENT -> APIErrorType.PASSWORD_MUST_BE_DIFFERENT
                APIErrorCode.MIGRATION_FAILED -> APIErrorType.MIGRATION_FAILED
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
        if (error?.type != null) error else null
    } catch (e: Exception) {
        null
    }
}

internal fun AuthorizationException.toOAuth2ErrorType(): OAuth2ErrorType {
    return when (this) {
        AuthorizationException.AuthorizationRequestErrors.ACCESS_DENIED -> OAuth2ErrorType.ACCESS_DENIED
        AuthorizationException.AuthorizationRequestErrors.CLIENT_ERROR -> OAuth2ErrorType.CLIENT_ERROR
        AuthorizationException.AuthorizationRequestErrors.INVALID_REQUEST -> OAuth2ErrorType.INVALID_REQUEST
        AuthorizationException.AuthorizationRequestErrors.INVALID_SCOPE -> OAuth2ErrorType.INVALID_SCOPE
        AuthorizationException.AuthorizationRequestErrors.OTHER -> OAuth2ErrorType.OTHER_AUTHORIZATION
        AuthorizationException.AuthorizationRequestErrors.SERVER_ERROR -> OAuth2ErrorType.SERVER_ERROR
        AuthorizationException.AuthorizationRequestErrors.TEMPORARILY_UNAVAILABLE -> OAuth2ErrorType.SERVER_ERROR
        AuthorizationException.AuthorizationRequestErrors.UNAUTHORIZED_CLIENT -> OAuth2ErrorType.UNAUTHORIZED_CLIENT
        AuthorizationException.AuthorizationRequestErrors.UNSUPPORTED_RESPONSE_TYPE -> OAuth2ErrorType.UNSUPPORTED_RESPONSE_TYPE

        AuthorizationException.TokenRequestErrors.INVALID_REQUEST -> OAuth2ErrorType.INVALID_REQUEST
        AuthorizationException.TokenRequestErrors.CLIENT_ERROR -> OAuth2ErrorType.CLIENT_ERROR
        AuthorizationException.TokenRequestErrors.INVALID_CLIENT -> OAuth2ErrorType.INVALID_CLIENT
        AuthorizationException.TokenRequestErrors.INVALID_GRANT -> OAuth2ErrorType.INVALID_GRANT
        AuthorizationException.TokenRequestErrors.OTHER -> OAuth2ErrorType.OTHER_AUTHORIZATION
        AuthorizationException.TokenRequestErrors.UNAUTHORIZED_CLIENT -> OAuth2ErrorType.UNAUTHORIZED_CLIENT
        AuthorizationException.TokenRequestErrors.UNSUPPORTED_GRANT_TYPE -> OAuth2ErrorType.UNSUPPORTED_GRANT_TYPE
        AuthorizationException.TokenRequestErrors.INVALID_SCOPE -> OAuth2ErrorType.INVALID_SCOPE

        AuthorizationException.RegistrationRequestErrors.INVALID_REDIRECT_URI -> OAuth2ErrorType.INVALID_REDIRECT_URI
        AuthorizationException.RegistrationRequestErrors.INVALID_CLIENT_METADATA -> OAuth2ErrorType.INVALID_CLIENT_METADATA
        AuthorizationException.RegistrationRequestErrors.CLIENT_ERROR -> OAuth2ErrorType.CLIENT_ERROR
        AuthorizationException.RegistrationRequestErrors.INVALID_REQUEST -> OAuth2ErrorType.INVALID_REQUEST
        AuthorizationException.RegistrationRequestErrors.OTHER -> OAuth2ErrorType.OTHER_AUTHORIZATION

        AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW -> OAuth2ErrorType.USER_CANCELLED
        AuthorizationException.GeneralErrors.SERVER_ERROR -> OAuth2ErrorType.SERVER_ERROR
        AuthorizationException.GeneralErrors.NETWORK_ERROR -> OAuth2ErrorType.NETWORK_ERROR
        else -> OAuth2ErrorType.OTHER_AUTHORIZATION
    }
}

internal fun String.toOAuth2ErrorResponse(): OAuth2ErrorResponse? {
    return try {
        Gson().fromJson<OAuth2ErrorResponse>(this)
    } catch (e: Exception) {
        null
    }
}