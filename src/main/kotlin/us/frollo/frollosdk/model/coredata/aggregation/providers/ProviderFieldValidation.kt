package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName

data class ProviderFieldValidation(
        @SerializedName("regExp") val regExp: String,
        @SerializedName("errorMsg") val errorMsg: String
)