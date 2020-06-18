/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/**
 * Models a login form for collecting details needed to link an account and provides validation and encryption features. Use ProviderLoginFormDisplay for easier handling of the login form at a UI level.
 */
data class ProviderLoginForm(

    /** ID of the login form (optional) */
    @SerializedName("id") val formId: String?,

    /** Forgot password URL for the selected provider (optional) */
    @SerializedName("forgetPasswordURL") val forgetPasswordUrl: String?,

    /** Additional help message for the current login form (optional) */
    @SerializedName("help") val help: String?,

    /** Additional information title for MFA login forms (optional) */
    @SerializedName("mfaInfoTitle") val mfaInfoTitle: String?,

    /** Additional information on how to complete the MFA challenge login form (optional) */
    @SerializedName("mfaInfoText") val mfaInfoText: String?,

    /** Time before the MFA challenge times out (optional) */
    @SerializedName("mfaTimeout") val mfaTimeout: Long?,

    /** Type of login form see [ProviderFormType] for details */
    @SerializedName("formType") val formType: ProviderFormType,

    /** List of login form rows. Use a ProviderLoginFormDisplay to collate multiple choice rows together for easier UI display */
    @SerializedName("row") val rows: List<ProviderFormRow>
) {

    /**
     * Tells whether the values of the login form must encrypted or not based on type of [encryptionType] and [formType]
     *
     * @param encryptionType Provider encryption type
     */
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

    /**
     * Encrypt values on the login form using a provider's encryption key
     *
     * @param encryptionKey PEM formatted public key to use for encryption
     * @param encryptionAlias Alias of the encryption key appended to the value fields
     */
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

    /**
     * Validate the form values. Checks max length, required fields and evaluates any regex provided
     *
     * @param completion Validation completion handler with valid result and optional error if validation fails
     */
    fun validateForm(completion: FormValidationCompletionListener) {
        rows.forEach { row ->
            row.fields.forEach { field ->
                val value = field.value
                val maxLength = field.maxLength

                if (!field.isOptional && (value == null || value.isBlank())) {
                    // Required field not filled
                    completion.invoke(false, LoginFormError(LoginFormErrorType.MISSING_REQUIRED_FIELD, row.label))

                    return
                } else if (value != null && maxLength != null && value.length > maxLength) {
                    // Value is too long
                    completion.invoke(false, LoginFormError(LoginFormErrorType.MAX_LENGTH_EXCEEDED, row.label))

                    return
                } else {
                    field.validations?.forEach { validation ->
                        if (value?.regexValidate(validation.regExp) == false) {
                            val error = LoginFormError(LoginFormErrorType.VALIDATION_FAILED, row.label)
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
