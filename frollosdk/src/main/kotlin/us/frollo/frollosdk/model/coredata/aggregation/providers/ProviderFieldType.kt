package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

enum class ProviderFieldType {
    @SerializedName("checkbox") CHECKBOX,
    @SerializedName("image") IMAGE,
    @SerializedName("option") OPTION,
    @SerializedName("password") PASSWORD,
    @SerializedName("radio") RADIO,
    @SerializedName("text") TEXT;

    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}