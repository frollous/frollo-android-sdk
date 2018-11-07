package us.frollo.frollosdk.extensions

import com.google.gson.annotations.SerializedName

/**
 * Retrieves the value from the [SerializedName] annotation, if present
 */
fun Enum<*>.serializedName(): String? {
    return javaClass.getField(name).annotations
            .filter { it.annotationClass == SerializedName::class }
            .map { it as SerializedName }
            .firstOrNull()?.value
}