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

package us.frollo.frollosdk.extensions

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.network.ApiResponse
import us.frollo.frollosdk.error.*
import us.frollo.frollosdk.mapping.toDataError
import us.frollo.frollosdk.mapping.toOAuth2ErrorResponse
import us.frollo.frollosdk.network.ErrorResponseType

internal fun <T> Call<T>.enqueue(errorResponseType: ErrorResponseType = ErrorResponseType.NORMAL, completion: (Resource<T>) -> Unit) {
    this.enqueue(object: Callback<T> {
        override fun onResponse(call: Call<T>?, response: Response<T>?) {
            val apiResponse = ApiResponse(response)
            if (apiResponse.isSuccessful) {
                completion.invoke(Resource.success(data = apiResponse.body))
            } else {
                handleFailure(errorResponseType, apiResponse, null, completion)
            }
        }

        override fun onFailure(call: Call<T>?, t: Throwable?) {
            val errorResponse = ApiResponse<T>(t)
            handleFailure(errorResponseType, errorResponse, t, completion)
        }
    })
}

internal fun <T> handleFailure(errorResponseType: ErrorResponseType, errorResponse: ApiResponse<T>,  t: Throwable? = null, completion: (Resource<T>) -> Unit) {
    val code = errorResponse.code
    val errorMsg = errorResponse.errorMessage

    val oAuth2ErrorResponse = errorMsg?.toOAuth2ErrorResponse()
    val dataError = errorMsg?.toDataError()

    if (dataError != null) {
        completion.invoke(Resource.error(error = DataError(dataError.type, dataError.subType))) // Re-create new DataError as the json converter does not has the context object
    } else if (errorResponseType == ErrorResponseType.OAUTH2 && oAuth2ErrorResponse != null) {
        val oAuth2Error = OAuthError(response = errorMsg)
        handleOAuth2Failure(oAuth2Error)
        completion.invoke(Resource.error(error = oAuth2Error))
    } else if (errorResponseType == ErrorResponseType.NORMAL && code != null) {
        completion.invoke(Resource.error(error = APIError(code, errorMsg)))
    } else if (t != null) {
        completion.invoke(Resource.error(error = NetworkError(t)))
    } else {
        completion.invoke(Resource.error(error = FrolloSDKError(errorMsg)))
    }
}

internal fun handleOAuth2Failure(error: OAuthError) {
    when (error.type) {
        OAuthErrorType.INVALID_REQUEST,
        OAuthErrorType.INVALID_CLIENT,
        OAuthErrorType.INVALID_GRANT,
        OAuthErrorType.INVALID_SCOPE,
        OAuthErrorType.UNAUTHORIZED_CLIENT,
        OAuthErrorType.UNSUPPORTED_GRANT_TYPE,
        OAuthErrorType.SERVER_ERROR,
        OAuthErrorType.UNKNOWN -> {
            FrolloSDK.forcedLogout()
        }

        else -> {
            // Do nothing
        }
    }
}