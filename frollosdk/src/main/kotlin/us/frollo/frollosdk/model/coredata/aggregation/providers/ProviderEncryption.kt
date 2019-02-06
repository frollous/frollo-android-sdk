package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName

data class ProviderEncryption(
        @SerializedName("encryption_type") val encryptionType: ProviderEncryptionType,
        @SerializedName("alias") val alias: String?,
        @SerializedName("pem") val pem: String?
)