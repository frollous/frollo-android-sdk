package us.frollo.frollosdk.model.coredata.aggregation.providers

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/** Provider encryption */
data class ProviderEncryption(

        /** Encryption Type */
        @ColumnInfo(name = "type") @SerializedName("encryption_type") val encryptionType: ProviderEncryptionType,

        /** Encryption alias to be appended to login form values (optional) */
        @ColumnInfo(name = "alias") @SerializedName("alias") val alias: String?,

        /** PEM Public key to be used to encrypt login forms (optional) */
        @ColumnInfo(name = "pem") @SerializedName("pem") val pem: String?
)