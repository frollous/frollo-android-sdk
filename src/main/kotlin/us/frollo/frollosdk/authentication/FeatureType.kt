package us.frollo.frollosdk.authentication

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/**
 * User feature types
 */
enum class FeatureType {
    /** Feature aggregation */
    @SerializedName("aggregation") AGGREGATION;

    /** Enum to serialized string */
    override fun toString(): String =
            serializedName() ?: super.toString()
}