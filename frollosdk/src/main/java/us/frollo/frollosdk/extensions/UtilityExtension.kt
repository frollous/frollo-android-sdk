package us.frollo.frollosdk.extensions

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

/* Kotlin extensions */
/**
 * Checks if [value1] and [value2] are not null and executes a function after.
 * Think of this as a 2 parameters `value?.let { ... }`
 */
fun <T1, T2> ifNotNull(value1: T1?, value2: T2?, bothNotNull: (T1, T2) -> (Unit)) {
    if (value1 != null && value2 != null) {
        bothNotNull(value1, value2)
    }
}

/* Gson extensions */
/**
 * Converts a [json] to a given [T] object.
 * @return the converted object.
 */
internal inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object: TypeToken<T>() {}.type)

/**
 * Retrieves the value from the [SerializedName] annotation, if present
 */
internal fun Enum<*>.serializedName(): String? {
    return javaClass.getField(name).annotations
            .filter { it.annotationClass == SerializedName::class }
            .map { it as SerializedName }
            .firstOrNull()?.value
}