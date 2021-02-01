package us.frollo.frollosdk.model.coredata.contacts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

enum class ContactBillerCRNType {

    /** PayID type is telephone */
    @SerializedName("fixed_crn") FIXED_CRN,

    /** PayID type is email */
    @SerializedName("variable_crn") VARIABLE_CRN,

    /** PayID type is org identifier*/
    @SerializedName("intelligent_crn") INTELLIGENT_CRN;

    /** Enum to serialized string */
    // This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
        // Try to get the annotation value if available instead of using plain .toString()
        // Fallback to super.toString() in case annotation is not present/available
        serializedName() ?: super.toString()
}
