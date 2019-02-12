package us.frollo.frollosdk.model.coredata.aggregation.providers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.annotations.SerializedName
import java.lang.Exception

data class ProviderFormField(
        @SerializedName("id") val fieldId: String,
        @SerializedName("image") val image: List<Byte>?,
        @SerializedName("name") val name: String,
        @SerializedName("maxLength") val maxLength: Int?,
        @SerializedName("type") val type: ProviderFieldType,
        @SerializedName("value") var value: String?,
        @SerializedName("prefix") var prefix: String?,
        @SerializedName("suffix") var suffix: String?,
        @SerializedName("isOptional") val isOptional: Boolean,
        @SerializedName("valueEditable") val valueEditable: Boolean,
        @SerializedName("option") val options: List<ProviderFieldOption>?,
        @SerializedName("validation") val validations: List<ProviderFieldValidation>?
) {
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