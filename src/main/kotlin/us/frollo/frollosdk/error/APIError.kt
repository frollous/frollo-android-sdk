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

import us.frollo.frollosdk.mapping.toAPIErrorResponse
import us.frollo.frollosdk.mapping.toAPIErrorType
import us.frollo.frollosdk.model.api.shared.APIErrorCode
import us.frollo.frollosdk.model.api.shared.APIErrorResponse

/**
 * Represents errors that can be returned from the API
 */
class APIError(
    /** Status code received from the API */
    val statusCode: Int,
    private val error: String?
) : FrolloSDKError(error) {

    /** Type of API Error */
    val type: APIErrorType
        get() = statusCode.toAPIErrorType(errorCode)

    /** Error code returned by the API if available and recognised */
    val errorCode: APIErrorCode?
        get() = errorResponse?.errorCode?.let { APIErrorCode.fromRawValue(it) }

    /** Error message returned by the API if available */
    override val message: String?
        get() = errorResponse?.errorMessage ?: error

    private var errorResponse: APIErrorResponse? = null

    init {
        errorResponse = error?.toAPIErrorResponse()
    }

    /** Localized description */
    override val localizedDescription: String?
        get() = type.toLocalizedString(context)

    /** Debug description */
    override val debugDescription: String?
        get() {
            var debug = "APIError: Type [${ type.name }] HTTP Status Code: $statusCode "
            errorCode?.let { debug = debug.plus("$it: ") }
            message?.let { debug = debug.plus("$it | ") }
            debug = debug.plus(localizedDescription)
            return debug
        }
}