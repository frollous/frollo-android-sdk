package us.frollo.frollosdk.model.coredata.user

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/**
 * Represents the make up of the household
 */
enum class HouseholdType {
    /** Single with no dependents */
    @SerializedName("single") SINGLE,
    /** Single with children */
    @SerializedName("single_parent") SINGLE_PARENT,
    /** Couple with no dependents */
    @SerializedName("couple") COUPLE,
    /** Couple with children */
    @SerializedName("couple_with_kids") COUPLE_WITH_KIDS;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}