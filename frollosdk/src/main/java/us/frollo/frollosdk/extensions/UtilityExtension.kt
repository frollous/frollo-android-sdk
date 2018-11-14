package us.frollo.frollosdk.extensions

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

/* Gson extensions */
/**
 * Converts a [json] to a given [T] object.
 * @return the converted object.
 */
inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object: TypeToken<T>() {}.type)

/**
 * Retrieves the value from the [SerializedName] annotation, if present
 */
fun Enum<*>.serializedName(): String? {
    return javaClass.getField(name).annotations
            .filter { it.annotationClass == SerializedName::class }
            .map { it as SerializedName }
            .firstOrNull()?.value
}