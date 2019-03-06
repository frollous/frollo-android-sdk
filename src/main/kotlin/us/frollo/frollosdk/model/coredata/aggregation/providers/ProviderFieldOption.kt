package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName

data class ProviderFieldOption(
        @SerializedName("displayText") val displayText: String,
        @SerializedName("optionValue") val optionValue: String,
        @SerializedName("isSelected") var isSelected: Boolean?
) {
    override fun toString(): String = displayText
}