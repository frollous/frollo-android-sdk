package us.frollo.frollosdk.model.coredata.aggregation.providers

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

data class ProviderEncryption(
        @ColumnInfo(name = "type") @SerializedName("encryption_type") val encryptionType: ProviderEncryptionType,
        @ColumnInfo(name = "alias") @SerializedName("alias") val alias: String?,
        @ColumnInfo(name = "pem") @SerializedName("pem") val pem: String?
)