package us.frollo.frollosdk.model.coredata.aggregation.merchants

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** The type of merchant so non-retail ones can be identified */
enum class MerchantType {

    /** Retailer */
    @SerializedName("retailer") RETAILER,

    /** Transactional */
    @SerializedName("transactional") TRANSACTIONAL,

    /** Unknown */
    @SerializedName("unknown") UNKNOWN;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}