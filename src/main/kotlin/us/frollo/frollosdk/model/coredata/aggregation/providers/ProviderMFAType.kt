package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Type of MFA the provider uses to authenticate */
enum class ProviderMFAType {

    /** Token. Usually an OTP or RSA token */
    @SerializedName("token") TOKEN,

    /** Question. Security question and answer typically */
    @SerializedName("question") QUESTION,

    /** Strong Multiple. Multiple different types */
    @SerializedName("strong_multiple") STRONG_MULTIPLE,

    /** Image- usually a captcha */
    @SerializedName("image") IMAGE,

    /** Unknown */
    @SerializedName("unknown") UNKNOWN;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}