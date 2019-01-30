package us.frollo.frollosdk.error

import us.frollo.frollosdk.mapping.toAPIErrorResponse
import us.frollo.frollosdk.mapping.toAPIErrorType
import us.frollo.frollosdk.model.api.shared.APIErrorCode
import us.frollo.frollosdk.model.api.shared.APIErrorResponse

class APIError(val statusCode: Int, private val error: String?) : FrolloSDKError(error) {

    // Type of API Error
    val type : APIErrorType
        get() = statusCode.toAPIErrorType(errorCode)

    // Error code returned by the API if available and recognised
    val errorCode: APIErrorCode?
        get() = errorResponse?.errorCode

    // Error message returned by the API if available
    override val message : String?
        get() = errorResponse?.errorMessage ?: error

    private var errorResponse: APIErrorResponse? = null

    init {
        errorResponse = error?.toAPIErrorResponse()
    }

    override val localizedDescription: String?
        get() = type.toLocalizedString(context)

    override val debugDescription: String?
        get() {
            var debug = "APIError: Type [${ type.name }] HTTP Status Code: $statusCode "
            errorCode?.let { debug = debug.plus("$it: ") }
            message?.let { debug = debug.plus("$it | ") }
            debug = debug.plus(localizedDescription)
            return debug
        }
}