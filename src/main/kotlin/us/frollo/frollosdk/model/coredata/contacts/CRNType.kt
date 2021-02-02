package us.frollo.frollosdk.model.coredata.contacts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Indicates the type of the Biller CRN */
enum class CRNType {

    /** Fixed */
    @SerializedName("fixed_crn") FIXED,

    /** Variable */
    @SerializedName("variable_crn") VARIABLE,

    /** Intelligent */
    @SerializedName("intelligent_crn") INTELLIGENT;

    /** Enum to serialized string */
    // This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
        // Try to get the annotation value if available instead of using plain .toString()
        // Fallback to super.toString() in case annotation is not present/available
        serializedName() ?: super.toString()
}
