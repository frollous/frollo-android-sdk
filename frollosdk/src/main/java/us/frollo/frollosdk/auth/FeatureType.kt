package us.frollo.frollosdk.auth

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

enum class FeatureType {
    @SerializedName("aggregation") AGGREGATION;

    override fun toString(): String =
            serializedName() ?: super.toString()
}