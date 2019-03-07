package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** The type of login form encryption supported by the provider if applicable */
enum class ProviderEncryptionType {

    /** Unsupported */
    @SerializedName("unsupported") UNSUPPORTED,

    /** Encrypt Values. Encrypt the value field of the login form */
    @SerializedName("encrypt_values") ENCRYPT_VALUES;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}