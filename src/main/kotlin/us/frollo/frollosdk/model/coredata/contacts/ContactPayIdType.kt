package us.frollo.frollosdk.model.coredata.contacts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

enum class ContactPayIdType {
    /** PayID type is telephone */
    @SerializedName("telephone") TELEPHONE,

    /** PayID type is email */
    @SerializedName("email") EMAIL,

    /** PayID type is org identifier*/
    @SerializedName("org_identifier") ORG_IDENTIFIER,

    /** PayID type is abn */
    @SerializedName("abn") ABN;

    /** Enum to serialized string */
    // This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
        // Try to get the annotation value if available instead of using plain .toString()
        // Fallback to super.toString() in case annotation is not present/available
        serializedName() ?: super.toString()
}
