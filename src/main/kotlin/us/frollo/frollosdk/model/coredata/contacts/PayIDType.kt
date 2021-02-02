package us.frollo.frollosdk.model.coredata.contacts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Indicates the type of the Pay ID */
enum class PayIDType {

    /** PayID type is telephone */
    @SerializedName("telephone") PHONE_NUMBER,

    /** PayID type is email */
    @SerializedName("email") EMAIL,

    /** PayID type is org identifier*/
    @SerializedName("org_identifier") ORGANISATION_ID,

    /** PayID type is ABN */
    @SerializedName("abn") ABN;

    /** Enum to serialized string */
    // This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
        // Try to get the annotation value if available instead of using plain .toString()
        // Fallback to super.toString() in case annotation is not present/available
        serializedName() ?: super.toString()
}
