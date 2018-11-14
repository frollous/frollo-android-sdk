package us.frollo.frollosdk.model.api.shared

import com.google.gson.annotations.SerializedName

data class APIErrorResponseWrapper(@SerializedName("error") val apiErrorResponse: APIErrorResponse?)