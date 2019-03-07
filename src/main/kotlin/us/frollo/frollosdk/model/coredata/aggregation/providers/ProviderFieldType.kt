package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Type of field indicating what type of data will be provided and how it should be displayed */
enum class ProviderFieldType {

    /** Checkbox. Show a standard check box to the user */
    @SerializedName("checkbox") CHECKBOX,

    /** Image. Show the image to the user */
    @SerializedName("image") IMAGE,

    /** Option. Show a drop down list of options to the user */
    @SerializedName("option") OPTION,

    /** Password. Show a secure text field to the user */
    @SerializedName("password") PASSWORD,

    /** Radio button. Show a radio button list to the user */
    @SerializedName("radio") RADIO,

    /** Text. Show a regular text field to the user */
    @SerializedName("text") TEXT;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}