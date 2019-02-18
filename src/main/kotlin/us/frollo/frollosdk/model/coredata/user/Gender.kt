package us.frollo.frollosdk.model.coredata.user

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Represents the gender of the user */
enum class Gender {
    /** Male */
    @SerializedName("male") MALE,
    /** Female */
    @SerializedName("female") FEMALE,
    /** Other or unspecified */
    @SerializedName("other") OTHER;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}