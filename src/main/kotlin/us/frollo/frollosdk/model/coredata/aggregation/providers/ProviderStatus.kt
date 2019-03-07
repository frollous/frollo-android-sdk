package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Status of the support of the provider */
enum class ProviderStatus {

    /** Beta. Support is still being developed so may have some issues */
    @SerializedName("beta") BETA,

    /** Disabled. Provider has been disabled by the aggregator */
    @SerializedName("disabled") DISABLED,

    /** Supported. Provider is fully supported */
    @SerializedName("supported") SUPPORTED,

    /** Unsupported. Provider is no longer supported */
    @SerializedName("unsupported") UNSUPPORTED;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}