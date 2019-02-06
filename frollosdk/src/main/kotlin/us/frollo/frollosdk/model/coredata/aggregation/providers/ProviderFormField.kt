package us.frollo.frollosdk.model.coredata.aggregation.providers

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import java.lang.Exception
import java.nio.IntBuffer

data class ProviderFormField(
        @SerializedName("id") val fieldId: String,
        @SerializedName("image") val image: List<Int>?,
        @SerializedName("name") val name: String,
        @SerializedName("maxLength") val maxLength: Int?,
        @SerializedName("type") val type: ProviderFieldType,
        @SerializedName("value") var value: String?,
        @SerializedName("isOptional") val isOptional: Boolean,
        @SerializedName("valueEditable") val valueEditable: Boolean,
        @SerializedName("option") val options: List<ProviderFieldOption>?,
        @SerializedName("validation") val validations: List<ProviderFieldValidation>?
) {
    val imageBitmap: Bitmap?
        get() {
            return image?.let {
                try {
                    val pixelArray = it.toIntArray()
                    // TODO: How to get the actual width and height?
                    val bitmap = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)
                    bitmap.copyPixelsFromBuffer(IntBuffer.wrap(pixelArray))
                    bitmap
                } catch (e: Exception) {
                    null
                }
            } ?: run {
                null
            }
        }
}