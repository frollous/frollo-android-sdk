package us.frollo.frollosdk.model.api.shared

import com.google.gson.annotations.SerializedName

internal data class APIErrorResponse(
        @SerializedName("error_code") val errorCode: APIErrorCode,
        @SerializedName("error_message") val errorMessage: String?
)