package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName

data class ProviderLoginForm(
        @SerializedName("id") val formId: String?,
        @SerializedName("forgetPasswordURL") val forgetPasswordUrl: String?,
        @SerializedName("help") val help: String?,
        @SerializedName("mfaInfoTitle") val mfaInfoTitle: String?,
        @SerializedName("mfaInfoText") val mfaInfoText: String?,
        @SerializedName("mfaTimeout") val mfaTimeout: Long?,
        @SerializedName("formType") val formType: ProviderFormType,
        @SerializedName("row") val rows: List<ProviderFormRow>
) {

    fun encryptValues() {
        // TODO: to be implemented
    }

    fun validateForm() {
        // TODO: to be implemented
    }
}