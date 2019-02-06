package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName

data class ProviderFieldOption(
        @SerializedName("displayText") val text: String,
        @SerializedName("optionValue") val value: String,
        @SerializedName("isSelected") val isSelected: Boolean?
)