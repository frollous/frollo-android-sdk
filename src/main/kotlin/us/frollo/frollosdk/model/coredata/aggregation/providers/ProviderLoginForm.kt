package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo
import org.spongycastle.openssl.PEMParser
import org.spongycastle.openssl.jcajce.JcaPEMKeyConverter
import org.spongycastle.util.encoders.Hex
import us.frollo.frollosdk.core.FormValidationCompletionListener
import us.frollo.frollosdk.error.LoginFormError
import us.frollo.frollosdk.error.LoginFormErrorType
import us.frollo.frollosdk.extensions.regexValidate
import us.frollo.frollosdk.logging.Log
import java.io.StringReader
import java.nio.charset.Charset
import javax.crypto.Cipher

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

    fun shouldEncrypt(encryptionType: ProviderEncryptionType): Boolean {
        return if (encryptionType == ProviderEncryptionType.ENCRYPT_VALUES) {
            when (formType) {
                ProviderFormType.LOGIN -> true
                ProviderFormType.QUESTION_AND_ANSWER -> true
                ProviderFormType.TOKEN -> false
                ProviderFormType.IMAGE -> false
            }
        } else {
            false
        }
    }

    fun encryptValues(encryptionKey: String, encryptionAlias: String) {
        rows.forEach { row ->
            row.fields.forEach { field ->
                val originalValue = field.value
                if (originalValue != null && originalValue.isNotEmpty())
                    field.value = encryptValue(encryptionKey, encryptionAlias, originalValue)
            }
        }
    }

    private fun encryptValue(pem: String, alias: String, value: String): String {
        return try {
            val parser = PEMParser(StringReader(pem))
            val obj = parser.readObject()
            val converter = JcaPEMKeyConverter()
            parser.close()
            val pk = converter.getPublicKey(obj as SubjectPublicKeyInfo)
            val cypher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cypher.init(Cipher.ENCRYPT_MODE, pk)
            val encrypted = Hex.encode(cypher.doFinal(value.toByteArray())).toString(Charset.defaultCharset())

            "$alias:$encrypted"
        } catch (e: Exception) {
            Log.e("ProviderLoginForm#encryptValue", "Encryption failed: ${e.message}")

            value
        }
    }

    fun validateForm(completion: FormValidationCompletionListener) {
        rows.forEach { row ->
            row.fields.forEach { field ->
                val value = field.value
                val maxLength = field.maxLength

                if (!field.isOptional && (value == null || value.isBlank())) {
                    // Required field not filled
                    completion.invoke(false, LoginFormError(LoginFormErrorType.MISSING_REQUIRED_FIELD, field.name))

                    return
                } else if (value != null && maxLength != null && value.length > maxLength) {
                    // Value is too long
                    completion.invoke(false, LoginFormError(LoginFormErrorType.MAX_LENGTH_EXCEEDED, field.name))

                    return
                } else {
                    field.validations?.forEach { validation ->
                        if (value?.regexValidate(validation.regExp) == false) {
                            val error = LoginFormError(LoginFormErrorType.VALIDATION_FAILED, field.name)
                            error.additionalError = validation.errorMsg
                            completion.invoke(false, error)
                            return
                        }
                    }
                }
            }
        }

        completion.invoke(true, null)
    }
}