package us.frollo.frollosdk.events

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

internal enum class EventNames {
    @SerializedName("TEST_EVENT") TEST;

    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}