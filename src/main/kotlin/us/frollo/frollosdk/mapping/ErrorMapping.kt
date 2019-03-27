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
import net.openid.appauth.AuthorizationException.*
import us.frollo.frollosdk.error.APIErrorType
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.OAuth2ErrorType
import us.frollo.frollosdk.extensions.fromJson
import us.frollo.frollosdk.model.api.shared.APIErrorCode
import us.frollo.frollosdk.model.api.shared.APIErrorResponse
import us.frollo.frollosdk.model.api.shared.APIErrorResponseWrapper
import us.frollo.frollosdk.model.oauth.OAuth2ErrorResponse

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

internal fun AuthorizationException.toOAuth2ErrorType(): OAuth2ErrorType {
    val exception = this
    return when (exception) {
        AuthorizationRequestErrors.ACCESS_DENIED -> OAuth2ErrorType.ACCESS_DENIED
        AuthorizationRequestErrors.CLIENT_ERROR -> OAuth2ErrorType.CLIENT_ERROR
        AuthorizationRequestErrors.INVALID_REQUEST -> OAuth2ErrorType.INVALID_REQUEST
        AuthorizationRequestErrors.INVALID_SCOPE -> OAuth2ErrorType.INVALID_SCOPE
        AuthorizationRequestErrors.OTHER -> OAuth2ErrorType.OTHER_AUTHORISATION
        AuthorizationRequestErrors.SERVER_ERROR -> OAuth2ErrorType.SERVER_ERROR
        AuthorizationRequestErrors.TEMPORARILY_UNAVAILABLE -> OAuth2ErrorType.SERVER_ERROR
        AuthorizationRequestErrors.UNAUTHORIZED_CLIENT -> OAuth2ErrorType.UNAUTHORIZED_CLIENT
        AuthorizationRequestErrors.UNSUPPORTED_RESPONSE_TYPE -> OAuth2ErrorType.UNSUPPORTED_RESPONSE_TYPE

        TokenRequestErrors.INVALID_REQUEST -> OAuth2ErrorType.INVALID_REQUEST
        TokenRequestErrors.CLIENT_ERROR -> OAuth2ErrorType.CLIENT_ERROR
        TokenRequestErrors.INVALID_CLIENT -> OAuth2ErrorType.INVALID_CLIENT
        TokenRequestErrors.INVALID_GRANT -> OAuth2ErrorType.INVALID_GRANT
        TokenRequestErrors.OTHER -> OAuth2ErrorType.OTHER_AUTHORISATION
        TokenRequestErrors.UNAUTHORIZED_CLIENT -> OAuth2ErrorType.UNAUTHORIZED_CLIENT
        TokenRequestErrors.UNSUPPORTED_GRANT_TYPE -> OAuth2ErrorType.UNSUPPORTED_GRANT_TYPE
        TokenRequestErrors.INVALID_SCOPE -> OAuth2ErrorType.INVALID_SCOPE

        RegistrationRequestErrors.INVALID_REDIRECT_URI -> OAuth2ErrorType.INVALID_REDIRECT_URI
        RegistrationRequestErrors.INVALID_CLIENT_METADATA -> OAuth2ErrorType.INVALID_CLIENT_METADATA
        RegistrationRequestErrors.CLIENT_ERROR -> OAuth2ErrorType.CLIENT_ERROR
        RegistrationRequestErrors.INVALID_REQUEST -> OAuth2ErrorType.INVALID_REQUEST
        RegistrationRequestErrors.OTHER -> OAuth2ErrorType.OTHER_AUTHORISATION

        GeneralErrors.USER_CANCELED_AUTH_FLOW -> OAuth2ErrorType.USER_CANCELLED
        GeneralErrors.SERVER_ERROR -> OAuth2ErrorType.SERVER_ERROR
        GeneralErrors.NETWORK_ERROR -> OAuth2ErrorType.NETWORK_ERROR
        else -> OAuth2ErrorType.OTHER_AUTHORISATION
    }
}

internal fun String.toOAuth2ErrorResponse(): OAuth2ErrorResponse? {
    return try {
        Gson().fromJson<OAuth2ErrorResponse>(this)
    } catch (e: Exception) {
        null
    }
}