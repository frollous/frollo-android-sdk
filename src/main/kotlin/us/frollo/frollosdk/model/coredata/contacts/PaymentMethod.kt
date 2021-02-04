package us.frollo.frollosdk.model.coredata.contacts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/**
 * Indicates the payment method type for the contact
 */
enum class PaymentMethod {

    /** Pay Anyone type*/
    @SerializedName("pay_anyone") PAY_ANYONE,

    /** Pay ID type */
    @SerializedName("payid") PAY_ID,

    /** BPay type */
    @SerializedName("bpay") BPAY,

    /** International Payment type */
    @SerializedName("international") INTERNATIONAL;

    /** Enum to serialized string */
    // This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
        // Try to get the annotation value if available instead of using plain .toString()
        // Fallback to super.toString() in case annotation is not present/available
        serializedName() ?: super.toString()
}
