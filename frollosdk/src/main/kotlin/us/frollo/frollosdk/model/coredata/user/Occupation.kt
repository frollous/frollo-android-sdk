package us.frollo.frollosdk.model.coredata.user

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Represents occupation of the user */
enum class Occupation {
    /** Clerical and Administrative Workers */
    @SerializedName("clerical_and_administrative_workers") CLERICAL_AND_ADMINISTRATIVE_WORKERS,
    /** Community and Personal Service Workers */
    @SerializedName("community_and_personal_service_workers") COMMUNITY_AND_PERSONAL_SERVICE_WORKERS,
    /** Labourers */
    @SerializedName("labourers") LABOURERS,
    /** Machinery Operators and Drivers */
    @SerializedName("machinery_operators_and_drivers") MACHINERY_OPERATORS_AND_DRIVERS,
    /** Managers */
    @SerializedName("managers") MANAGERS,
    /** Professionals */
    @SerializedName("professionals") PROFESSIONALS,
    /** Sales Workers */
    @SerializedName("sales_workers") SALES_WORKERS,
    /** Technicians and Trades Workers */
    @SerializedName("technicians_and_trades_workers") TECHNICIANS_AND_TRADES_WORKERS;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}