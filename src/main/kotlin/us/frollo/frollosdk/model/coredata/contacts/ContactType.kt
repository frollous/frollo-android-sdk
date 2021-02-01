package us.frollo.frollosdk.model.coredata.contacts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

enum class ContactType {

    /** PayID type is telephone */
    @SerializedName("pay_anyone") PAY_ANYONE,

    /** PayID type is email */
    @SerializedName("pay_id") PAY_ID,

    /** PayID type is org identifier*/
    @SerializedName("bpay") BPAY,

    /** PayID type is abn */
    @SerializedName("international") INTERNATIONAL;

    /** Enum to serialized string */
    // This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
        // Try to get the annotation value if available instead of using plain .toString()
        // Fallback to super.toString() in case annotation is not present/available
        serializedName() ?: super.toString()
}
