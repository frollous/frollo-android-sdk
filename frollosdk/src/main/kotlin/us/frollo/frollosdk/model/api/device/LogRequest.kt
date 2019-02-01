package us.frollo.frollosdk.model.api.device

import com.google.gson.annotations.SerializedName

internal data class LogRequest(
        @SerializedName("details") var details: String? = null,
        @SerializedName("message") var message: String,
        @SerializedName("score") var score: Int
)