package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName

data class ProviderFormRow(
        @SerializedName("id") val rowId: String,
        @SerializedName("label") val label: String,
        @SerializedName("form") val form: String,
        @SerializedName("fieldRowChoice") val fieldRowChoice: String,
        @SerializedName("hint") val hint: String?,
        @SerializedName("field") val fields: List<ProviderFormField>
)