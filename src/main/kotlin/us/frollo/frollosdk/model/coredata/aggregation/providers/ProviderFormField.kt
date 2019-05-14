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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.annotations.SerializedName
import java.lang.Exception

/** Field representing a piece of information to be entered and validated */
data class ProviderFormField(

    /** Unique ID of the field */
    @SerializedName("id") val fieldId: String,

    /** Byte array representing an image (optional) */
    @SerializedName("image") val image: List<Byte>?,

    /** Name of the field to be displayed to the user */
    @SerializedName("name") val name: String,

    /** Maximum length of the text to be entered (optional) */
    @SerializedName("maxLength") val maxLength: Int?,

    /** Type of field. This will affect the display of the field. See [ProviderFieldType] for details */
    @SerializedName("type") val type: ProviderFieldType,

    /** Value entered into the field (optional) */
    @SerializedName("value") var value: String?,

    /** Prefix to be displayed before the field to user (optional) */
    @SerializedName("prefix") var prefix: String?,

    /** Suffix to be displayed after the field to the user (optional) */
    @SerializedName("suffix") var suffix: String?,

    /** Optional field indicator indicating if this is required to be filled by the user */
    @SerializedName("isOptional") val isOptional: Boolean,

    /** Indicates if the user can edit the value */
    @SerializedName("valueEditable") val valueEditable: Boolean,

    /** List of options to be selected if [type] is [ProviderFieldType.OPTION] (optional) */
    @SerializedName("option") val options: List<ProviderFieldOption>?,

    /** List of validations to be performed on the field (optional) */
    @SerializedName("validation") val validations: List<ProviderFieldValidation>?
) {

    /** Bitmap of the [image] bytes */
    val imageBitmap: Bitmap?
        get() {
            return image?.let {
                try {
                    val byteArray = it.toByteArray()
                    BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                } catch (e: Exception) {
                    null
                }
            } ?: run {
                null
            }
        }
}